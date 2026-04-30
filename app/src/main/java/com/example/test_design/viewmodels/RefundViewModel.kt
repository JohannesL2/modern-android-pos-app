package com.example.test_design.viewmodels

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_design.R
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.entity.OrderEntity
import com.example.test_design.data.entity.OrderRow
import com.example.test_design.data.entity.OrderStatus
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.data.entity.PaymentEntity
import com.example.test_design.data.entity.PaymentStatus
import com.example.test_design.data.entity.PaymentType
import com.example.test_design.data.utils.ReceiptNumberGenerator
import com.example.test_design.data.utils.isValidReceipt
import com.example.test_design.scancontrol.ScanSoundPlayer
import com.example.test_design.utils.ReceiptFormatter
import com.example.test_design.utils.ReceiptFormatter.drawableToBase64
import com.example.test_design.utils.bitmapToBase64
import com.example.test_design.utils.generateReceiptBarcodeBitmap
import com.example.integration.api.ApiModule
import com.example.integration.api.model.PaymentError
import com.example.integration.api.model.PaymentResult
import com.example.integration.api.model.PrintContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class SearchSource {
    SCANNER,
    MANUAL
}

class RefundViewModel : ViewModel() {

    var showCardMismatchDialog by mutableStateOf(false)
        private set

    private var pendingRefundData: PendingRefundData? = null
    private val terminalApi = ApiModule.terminal
    var refundCart = mutableStateListOf<OrderRow>() //Creates a list with items to refund
        private set
    @get:StringRes
    var refundErrorMessage by mutableStateOf<Int?>(null) // Error message resource if something goes wrong with refund
        private set

    var lastPaymentInfo: String? = null
    var completedRefund by mutableStateOf<CompletedRefund?>(null)
        private set

    /**
     * Function for searching receipts in refundscreen, if receipt is not valid give a error message and return false.
     * If receipt is a valid format and exists in the database give no error message and get all items from the order rows.
     */
    suspend fun searchReceipt(
        source: SearchSource,
        receipt: String,
        orderDao: OrderDao
    ): Boolean {
        if (!receipt.isValidReceipt()) {
            refundErrorMessage = R.string.refund_invalid_receipt
            return false
        }

        val rows = orderDao.getRowsByReceipt(receipt)
        refundCart.clear()

        return if (rows.isEmpty()) {
            refundErrorMessage = R.string.receipt_not_found
            if (source == SearchSource.SCANNER) ScanSoundPlayer.playDenied()
            false
        } else if (orderDao.getOrderStatus(receipt) != OrderStatus.PAID) {
            refundErrorMessage = R.string.refund_invalid_receipt_status
            false
        } else {
            refundErrorMessage = null
            refundCart.addAll(rows)
            if (source == SearchSource.SCANNER) ScanSoundPlayer.playSuccess()
            true
        }
    }

    data class RefundSelection(
        val rowId: Int,
        val articleNumber: String,
        val productName: String,
        val unitPrice: Int,
        val quantityToRefund: Int,
        val variantValue: String?,
    )



    data class PendingRefundData(
        val receiptNumber: String,
        val selections: List<RefundSelection>,
        val order: OrderEntity,
        val orderDao: OrderDao,
        val paymentDao: PaymentDao,
        val originalMaskedPan: String?,
        val totalAmount: Int
    )
    data class RefundResult(
        val receiptNumber: String,
        val refundRows: List<OrderRow>
    )

    data class CompletedRefund(
        val result: RefundResult,
        val originalReceiptNumber: String
    )

