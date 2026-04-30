package com.example.integration.internal.payment

internal sealed class SessionEndResult {
    object Success : SessionEndResult()

    sealed class Failure(
        val reason: String,
        val errorMessage: String
    ) : SessionEndResult() {
        object EndExistingSessionFailed :
            Failure(
                "Failed to end existing session",
                "Misslyckades avsluta pågående session"
            )

        object EndSessionRejectedImmediately :
            Failure(
                "endSession rejected after POI reset",
                "Misslyckades avsluta pågåendesession"
            )

        object Timeout :
            Failure(
                "Timed out waiting for SESSION_STARTED",
                "Misslyckades avsluta pågående session"
            )
    }
}