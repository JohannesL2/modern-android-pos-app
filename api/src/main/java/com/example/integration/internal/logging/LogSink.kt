package com.example.integration.internal.logging

internal interface LogSink {
    fun log(message: String)
}