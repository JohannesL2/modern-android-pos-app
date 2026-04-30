# Payments

**Target audience:** consumers of the integration layer.

Use this page for payment-specific behavior. Method signatures are defined in
[TerminalApi](../terminal-api.md), and result models in
[Error Handling](../error-handling.md).

## Standard payment

```kotlin
val result = ApiModule.terminal.pay(amountMinorUnits = 1000)
```

Amounts are specified in minor units.
For SEK, 1 krona = 100 öre.
Example: 10.50 SEK → 1050.

## Concurrency

The integration layer supports one payment at a time. If a payment is already in progress, `PaymentError.PaymentAlreadyInProgress` is returned.

## Abort

```kotlin
ApiModule.terminal.abortPayment()
```

Abort sends a cancellation request to the terminal. The UI should wait for the result of the ongoing payment call.

## Split payment (partial payment)

```kotlin
val result = ApiModule.terminal.paySplitPart(part, totalsGroupId)
```

`part.paymentType` determines whether the part is processed as a card or gift card payment.

## Example

See [Example: Basic payment](../examples/basic-payment.md).
