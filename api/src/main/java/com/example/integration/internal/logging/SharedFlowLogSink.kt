package com.example.integration.internal.logging

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class SharedFlowLogSink : LogSink {

    private val _logs = MutableSharedFlow<String>(replay = 20)
    val logs = _logs.asSharedFlow()

    override fun log(message: String) {
        _logs.tryEmit(message)
    }
}