    suspend fun refund(
        receiptNumber: String,
        selections: List<RefundSelection>,
        order: OrderEntity,
        orderDao: OrderDao,
        paymentDao: PaymentDao,
        skipCardCheck: Boolean = false
    ): RefundResult? {
        completedRefund = null

        if (selections.isEmpty()) return null

        val rowsById = orderDao.getRowsByReceipt(receiptNumber).associateBy { it.id }
        val validSelections = selections.mapNotNull { selection ->
            val originalRow = rowsById[selection.rowId] ?: return@mapNotNull null
            val availableQty = originalRow.quantity.coerceAtLeast(0)
            val quantityToRefund = selection.quantityToRefund.coerceAtMost(availableQty)

            if (quantityToRefund <= 0) return@mapNotNull null

            RefundSelection(
                rowId = originalRow.id,
                articleNumber = originalRow.articleNumber,
                productName = originalRow.productName,
                unitPrice = originalRow.unitPrice,
                quantityToRefund = quantityToRefund,
                variantValue = originalRow.variantValue
            )
        }

        if (validSelections.isEmpty()) return null

        val refundReceipt = ReceiptNumberGenerator.receiptNumber(orderDao)
        val totalAmount = validSelections.sumOf { it.unitPrice * it.quantityToRefund }

        val pendingRefundOrder = OrderEntity(
            orderNumber = refundReceipt,
            originalOrderId = order.id,
            totalAmount = -totalAmount,
            status = OrderStatus.PENDING,
            refundedAmount = 0,
        )


        val refundRows = validSelections.map { selection ->
            val refundAmount = selection.unitPrice * selection.quantityToRefund
            OrderRow(
                orderNumber = refundReceipt,
                articleNumber = selection.articleNumber,
                productName = selection.productName,
                unitPrice = selection.unitPrice,
                lineAmount = -refundAmount,
                quantity = selection.quantityToRefund,
                refundedQuantity = 0,
                refundedAmount = 0.0,
                variantValue = selection.variantValue
            )
        }

        val payment = paymentDao.getPaymentByOrderNumber(order.orderNumber)
        val cardDetails = payment?.id?.let { paymentId ->
            paymentDao.getCardDetailsByPaymentId(paymentId)
        }
        val originalMaskedPan = cardDetails?.maskedPan

        val result = terminalApi.refundUnlinked(
            amountMinorUnits = totalAmount * 100,
            originalMaskedPan = originalMaskedPan,
            skipCardCheck = skipCardCheck
        )

        return when (result) {
            is PaymentResult.Success -> {
                orderDao.insertFullOrder(pendingRefundOrder, refundRows)
                val data = result.appSpecificData
                val paymentInfoText: String? = result.paymentInfo

                paymentDao.insertPayment(
                    PaymentEntity(
                        orderNumber = refundReceipt,
                        type = PaymentType.REFUND,
                        amount = -totalAmount,
                        status = PaymentStatus.REFUNDED,
                        method = PaymentMethod.CARD,
                        terminalId = "001",
                        userId = "001",
                        paidAmount = -totalAmount
                    )
                )
                paymentDao.upsertCardDetailsForOrder(
                    orderNumber = refundReceipt,
                    cardBrand = result.brand,
                    maskedPan = result.maskedPan,
                    paymentInfo = paymentInfoText,
                    appSpecificData = data
                )

                orderDao.updateRefund(
                    refundReceipt,
                    totalAmount,
                    OrderStatus.REFUNDED
                )

                val selectionById = validSelections.associateBy { it.rowId }
                val updatedRows = rowsById.values.map { originalRow ->
                    val selection = selectionById[originalRow.id] ?: return@map originalRow
                    val refundAmount = selection.unitPrice * selection.quantityToRefund
                    val newRefundedQuantity = originalRow.refundedQuantity + selection.quantityToRefund
                    val newRefundedAmount = originalRow.refundedAmount + refundAmount.toDouble()

                    orderDao.updateOrderRowRefund(
                        rowId = originalRow.id,
                        refundedQuantity = newRefundedQuantity,
                        refundedAmount = newRefundedAmount
                    )

                    originalRow.copy(
                        refundedQuantity = newRefundedQuantity,
                        refundedAmount = newRefundedAmount
                    )
                }

                val newRefundedAmount = updatedRows.sumOf { it.refundedAmount.toInt() }
                    .coerceAtMost(order.totalAmount)
                val newStatus = if (newRefundedAmount >= order.totalAmount) {
                    OrderStatus.REFUNDED
                } else {
                    OrderStatus.PAID
                }

                orderDao.updateRefund(
                    receiptNumber = receiptNumber,
                    refundedAmount = newRefundedAmount,
                    status = newStatus
                )
                lastPaymentInfo = paymentInfoText
                completedRefund = CompletedRefund(
                    result = RefundResult(
                        receiptNumber = refundReceipt,
                        refundRows = refundRows
                    ),
                    originalReceiptNumber = receiptNumber
                )
                Log.d("REFUND", "Multiple rows refund success: $totalAmount kr")
                RefundResult(
                    receiptNumber = refundReceipt,
                    refundRows = refundRows
                )
            }

            is PaymentResult.Failure -> {

                if (result.error is PaymentError.WrongCard) {

                    pendingRefundData = PendingRefundData(
                        receiptNumber,
                        selections,
                        order,
                        orderDao,
                        paymentDao,
                        originalMaskedPan,
                        totalAmount
                    )

                    showCardMismatchDialog = true
                    return null
                }

                completedRefund = null
                orderDao.updateRefund(refundReceipt, 0, OrderStatus.FAILED)
                null
            }

            PaymentResult.Aborted -> {
                completedRefund = null
                orderDao.updateRefund(refundReceipt, 0, OrderStatus.CANCELLED)
                Log.d("REFUND", "Refund aborted by user/termial")
                null
            }
        }
    }

