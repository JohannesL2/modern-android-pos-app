package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

PaymentAttemptResult is the repository-level result from a terminal SDK payment
attempt. SDK receipt/event objects are represented here as neutral placeholders.

Success:
    receiptData
    appSpecificData
    maskedPan
    cardBrand

Failure:
    receiptData
    paymentFailure

Aborted:
    no additional data
*/
