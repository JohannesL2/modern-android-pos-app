package com.example.integration.api

import android.app.Activity
import com.example.integration.api.model.DeviceInfo
import com.example.integration.api.model.PrinterPrintData
import com.example.integration.api.model.TerminalInitResult
import com.example.integration.api.model.PaymentResult
import com.example.integration.api.model.PrintContentType
import com.example.integration.api.model.PrintResult
import com.example.integration.api.model.ScanBehavior
import com.example.integration.api.model.TerminalConnectionConfig
import com.example.integration.internal.payment.SplitPaymentPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface TerminalApi {

    val logs: SharedFlow<String>
    val scannedCode: Flow<String>
    val terminalReady: StateFlow<Boolean>
    val terminalConnected: StateFlow<Boolean>
    val deviceInfo: StateFlow<DeviceInfo?>

    suspend fun startTerminal(
        config: TerminalConnectionConfig = TerminalConnectionConfig.Persisted
    ): TerminalInitResult
    suspend fun teardownTerminal(): Boolean

    suspend fun pay(amountMinorUnits: Int): PaymentResult

    suspend fun refundUnlinked(amountMinorUnits: Int, originalMaskedPan: String?,  skipCardCheck: Boolean = false): PaymentResult

    suspend fun voidPayment(appSpecificData: String): PaymentResult

    suspend fun paySplitPart(
        part: SplitPaymentPart,
        totalsGroupId: String
    ): PaymentResult

    fun abortPayment()

    fun initializeScanner()


    fun startScanner(activity: Activity, behavior: ScanBehavior)


    suspend fun print(content: String, contentType: PrintContentType): PrintResult

    suspend fun initializeExternalPrinter(): PrintResult

    suspend fun printExternal(data: PrinterPrintData): PrintResult
}
