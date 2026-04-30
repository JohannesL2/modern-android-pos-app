# Refund

**Target audience:** consumers of the integration layer.

This page describes refund-specific behavior only. API signatures are defined in
[TerminalApi](../terminal-api.md), and result models in
[Error Handling](../error-handling.md).

## Unlinked refund

```kotlin
val result = ApiModule.terminal.refundUnlinked(
    amountMinorUnits = 1000,
    originalMaskedPan = originalMaskedPan,
    skipCardCheck = false,
)
```

Amounts are specified in minor units.

## Card verification

When `skipCardCheck` is `false`, the integration attempts to read the card and compare the BIN and last four digits against `originalMaskedPan`.

Possible refund-specific errors:

* `CardReadFailed`
* `WrongCard`

When `skipCardCheck` is `true`, the integration proceeds directly to the refund command.

## Responsibilities of the application layer

This is an unlinked refund. The application layer is responsible for determining whether a refund is allowed based on business rules and the stored original transaction.
