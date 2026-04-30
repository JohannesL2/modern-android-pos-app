package com.example.test_design.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.entity.OrderEntity
import com.example.test_design.data.entity.OrderRow
import com.example.test_design.data.entity.OrderStatus
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.data.entity.PaymentEntity
import com.example.test_design.data.entity.PaymentStatus
import com.example.test_design.data.entity.PaymentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.withContext
import com.example.test_design.domain.models.CartItem
import com.example.test_design.data.utils.ReceiptNumberGenerator

class OrderViewModel(
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao
) : ViewModel() {

    suspend fun nextReceiptNumber(): String = ReceiptNumberGenerator.receiptNumber(orderDao)

    suspend fun getOrderStatus(receiptNumber: String): OrderStatus? =
        orderDao.getOrderStatus(receiptNumber)

    suspend fun createOrderAndReturnReceipt(
        receiptNumber: String,
        cart: List<CartItem>,
        paymentMethod: PaymentMethod,
        initialPaidAmount: Int? = null,
    ): String = withContext(Dispatchers.IO) {
        val order = OrderEntity(
            orderNumber = receiptNumber,
            totalAmount = cart.sumOf { it.totalPrice },
            status = OrderStatus.PENDING,
        )

        val rows = cart.map { item ->
            OrderRow(
                orderNumber = receiptNumber,
                productName = item.product.name,
                articleNumber = item.product.articleNumber,
                variantValue = item.product.variantValue,
                unitPrice = item.pricePerItem,
                quantity = item.quantity,
                lineAmount = item.totalPrice,
                refundedQuantity = 0,
            )
        }

        orderDao.insertFullOrder(order, rows)
        paymentDao.insertPayment(
            PaymentEntity(
                orderNumber = receiptNumber,
                type = PaymentType.PAYMENT,
                amount = order.totalAmount,
                status = PaymentStatus.PENDING,
                method = paymentMethod,
                userId = "001",
                terminalId = "001",
                paidAmount = initialPaidAmount ?: order.totalAmount,
            )
        )

        receiptNumber
    }

    suspend fun setOrderStatus(
        receiptNumber: String,
        status: OrderStatus,
        paymentMethod: PaymentMethod?
    ) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(receiptNumber = receiptNumber, status = status)
        val paymentStatus = when (status) {
            OrderStatus.PAID -> PaymentStatus.COMPLETED
            OrderStatus.REFUNDED -> PaymentStatus.REFUNDED
            OrderStatus.CANCELLED -> PaymentStatus.CANCELLED
            OrderStatus.FAILED -> PaymentStatus.FAILED
            else -> PaymentStatus.PENDING
        }
        paymentDao.updatePaymentStatusAndMethod(
            orderNumber = receiptNumber,
            status = paymentStatus,
            paymentMethod = paymentMethod
        )
    }

    suspend fun saveCardDetails(
        receiptNumber: String,
        appSpecificData: String,
        brand: String?,
        maskedPan: String?,
        paymentInfo: String?
    ) = withContext(Dispatchers.IO) {
        paymentDao.upsertCardDetailsForOrder(
            orderNumber = receiptNumber,
            cardBrand = brand,
            maskedPan = maskedPan,
            paymentInfo = paymentInfo,
            appSpecificData = appSpecificData
        )
    }

    suspend fun addPaymentPart(
        receiptNumber: String,
        amountMinorUnits: Int,
        brand: String?,
        maskedPan: String?,
        paymentInfo: String?,
        appSpecificData: String?
    ) = withContext(Dispatchers.IO) {
        paymentDao.insertPaymentCardDetailsForOrderAndIncrementPaidAmount(
            orderNumber = receiptNumber,
            paidAmount = amountMinorUnits / 100,
            cardBrand = brand,
            maskedPan = maskedPan,
            paymentInfo = paymentInfo,
            appSpecificData = appSpecificData
        )
    }

    fun updateOrderStatus(
        receiptNumber: String,
        status: OrderStatus,
        paymentMethod: PaymentMethod?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            setOrderStatus(receiptNumber, status, paymentMethod)
        }
    }
    fun cancelOrder(
        receiptNumber: String,
        paymentMethod: PaymentMethod? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val status = orderDao.getOrderStatus(receiptNumber)
            if (status == OrderStatus.PENDING || status == OrderStatus.FAILED) {
                orderDao.updateOrderStatus(
                    receiptNumber = receiptNumber,
                    status = OrderStatus.CANCELLED
                )
                paymentDao.updatePaymentStatusAndMethod(
                    orderNumber = receiptNumber,
                    status = PaymentStatus.CANCELLED,
                    paymentMethod = paymentMethod
                )
            }
        }
    }

    suspend fun updatePaymentStatus(
        receiptNumber: String,
        status: PaymentStatus,
        paymentMethod: PaymentMethod?
    ) {
        paymentDao.updatePaymentStatusAndMethod(receiptNumber, status, paymentMethod)
    }

    fun CartItem.totalUnitPrice(): Int {
        val modifiers = variantSelections.sumOf { it.priceModifier }
        return product.price + modifiers
    }
    fun updateCardDetails(
        receiptNumber: String,
        appSpecificData: String,
        brand: String?,
        maskedPan: String?,
        paymentInfo: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            saveCardDetails(receiptNumber, appSpecificData, brand, maskedPan, paymentInfo)
        }
    }

    suspend fun updateOrderFromCart(
        receiptNumber: String,
        cart: List<CartItem>
    ) = withContext(Dispatchers.IO) {
        val status = orderDao.getOrderStatus(receiptNumber)

        if (status != OrderStatus.PENDING && status != OrderStatus.FAILED) {
            return@withContext
        }

        val newTotal = cart.sumOf { it.totalPrice }

        val newRows = cart.map { item ->
            OrderRow(
                orderNumber = receiptNumber,
                productName = item.product.name,
                articleNumber = item.product.articleNumber,
                variantValue = item.product.variantValue,
                unitPrice = item.pricePerItem,
                quantity = item.quantity,
                lineAmount = item.totalPrice,
                refundedQuantity = 0,
            )
        }

        orderDao.replaceOrder(receiptNumber, newTotal, newRows)

        paymentDao.updatePaymentAmount(receiptNumber, newTotal)
    }
}
