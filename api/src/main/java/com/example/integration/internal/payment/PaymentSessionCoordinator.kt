package com.example.integration.internal.payment

import com.example.integration.internal.logging.LogSink
import com.example.integration.internal.terminal.TerminalConnectionManager

internal class PaymentSessionCoordinator(
    private val repo: PaymentSdkRepository,
    private val logSink: LogSink,
    private val terminalConnectionManager: TerminalConnectionManager
) {

    suspend fun <T> withSession(
        source: String,
        autoClose: Boolean = true,
        onSessionStartFailure: suspend (SessionStartResult) -> T,
        block: suspend () -> T
    ): T {
        val session = repo.startSession()

        if (session !is SessionStartResult.Success) {
            logSink.log("startSession failed: (${session::class.simpleName} source:$source)")
            terminalConnectionManager.setConnected(false)
            return onSessionStartFailure(session)
        }

        return try {
            block()
        } finally {
            if (autoClose) {
                val endResult = repo.endSession()
                if (endResult !is SessionEndResult.Success) {
                    logSink.log("endSession failed: (${endResult::class.simpleName} source:$source)")
                }
            }
        }
    }
}