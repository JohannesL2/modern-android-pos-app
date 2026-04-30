package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

PaymentService coordinates app payment flows around the SDK repository without
exposing SDK request or event types to the UI.

pay(amountMinorUnits):
    reject if another payment is already active
    with active payment session:
        amount = convert minor units to decimal currency amount
        attempt = repository.processSale(amount)
        return map attempt to app PaymentResult

paySplitPart(part, totalsGroupId):
    reject if another payment is already active
    with active payment session kept open:
        sdkPaymentKind = map app card type to SDK payment kind
        storedValueInfo = build stored-value metadata when needed
        attempt = repository.processSingleSplitPart(
            amount = part amount,
            totalsGroupId = totalsGroupId,
            paymentType = sdkPaymentKind,
            storedValueInfo = storedValueInfo
        )
        return map attempt to app PaymentResult

refundUnlinked(amountMinorUnits, originalMaskedPan, skipCardCheck):
    with active payment session:
        optionally acquire and validate original card
        attempt = repository.processRefund(amount, original card reference)
        return map attempt to app PaymentResult

voidPayment(appSpecificData):
    with active payment session:
        attempt = repository.voidPayment(appSpecificData)
        return map attempt to app PaymentResult

abortPayment():
    request repository to abort the active SDK transaction
    end the active payment session if needed
*/
