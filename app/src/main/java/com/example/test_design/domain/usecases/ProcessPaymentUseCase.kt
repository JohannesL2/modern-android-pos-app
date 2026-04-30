package com.example.test_design.domain.usecases

import com.example.test_design.data.entity.OrderStatus
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.data.entity.PaymentStatus
import com.example.test_design.domain.models.CartItem
import com.example.test_design.viewmodels.OrderViewModel
import com.example.test_design.viewmodels.PaymentViewModel
import com.example.integration.api.model.PaymentError
import com.example.integration.api.model.PaymentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProcessPaymentUseCase(
    private val paymentViewModel: PaymentViewModel,
    private val orderViewModel: OrderViewModel,
) {
    suspend operator fun invoke(
        cart: List<CartItem>,
        total: Int,
    ): PaymentOutcome {
        val receiptNumber = findReusableRecieptNumber()
            ?: createOrderFromCart(cart)

        return processExistingOrder(receiptNumber, total, cart)
    }

    private suspend fun findReusableRecieptNumber(): String? {
        val order = paymentViewModel.lastOrder ?: return null
        val status = withContext(Dispatchers.IO) {
            orderViewModel.getOrderStatus(order.receiptNumber)
        }
        paymentViewModel.lastOrder = order
        return if (status == OrderStatus.PENDING || status == OrderStatus.FAILED ) {
            order.receiptNumber
        } else {
            null
        }
    }

    private suspend fun createOrderFromCart(cart: List<CartItem>): String {
        val receiptNumber = withContext(Dispatchers.IO) {
            orderViewModel.nextReceiptNumber()
        }
        val order = paymentViewModel.buildOrderFromCart(
            cart = cart,
            paymentMethod = PaymentMethod.CARD,
            receiptNumber = receiptNumber,
        )

        paymentViewModel.lastOrder = order

        return orderViewModel.createOrderAndReturnReceipt(
            receiptNumber = order.receiptNumber,
            cart = order.items,
            paymentMethod = order.paymentMethod,
        )
    }

    private suspend fun processExistingOrder(
        receiptNumber: String,
        total: Int,
        cart: List<CartItem>
    ): PaymentOutcome {
        val paymentResult = try {
            paymentViewModel.payTotal(total)
        } catch (e: Exception) {
            null
        }

        orderViewModel.updateOrderFromCart(receiptNumber, cart)

        return when (paymentResult) {
            is PaymentResult.Success -> {
                orderViewModel.saveCardDetails(
                    receiptNumber = receiptNumber,
                    appSpecificData = paymentViewModel.lastPaidAppSpecificData ?: "",
                    brand = paymentViewModel.lastCardBrand,
                    maskedPan = paymentViewModel.lastMaskedPan,
                    paymentInfo = paymentViewModel.lastPaymentInfo,
                )
                orderViewModel.setOrderStatus(
                    receiptNumber = receiptNumber,
                    status = OrderStatus.PAID,
                    paymentMethod = PaymentMethod.CARD
                )
                PaymentOutcome.Success(receiptNumber)
            }
            is PaymentResult.Aborted -> {
                orderViewModel.updatePaymentStatus(
                    receiptNumber = receiptNumber,
                    status = PaymentStatus.ABORTED,
                    paymentMethod = PaymentMethod.CARD,
                )
                PaymentOutcome.Aborted(receiptNumber)
            }

            is PaymentResult.Failure -> {
                orderViewModel.setOrderStatus(
                    receiptNumber = receiptNumber,
                    status = OrderStatus.FAILED,
                    paymentMethod = PaymentMethod.CARD,
                )
                PaymentOutcome.Failure(receiptNumber, paymentResult.error)
            }
            null -> {
                orderViewModel.setOrderStatus(
                    receiptNumber = receiptNumber,
                    status = OrderStatus.FAILED,
                    paymentMethod = PaymentMethod.CARD,
                )
                PaymentOutcome.UnknownFailure(receiptNumber)
            }
        }
    }
}
sealed class PaymentOutcome {
    data class Success(val receiptNumber: String) : PaymentOutcome()
    data class Aborted(val receiptNumber: String) : PaymentOutcome()
    data class Failure(val receiptNumber: String, val error: PaymentError) : PaymentOutcome()
    data class UnknownFailure(val receiptNumber: String) : PaymentOutcome()
}