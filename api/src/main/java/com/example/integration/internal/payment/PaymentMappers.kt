package com.example.integration.internal.payment


import com.example.integration.api.model.PaymentError
import com.example.integration.api.model.PaymentResult

internal fun PaymentAttemptResult.toPaymentResult(): PaymentResult =
    when (this) {
        is PaymentAttemptResult.Success -> PaymentResult.Success(
            paymentInfo = receipt?.let { PaymentInfoTextExtractor.from(it) },
            appSpecificData = appSpecificData,
            maskedPan = maskedPan,
            brand = brand
        )

        is PaymentAttemptResult.Failure -> PaymentResult.Failure(
            paymentInfo = receipt?.let { PaymentInfoTextExtractor.from(it) },
            error = failure.toPaymentError()
        )

        PaymentAttemptResult.Aborted -> PaymentResult.Aborted
    }

private fun PaymentFailure.toPaymentError(): PaymentError =
    when (this) {
        PaymentFailure.DeviceBusy -> PaymentError.DeviceBusy
        PaymentFailure.DeviceNotConnected -> PaymentError.DeviceNotConnected
        PaymentFailure.Timeout -> PaymentError.Timeout
        PaymentFailure.Aborted -> PaymentError.Aborted
        PaymentFailure.StartFailed -> PaymentError.StartFailed
        PaymentFailure.SessionNotActive -> PaymentError.SessionNotActive
        PaymentFailure.Unknown -> PaymentError.Unknown()
        is PaymentFailure.Declined -> PaymentError.Declined(errorMessage)
    }