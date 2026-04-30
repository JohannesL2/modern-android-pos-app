package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

PaymentFailure is the vendor-neutral failure taxonomy used after mapping SDK
authorization and terminal statuses.

failure kinds:
    deviceBusy
    deviceNotConnected
    timeout
    aborted
    startFailed
    sessionNotActive
    declined(message, rawAuthorizationStatus)
    unknown
*/
