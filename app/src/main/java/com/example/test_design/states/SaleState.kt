package com.example.test_design.states

import androidx.annotation.StringRes

sealed interface SaleState {

    object Idle : SaleState

    data class BuildingSale(
        val orderId: String,
        val items: List<String> = emptyList()
    ) : SaleState

    object ProcessingPayment : SaleState
    object ProcessingSplitPayment : SaleState
    object ProcessingSplitPart : SaleState
    object AllPaymentsDone : SaleState
    object PrintingReceipt : SaleState

    data class PaymentFailed(
        @StringRes val reasonRes: Int
    ) : SaleState

    data class Error(
        val message: String
    ) : SaleState

    data class PaymentSuccess(
        val receiptNumber: String,
        val totalAmount: Int,
        val appliedCode: String,
        val discountAmount: Int,
        val slipHtml: String? = null
    ) : SaleState
}