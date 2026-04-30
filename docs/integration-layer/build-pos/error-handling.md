# Error Handling

**Target audience:** consumers of the integration layer.

All public terminal operations return models from `api.model`. The application layer should handle these models rather than PSDK-specific status codes.

## TerminalInitResult

* `Success`
* `Failure(errorMessage)`

Normal startup is performed via `ApiModule.start(scope)`, see [Quick Start](build-pos-app.md).

## PaymentResult

* `Success(paymentInfo, appSpecificData, maskedPan, brand)`
* `Failure(paymentInfo, error)`
* `Aborted`

Persist `appSpecificData` from `Success` if void operations need to be supported. The void flow is described in [Void](features/void.md).

## PaymentError

* `DeviceNotConnected`
* `DeviceBusy`
* `Timeout`
* `Aborted`
* `StartFailed`
* `SessionNotActive`
* `CardReadFailed`
* `WrongCard`
* `PaymentAlreadyInProgress`
* `Declined(message)`
* `SdkError(message)`
* `Unknown(message)`

Function-specific error cases are described on their respective pages:

* [Payments](features/payments.md)
* [Refund](features/refund.md)
* [Void](features/void.md)

## PrintResult

* `Success`
* `Failure`

`Failure` contains:

* `errorMessage`: user-facing message.
* `reason`: technical message for logging.

Printing flows are described in [Receipt Printing](features/receipt-printing.md). 
