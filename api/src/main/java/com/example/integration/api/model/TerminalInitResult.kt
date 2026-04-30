package com.example.integration.api.model

sealed class TerminalInitResult {

    object Success : TerminalInitResult()

    data class Failure(
        val errorMessage: String
    ) : TerminalInitResult()
}
