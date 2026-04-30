package com.example.integration.api.model

sealed class PaymentResult {
    data class Success(
        val paymentInfo: String?,
        val appSpecificData: String,
        val maskedPan: String?,
        val brand: String?
    ) : PaymentResult()

    class Failure(
        val paymentInfo: String?,
        val error: PaymentError
    ) : PaymentResult()

    object Aborted : PaymentResult()
}



sealed interface PaymentError {
    data object DeviceNotConnected : PaymentError
    data object DeviceBusy : PaymentError
    data object Timeout : PaymentError
    data object Aborted : PaymentError
    data object StartFailed : PaymentError
    data object SessionNotActive : PaymentError
    data object CardReadFailed : PaymentError
    data object WrongCard : PaymentError
    data object PaymentAlreadyInProgress : PaymentError
    data class Declined(val message: String?) : PaymentError
    data class SdkError(val message: String?) : PaymentError
    data class  Unknown(val message: String? = null) : PaymentError
}