package com.example.integration.api

import android.content.Context
import com.example.integration.internal.logging.SharedFlowLogSink
import com.example.integration.internal.payment.PaymentSdkRepository
import com.example.integration.internal.payment.PaymentService
import com.example.integration.internal.payment.PaymentSessionCoordinator
import com.example.integration.internal.printer.external.ExternalPrinterConnection
import com.example.integration.internal.printer.external.ExternalPrinterService
import com.example.integration.internal.printer.terminal.TerminalPrinterService
import com.example.integration.internal.runtime.RuntimeProvider
import com.example.integration.internal.scanner.ScannerService
import com.example.integration.internal.terminal.TerminalApiImpl
import com.example.integration.internal.terminal.TerminalConnectionManager
import kotlinx.coroutines.CoroutineScope

internal object TerminalApiFactory {
    data class Bundle(
        val integration: TerminalApi,
        val terminalConnectionManager: TerminalConnectionManager
    )

    fun create(context: Context, scope: CoroutineScope): Bundle {
        val runtime = RuntimeProvider.get()
        val logSink = SharedFlowLogSink()

        val terminalConnectionManager = TerminalConnectionManager(runtime, scope)
        val paymentRepo = PaymentSdkRepository(runtime)
        val paymentSessionCoordinator = PaymentSessionCoordinator(paymentRepo, logSink, terminalConnectionManager)
        val paymentService = PaymentService(paymentRepo, logSink, terminalConnectionManager, paymentSessionCoordinator)

        // External printer for off-device. One connection instance is kept for the API lifetime.
        val externalPrinterConnection = ExternalPrinterConnection(context.applicationContext)
        val externalPrinterService = ExternalPrinterService(externalPrinterConnection)
        // Integrated terminal printer for on-device.
        val printerService = TerminalPrinterService(runtime)

        val scannerService = ScannerService(runtime.sdk)

        val integration = TerminalApiImpl(
            paymentService = paymentService,
            printerService = printerService,
            externalPrinterService = externalPrinterService,
            scannerService = scannerService,
            runtime = runtime,
            terminalConnectionManager = terminalConnectionManager,
            logSink = logSink
        )
        return Bundle(
            integration = integration,
            terminalConnectionManager = terminalConnectionManager
        )
    }
}
