# Void

**Target audience:** consumers of the integration layer.

Void cancels a previous payment using the `appSpecificData` from the original transaction. API signatures are defined in
[TerminalApi](../terminal-api.md).

## Usage

```kotlin id="7gk0nq"
val result = ApiModule.terminal.voidPayment(appSpecificData)
```

Persist the value from a successful payment:

```kotlin id="m1x4ze"
if (result is PaymentResult.Success) {
    save(result.appSpecificData)
}
```

## Responsibilities of the application layer

The integration layer cannot void a payment without `appSpecificData`. The application layer must therefore persist this value together with the original transaction.

Results and errors are described in
[Error Handling](../error-handling.md).
