# Example: Simple Payment

**Target audience:** consumers of the integration layer.

For payment rules, see [Payments](../features/payments.md). For result handling, see [Error Handling](../error-handling.md).

```kotlin
class PaymentViewModel : ViewModel() {
    private val terminal = ApiModule.terminal

    val terminalReady = terminal.terminalReady

    fun pay(amountMinorUnits: Int) {
        viewModelScope.launch {
            when (val result = terminal.pay(amountMinorUnits)) {
                is PaymentResult.Success -> {
                    saveAppSpecificData(result.appSpecificData)
                    showMessage("Payment approved")
                }

                is PaymentResult.Failure -> {
                    showMessage(formatPaymentError(result.error))
                }

                PaymentResult.Aborted -> {
                    showMessage("Payment was aborted")
                }
            }
        }
    }

    fun abort() {
        terminal.abortPayment()
    }
}
```

```kotlin
val ready by viewModel.terminalReady.collectAsState()

Button(
    enabled = ready,
    onClick = { viewModel.pay(1000) }
) {
    Text("Pay")
}
```