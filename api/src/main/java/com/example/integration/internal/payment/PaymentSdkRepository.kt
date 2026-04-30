package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

PaymentSdkRepository is the only payment component that talks directly to the
payment terminal SDK. It converts app-level payment requests into SDK requests
and converts SDK events/statuses back into app-level result objects.

startSession():
    request terminalSdk.transactionManager.startSession()
    wait until transaction manager reports session active
    return session started or failure

endSession():
    request terminalSdk.transactionManager.endSession()
    wait until transaction manager reports idle

processSale(amount):
    paymentRequest = build SDK payment request:
        amount = amount
        type = card purchase
        appSpecificData = generated transaction id

    terminalSdk.transactionManager.startPayment(paymentRequest)
    event = wait for payment-completed, timeout, or abort

    if event means approved:
        return PaymentAttemptResult.Success(
            receipt = event.receipt,
            appSpecificData = paymentRequest.appSpecificData,
            maskedPan = event.maskedPan,
            brand = event.cardBrand
        )

    if event means declined:
        return PaymentAttemptResult.Failure(
            receipt = event.receipt,
            failure = declined(event.reason)
        )

    if event means aborted:
        return PaymentAttemptResult.Aborted

    return PaymentAttemptResult.Failure(receipt = null, failure = unknown)

processRefund(amount, originalCard):
    build SDK refund request
    submit request
    wait for completion event
    map completion event to PaymentAttemptResult

voidPayment(appSpecificData):
    build SDK void request for original appSpecificData
    submit request
    wait for completion event
    map completion event to PaymentAttemptResult

acquireCard():
    start SDK card-acquisition request
    wait for card-acquired event
    return AcquiredCard(reference, bin, last4, brand)

processSingleSplitPart(amount, totalsGroupId, paymentType, storedValueInfo):
    build SDK split-payment request:
        amount = amount
        group id = totalsGroupId
        payment type = paymentType
        optional stored-value metadata = storedValueInfo

    submit request and map the completion event to PaymentAttemptResult

buildStoredValueCardInfo(barcode, pin, providerName):
    create SDK stored-value-card object
    attach barcode, pin, provider, and default card type
    return object

abortPayment():
    terminalSdk.transactionManager.abortCurrentTransaction()
*/
