package com.example.integration.internal.payment

/*
PSEUDO-CODE ONLY

PaymentInfoTextExtractor turns SDK receipt data into printable plain text.

from(receiptData):
    html = receiptData.asHtml()
    wantedSections = configured receipt section ids

    for each section id:
        tableHtml = find table in html by section id
        text = convert table rows and cells to plain text
        append non-empty text to bank slip

    return joined bank slip text, or null if empty
*/
