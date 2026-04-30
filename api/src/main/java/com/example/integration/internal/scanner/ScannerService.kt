package com.example.integration.internal.scanner

/*
PSEUDO-CODE ONLY

ScannerService adapts scanner events from the terminal SDK into app callbacks.

state onCodeScanned = no-op

setOnCodeScanned(callback):
    onCodeScanned = callback

initializeScanner():
    terminalSdk.scanner.configure(
        barcodeFormats = configuredFormats,
        behavior = configuredBehavior
    )
    terminalSdk.scanner.setListener { event ->
        if event contains scannedCode:
            onCodeScanned(event.scannedCode)
    }

startScanner(activity, behavior):
    sdkBehavior = map behavior to SDK scanner behavior
    terminalSdk.scanner.start(activity, sdkBehavior)
*/
