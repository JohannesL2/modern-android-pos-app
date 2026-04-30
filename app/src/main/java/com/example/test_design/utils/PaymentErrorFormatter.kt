package com.example.test_design.utils

import android.content.Context
import com.example.test_design.R
import com.example.integration.api.model.PaymentError

fun PaymentError.toDisplayMessage(context: Context): String =
    when (this) {
        PaymentError.DeviceNotConnected -> context.getString(R.string.payment_error_device_not_connected)
        PaymentError.DeviceBusy -> context.getString(R.string.payment_error_device_busy)
        PaymentError.Timeout -> context.getString(R.string.payment_error_timeout)
        PaymentError.Aborted -> context.getString(R.string.payment_error_aborted)
        PaymentError.StartFailed -> context.getString(R.string.payment_error_start_failed)
        PaymentError.SessionNotActive -> context.getString(R.string.payment_error_session_not_active)
        PaymentError.CardReadFailed -> context.getString(R.string.payment_error_card_read_failed)
        PaymentError.WrongCard -> context.getString(R.string.payment_error_wrong_card)
        PaymentError.PaymentAlreadyInProgress -> context.getString(R.string.payment_error_already_in_progress)
        is PaymentError.Declined -> context.getString(R.string.payment_error_declined)
        is PaymentError.SdkError -> context.getString(R.string.payment_error_terminal_unknown)
        is PaymentError.Unknown -> context.getString(R.string.payment_error_unknown)
    }