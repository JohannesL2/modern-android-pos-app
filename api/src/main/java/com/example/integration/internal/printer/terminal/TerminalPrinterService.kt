package com.example.integration.internal.printer.terminal

/*
PSEUDO-CODE ONLY

TerminalPrinterService sends print jobs to the integrated terminal printer
through the payment terminal SDK.

print(content, contentType):
    sdkContentType = map app contentType to SDK content type

    status = terminalSdk.transactionManager.print(
        payload = content,
        contentType = sdkContentType,
        receiptType = merchantReceipt,
        delivery = printer
    )

    return map status.code to:
        success
        outOfPaper
        overTemperature
        paperJam
        lowBattery
        unknown(status.code)
*/
