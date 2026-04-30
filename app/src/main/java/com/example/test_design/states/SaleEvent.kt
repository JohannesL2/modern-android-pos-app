package com.example.test_design.states

sealed interface SaleEvent {

    // Cart / building
    data class AddItem(val item: String) : SaleEvent
    data class RemoveItem(val item: String) : SaleEvent

    // Checkout flow
    object StartCheckout : SaleEvent
    object PaymentApproved : SaleEvent
    data class PaymentDeclined(val reason: String) : SaleEvent

    // Receipt / reset
    object ReceiptHandled : SaleEvent
}
