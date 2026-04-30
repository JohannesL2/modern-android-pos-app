package com.example.integration.api.model

sealed class PrintResult {
    object Success : PrintResult()

    sealed class Failure(
        val reason: String,
        val errorMessage: String
    ) : PrintResult() {
        object OutOfPaper : Failure("Printer is out of paper", "Slut på kvitto papper")
        object OverTemperature : Failure("Printer is overheated", "Skrivare är överhettad")
        object PaperJam : Failure("Printer paper jam", "Papperstopp i skrivare")
        object LowBattery : Failure("Printer battery is low", "Låg batteri nivå")
        data class Unknown(val code: Int?) : Failure("Unknown printer error (code=$code)", "Okänt fel")
    }
}