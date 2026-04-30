package com.example.integration.api

import android.content.Context
import android.util.Log
import com.example.integration.api.model.TerminalConnectionConfig
import com.example.integration.internal.runtime.RuntimeProvider
import com.example.integration.internal.terminal.TerminalConnectionManager
import com.example.integration.internal.terminal.MockTerminalApi
import com.example.integration.api.model.TerminalInitResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object ApiModule {
    private const val TAG = "TERMINAL_MODE"

    private val initialized = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
    private val useEmulatedTerminal = AtomicBoolean(false)
    private var terminalConnectionConfig: TerminalConnectionConfig = TerminalConnectionConfig.Persisted
    private lateinit var appContext: Context

    private lateinit var terminalIntegration: TerminalApi
    private lateinit var terminalConnectionManager: TerminalConnectionManager

    val terminal: TerminalApi
        get() {
            check(initialized.get()) { "ApiModule not initialized" }
            check(::terminalIntegration.isInitialized) { "ApiModule not started" }
            return terminalIntegration
        }

    fun initialize(context: Context) {
        if (initialized.get()) return
        synchronized(this) {
            if (initialized.get()) return
            if (!useEmulatedTerminal.get()) {
                appContext = context.applicationContext
                RuntimeProvider.init(appContext)
            }
            initialized.set(true)
        }
    }

    fun setUseEmulatedTerminal(enabled: Boolean) {
        check(!started.get()) { "ApiModule already started" }
        useEmulatedTerminal.set(enabled)
    }

    fun setTerminalConnectionConfig(config: TerminalConnectionConfig) {
        check(!started.get()) { "ApiModule already started" }
        terminalConnectionConfig = config
    }

    fun start(scope: CoroutineScope) {
        check(initialized.get()) { "ApiModule not initialized" }
        if (started.getAndSet(true)) return

        if (useEmulatedTerminal.get()) {
            Log.w(TAG, "start: using emulated terminal")
            terminalIntegration = MockTerminalApi()
            scope.launch {
                terminalIntegration.startTerminal(terminalConnectionConfig)
            }
            return
        }

        Log.w(TAG, "start: using physical terminal")
        if (!::terminalIntegration.isInitialized || !::terminalConnectionManager.isInitialized) {
            val bundle = TerminalApiFactory.create(appContext, scope)
            terminalIntegration = bundle.integration
            terminalConnectionManager = bundle.terminalConnectionManager
        }
        scope.launch {
            val result = terminalIntegration.startTerminal(terminalConnectionConfig)
            terminalConnectionManager.setConnected(result is TerminalInitResult.Success)
            terminalIntegration.initializeExternalPrinter()
        }
    }
}
