package com.example.integration.internal.terminal

import android.app.Activity
import com.example.integration.api.TerminalApi
import com.example.integration.api.model.DeviceInfo
import com.example.integration.api.model.PrinterPrintData
import com.example.integration.api.model.PaymentError
import com.example.integration.internal.logging.LogSink
import com.example.integration.internal.logging.SharedFlowLogSink
import com.example.integration.api.model.TerminalInitResult
import com.example.integration.api.model.PaymentResult
import com.example.integration.internal.payment.PaymentService
import com.example.integration.api.model.PrintContentType
import com.example.integration.api.model.PrintResult
import com.example.integration.internal.printer.external.ExternalPrinterService
import com.example.integration.internal.printer.terminal.TerminalPrinterService
import com.example.integration.internal.runtime.SdkRuntime
import com.example.integration.api.model.ScanBehavior
import com.example.integration.api.model.TerminalConnectionConfig
import com.example.integration.internal.payment.SplitPaymentPart
import com.example.integration.internal.scanner.ScannerService
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class TerminalApiImpl (
    private val paymentService: PaymentService,
    private val scannerService: ScannerService,
    private val printerService: TerminalPrinterService,
    private val externalPrinterService: ExternalPrinterService,
    private val runtime: SdkRuntime,
    private val terminalConnectionManager: TerminalConnectionManager,
    private val logSink: LogSink
) : TerminalApi {
    override val logs: SharedFlow<String> =
        (logSink as SharedFlowLogSink).logs

    private val _scannedCode = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val deviceInfo: StateFlow<DeviceInfo?> = runtime.deviceInfoState
    override val scannedCode: SharedFlow<String> = _scannedCode.asSharedFlow()
    override val terminalReady = terminalConnectionManager.terminalReady
    override val terminalConnected = terminalConnectionManager.isConnected

    override suspend fun startTerminal(config: TerminalConnectionConfig): TerminalInitResult {
        logSink.log("Initierar SDK...")
        terminalConnectionManager.setConnectionConfig(config)
        return try {
            runtime.initialize(config)

            val initResult = runtime.awaitInitialized()
            if (initResult.isFailure) {
                terminalConnectionManager.setConnected(false)
                TerminalInitResult.Failure(
                    "SDK-initiering misslyckades: ${initResult.exceptionOrNull()?.message}"
                )
            } else if (!runtime.login()) {
                TerminalInitResult.Failure("Login misslyckades")

            } else {
                runtime.emitDeviceInformation()
                TerminalInitResult.Success
            }

        } catch (e: Exception) {
            TerminalInitResult.Failure("Oväntat fel: ${e.message}")
        }
    }


    override suspend fun teardownTerminal(): Boolean {
        logSink.log("Startar logout från terminalen...")

        try {
            val logoutSuccess = runtime.logout()
            if (!logoutSuccess) {
                logSink.log("Logout misslyckades enligt SDK")
                return false
            }
            logSink.log("Logout lyckades")

            logSink.log("Startar SDK teardown...")
            val teardownSuccess = runtime.teardown()
            if (!teardownSuccess) {
                logSink.log("SDK teardown misslyckades enligt SDK")
                return false
            }
            logSink.log("SDK teardown lyckades")
            terminalConnectionManager.setConnected(false)

            return true

        } catch (e: Exception) {
            logSink.log("Oväntat fel vid logout/teardown: ${e.message}")
            return false
        }
    }

    override suspend fun pay(amountMinorUnits: Int): PaymentResult =
        paymentService.pay(amountMinorUnits)

    override suspend fun refundUnlinked(amountMinorUnits: Int, originalMaskedPan: String?, skipCardCheck: Boolean ): PaymentResult =
        paymentService.refundUnlinked(amountMinorUnits, originalMaskedPan, skipCardCheck)

    override suspend fun voidPayment(appSpecificData: String): PaymentResult {
        logSink.log("Startar void för appSpecificData=$appSpecificData")
        return try {
            // Om paymentService har voidPayment, använd den direkt
            paymentService.voidPayment(appSpecificData)
        } catch (e: Exception) {
            logSink.log("Void misslyckades: ${e.message}")
            PaymentResult.Failure(
                paymentInfo = null,
                error = PaymentError.SdkError(e.message)
            )
        }
    }
    override fun abortPayment() =
        paymentService.abortPayment()

    override fun initializeScanner() {
        scannerService.setOnCodeScanned { code ->
            _scannedCode.tryEmit(code)
        }
        scannerService.initializeScanner()
    }

    override fun startScanner(
        activity: Activity,
        behavior: ScanBehavior
    ) {
        scannerService.startScanner(activity, behavior)
    }

    override suspend fun paySplitPart(
        part: SplitPaymentPart,
        totalsGroupId: String
    ): PaymentResult =
        paymentService.paySplitPart(part, totalsGroupId)

    override suspend fun print(
        content: String,
        contentType: PrintContentType
    ): PrintResult =
        printerService.print(content, contentType)

    override suspend fun initializeExternalPrinter(): PrintResult =
        externalPrinterService.initialize()

    override suspend fun printExternal(data: PrinterPrintData): PrintResult =
        externalPrinterService.print(data)

}
