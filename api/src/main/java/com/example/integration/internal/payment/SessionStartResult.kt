package com.example.integration.internal.payment

internal sealed class SessionStartResult {
    object Success : SessionStartResult()

    sealed class Failure(
        val reason: String,
        val errorMessage: String
    ) : SessionStartResult() {
        object EndExistingSessionFailed :
            Failure(
                "Failed to end existing session",
                "Misslyckades avsluta pågående session"
            )

        object StartRejectedAfterReset :
            Failure(
                "startSession2 rejected after POI reset",
                "Misslyckades starta session"
            )

        object Timeout :
            Failure(
                "Timed out waiting for SESSION_STARTED",
                "Misslyckades starta session"
            )
    }
}