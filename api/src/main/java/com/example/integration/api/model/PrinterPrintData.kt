package com.example.integration.api.model

import android.graphics.Bitmap

sealed class PrinterPrintBlock {
    data class Logo(val bitmap: Bitmap) : PrinterPrintBlock()
    data class Text(val text: String, val align: PrinterTextAlign = PrinterTextAlign.LEFT) : PrinterPrintBlock()
    data class Feed(val lines: Int) : PrinterPrintBlock()
    data class Cut(val type: PrinterCutType = PrinterCutType.FEED) : PrinterPrintBlock()
    data class Barcode(val data: String) : PrinterPrintBlock()
}

enum class PrinterTextAlign {
    LEFT, CENTER, RIGHT
}

enum class PrinterCutType {
    FEED
}

data class PrinterPrintData(
    val blocks: List<PrinterPrintBlock>
)