    fun continueRefundAnyway(onSuccess: () -> Unit) {
        val data = pendingRefundData ?: return
        showCardMismatchDialog = false

        viewModelScope.launch {
            val result = refund(
                receiptNumber = data.receiptNumber,
                selections = data.selections,
                order = data.order,
                orderDao = data.orderDao,
                paymentDao = data.paymentDao,
                skipCardCheck = true
            )

            if (result != null) {
                onSuccess()
            } else {
                if (refundErrorMessage == null) {
                    refundErrorMessage = R.string.refund_failed
                }
            }
            pendingRefundData = null
        }
    }



    fun cancelRefund() {
        showCardMismatchDialog = false
        pendingRefundData = null
    }
    fun onReceiptInputChanged(input: String) {
        if (input.length != 6) {
            refundCart.clear()
            refundErrorMessage = null
        }
    }
    fun clearRefundState() {
        refundCart.clear()
        refundErrorMessage = null
    }

    fun clearCompletedRefund() {
        completedRefund = null
    }

    /**
     *  This function prints a receipt when we make a refund.
     */

    fun printRefundReceipt(
        context: Context,
        refundRows: List<OrderRow>,
        receiptNumber: String,
        originalReceiptNumber: String
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val logoBase64 = drawableToBase64(context, R.drawable.receiptcoffeelogo)
            val barcodeBitmap = generateReceiptBarcodeBitmap(
                receiptNumber = "R$receiptNumber",
                width = 300,
                height = 120
            )
            val barcodeBase64 = bitmapToBase64(barcodeBitmap)

            val html = ReceiptFormatter.generateRefundHtml(
                refundRows = refundRows,
                originalReceiptNumber = originalReceiptNumber,
                receiptNumber = receiptNumber,
                logoBase64 = logoBase64,
                barcodeBase64 = barcodeBase64,
                paymentInfo = lastPaymentInfo
            )

            terminalApi.print(html, PrintContentType.HTML)
        }
    }

    fun printCompletedRefundReceipt(context: Context): Boolean {
        val refund = completedRefund ?: return false
        printRefundReceipt(
            context = context,
            refundRows = refund.result.refundRows,
            receiptNumber = refund.result.receiptNumber,
            originalReceiptNumber = refund.originalReceiptNumber
        )
        return true
    }

}
