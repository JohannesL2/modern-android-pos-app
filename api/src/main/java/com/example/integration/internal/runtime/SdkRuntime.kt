package com.example.integration.internal.runtime

/*
PSEUDO-CODE ONLY

SdkRuntime is the single process-wide owner of the payment terminal SDK.

state sdkHandle = null
state initializationResult = pending
state deviceInfo = null
state paymentEvents = shared stream
state statusEvents = shared stream
state transactionEvents = shared stream

initialize(config):
    if sdkHandle already exists:
        return

    sdkConfig = buildSdkConfiguration(
        connectionMode = config.mode,
        terminalAddress = config.address,
        appIdentity = configuredAppIdentity,
        environment = configuredEnvironment
    )

    sdkHandle = PaymentTerminalSdk.create(
        context = applicationContext,
        config = sdkConfig,
        listener = {
            on payment-completed event:
                paymentEvents.emit(event)

            on card-information event:
                paymentEvents.emit(event)

            on transaction event:
                transactionEvents.emit(event)

            on status event:
                statusEvents.emit(event)
        }
    )

    initializationResult = wait until SDK reports initialized or failed

awaitInitialized():
    return initializationResult

login():
    return sdkHandle.login(credentials = configuredCredentials).isSuccessful

logout():
    return sdkHandle.logout().isSuccessful

teardown():
    sdkHandle.release()
    sdkHandle = null
    return true

emitDeviceInformation():
    rawDeviceInfo = sdkHandle.getDeviceInformation()
    deviceInfo = map rawDeviceInfo to app DeviceInfo
*/
