package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

Split-payment internals. SDK payment types and stored-value metadata are kept as
neutral placeholders in this anonymized version.

SplitPaymentResult:
    results
    isFullySuccessful
    failedIndex

SplitPaymentRequest:
    amount
    paymentKind
    storedValueMetadata
    storedValueAction
    reuseCard
    maxRetries

SplitPaymentPart:
    amountMinorUnits
    appCardType
    barcode
    reuseCard
*/
