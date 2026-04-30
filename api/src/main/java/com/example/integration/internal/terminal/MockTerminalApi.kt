package com.example.integration.internal.terminal

import android.app.Activity
import android.util.Log
import com.example.integration.api.TerminalApi
import com.example.integration.api.model.DeviceInfo
import com.example.integration.api.model.PrinterPrintData
import com.example.integration.api.model.PaymentResult
import com.example.integration.api.model.PrintContentType
import com.example.integration.api.model.PrintResult
import com.example.integration.api.model.ScanBehavior
import com.example.integration.api.model.TerminalConnectionConfig
import com.example.integration.api.model.TerminalInitResult
import com.example.integration.internal.payment.SplitPaymentPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

internal class MockTerminalApi : TerminalApi {
    companion object {
        private const val TAG = "TERMINAL_MODE"
    }

    private val _logs = MutableSharedFlow<String>(extraBufferCapacity = 20)
    override val logs: SharedFlow<String> = _logs.asSharedFlow()

    private val _scannedCode = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val scannedCode: Flow<String> = _scannedCode.asSharedFlow()

    private val _terminalReady = MutableStateFlow(true)
    override val terminalReady: StateFlow<Boolean> = _terminalReady

    private val _terminalConnected = MutableStateFlow(true)
    override val terminalConnected: StateFlow<Boolean> = _terminalConnected

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    override val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo

    override suspend fun startTerminal(config: TerminalConnectionConfig): TerminalInitResult {
        _terminalReady.value = true
        Log.d(TAG, "startTerminal: mock terminal ready config=$config")
        _logs.tryEmit("Emulated terminal started")
        return TerminalInitResult.Success
    }

    override suspend fun teardownTerminal(): Boolean {
        _terminalReady.value = false
        Log.d(TAG, "teardownTerminal: mock terminal stopped")
        _logs.tryEmit("Emulated terminal stopped")
        return true
    }

    override suspend fun pay(amountMinorUnits: Int): PaymentResult {
        Log.d(TAG, "pay: SUCCESS amountMinorUnits=$amountMinorUnits")
        _logs.tryEmit("Mock pay success: amountMinorUnits=$amountMinorUnits")
        return PaymentResult.Success(
            paymentInfo = null,
            appSpecificData = "MOCK-PAY-${UUID.randomUUID()}",
            brand = "VISA",
            maskedPan = "411111******1111"
        )
    }

    override suspend fun refundUnlinked(amountMinorUnits: Int,  originalMaskedPan: String?,skipCardCheck: Boolean): PaymentResult {
        Log.d(TAG, "refundUnlinked: SUCCESS amountMinorUnits=$amountMinorUnits")
        _logs.tryEmit("Mock unlinked refund success: amountMinorUnits=$amountMinorUnits")
        return PaymentResult.Success(
            paymentInfo = null,
            appSpecificData = "MOCK-REFUND-${UUID.randomUUID()}",
            brand = "VISA",
            maskedPan = "411111******1111"
        )
    }

    override suspend fun voidPayment(appSpecificData: String): PaymentResult {
        Log.d(TAG, "voidPayment: SUCCESS appSpecificData=$appSpecificData")
        _logs.tryEmit("Mock void success for appSpecificData=$appSpecificData")
        return PaymentResult.Success(
            paymentInfo = null,
            appSpecificData = "MOCK-VOID-${UUID.randomUUID()}",
            brand = "VISA",
            maskedPan = "411111******1111"
        )
    }

    override fun abortPayment() {
        Log.d(TAG, "abortPayment: called")
        _logs.tryEmit("Mock abortPayment called")
    }

    override fun initializeScanner() {
        Log.d(TAG, "initializeScanner: mock scanner initialized")
        _logs.tryEmit("Mock scanner initialized")
    }

    override fun startScanner(activity: Activity, behavior: ScanBehavior) {
        Log.d(TAG, "startScanner: behavior=$behavior")
        _logs.tryEmit("Mock scanner started with behavior=$behavior")
    }

    override suspend fun print(content: String, contentType: PrintContentType): PrintResult {
        Log.d(TAG, "print: SUCCESS contentType=$contentType")
        _logs.tryEmit("Mock print success: contentType=$contentType")
        return PrintResult.Success
    }

    override suspend fun paySplitPart(
        part: SplitPaymentPart,
        totalsGroupId: String
    ): PaymentResult {

        Log.d(TAG, "paySplitPart: SUCCESS part=$part group=$totalsGroupId")

        _logs.tryEmit("Mock split part success: amount=${part.amountMinorUnits}")

        return PaymentResult.Success(
            paymentInfo = null,
            appSpecificData = "MOCK-SPLIT-PART-${UUID.randomUUID()}",
            brand = "VISA",
            maskedPan = "411111******1111"
        )
    }

    override suspend fun initializeExternalPrinter(): PrintResult {
        Log.d(TAG, "initializeExternalPrinter: SUCCESS")
        _logs.tryEmit("Mock external printer initialized")
        return PrintResult.Success
    }

    override suspend fun printExternal(data: PrinterPrintData): PrintResult {
        Log.d(TAG, "printExternal: SUCCESS blocks=${data.blocks.size}")
        _logs.tryEmit("Mock External printer print success: blocks=${data.blocks.size}")
        return PrintResult.Success
    }
}