package com.example.test_design.events

data class PaymentUiFeedback(
    val message: String,
    val duration: Int,
    val navigateBack: Boolean
)
