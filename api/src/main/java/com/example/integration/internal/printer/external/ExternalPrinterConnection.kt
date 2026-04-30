package com.example.integration.internal.printer.external

/*
PSEUDO-CODE ONLY

ExternalPrinterConnection owns the lifecycle of the external receipt printer.

state printerHandle = null

connect():
    if printerHandle already exists:
        return success

    try:
        printerHandle = ExternalPrinterSdk.createPrinter(model = configuredModel)
        printerHandle.onReceive = { event ->
            log("External printer finished: " + event.statusCode)
        }
        printerHandle.connect(target = configuredConnectionTarget)
        return success
    catch sdkError:
        printerHandle = null
        return printerFailure(code = sdkError.statusCode)
    catch anyError:
        printerHandle = null
        return printerFailure(code = null)

getPrinter():
    if printerHandle is null:
        throw "Printer not connected"
    return printerHandle

disconnect():
    clear pending printer commands
    unregister printer callbacks
    close printer connection
    printerHandle = null
*/
