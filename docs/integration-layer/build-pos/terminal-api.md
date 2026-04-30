# TerminalApi

**Target audience:** consumers of the integration layer.

`TerminalApi` is the contract used by the application layer via `ApiModule.terminal`.

## Status

```kotlin
val logs: SharedFlow<String>
val scannedCode: Flow<String>
val terminalReady: StateFlow<Boolean>
val terminalConnected: StateFlow<Boolean>
val deviceInfo: StateFlow<DeviceInfo?>
```

The semantics of these flows are described in [State and Flows](state-and-flows.md).

## Lifecycle

```kotlin
suspend fun startTerminal(config: TerminalConnectionConfig): TerminalInitResult
suspend fun teardownTerminal(): Boolean
```

`startTerminal(...)` is owned by `ApiModule` in typical application code.
Startup order is described in [Quick Start](build-pos-app.md).

## Payments

```kotlin
suspend fun pay(amountMinorUnits: Int): PaymentResult
suspend fun refundUnlinked(
    amountMinorUnits: Int,
    originalMaskedPan: String?,
    skipCardCheck: Boolean = false
): PaymentResult
suspend fun voidPayment(appSpecificData: String): PaymentResult
suspend fun paySplitPart(part: SplitPaymentPart, totalsGroupId: String): PaymentResult
fun abortPayment()
```

Function-specific usage is described in:

* [Payments](features/payments.md)
* [Refund](features/refund.md)
* [Void](features/void.md)

Results and errors are described in [Error Handling](error-handling.md).

## Scanner

```kotlin
fun initializeScanner()
fun startScanner(activity: Activity, behavior: ScanBehavior)
```

Usage is described in [Scanner](features/scanner.md).

## Printing

```kotlin
suspend fun print(content: String, contentType: PrintContentType): PrintResult
suspend fun initializeEpsonPrinter(): PrintResult
suspend fun printEpson(data: EpsonPrintData): PrintResult
```

Usage is described in [Receipt Printing](features/receipt-printing.md).
