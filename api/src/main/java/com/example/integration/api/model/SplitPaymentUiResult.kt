package com.example.integration.api.model

data class SplitPaymentUiResult(
    val results: List<PaymentResult>,
    val isFullySuccessful: Boolean,
    val failedIndex: Int? = null
)

