package com.example.test_design.utils

import android.graphics.Bitmap
import com.example.test_design.domain.models.Order
import com.example.integration.api.model.PrinterTextAlign
import com.example.integration.api.model.PrinterPrintBlock
import com.example.integration.api.model.PrinterPrintData

class ReceiptPrintMapper {
    fun map(
        order: Order,
        logo: Bitmap?,
        bankSlip: String?,
        appliedCode: String = "",
        discountAmount: Int = 0
    ): PrinterPrintData {
        val blocks = mutableListOf<PrinterPrintBlock>()
        val subtotal = order.items.sumOf { it.totalPrice }
        val totalAmount = (subtotal - discountAmount).coerceAtLeast(0)
        val vatAmount = totalAmount * 0.25 / 1.25
        val greeting = ReceiptFormatter.getReceiptGreeting(order.time)

        logo?.let {
            blocks += PrinterPrintBlock.Logo(it)
            blocks += PrinterPrintBlock.Feed(1)
        }

        blocks += PrinterPrintBlock.Text("KVITTO", PrinterTextAlign.CENTER)
        blocks += PrinterPrintBlock.Text("Cafe Kvarnen", PrinterTextAlign.CENTER)
        blocks += PrinterPrintBlock.Text("Hornstulls Strand 5", PrinterTextAlign.CENTER)
        blocks += PrinterPrintBlock.Text(separator())
        blocks += PrinterPrintBlock.Text("Datum: ${order.date}")
        blocks += PrinterPrintBlock.Text("Tid: ${order.time}")
        blocks += PrinterPrintBlock.Text("Kvittonr: #${order.receiptNumber}")
        blocks += PrinterPrintBlock.Text("Säljare: ${sanitizeForReceiptPrinter(order.seller)}")
        blocks += PrinterPrintBlock.Text(separator())

        order.items.forEach { item ->
            blocks += PrinterPrintBlock.Text(
                sanitizeForReceiptPrinter("${item.product.name} x${item.quantity}")
            )

            val variantLines = buildVariantLines(item.product.variantValue)
            variantLines.forEach { blocks += PrinterPrintBlock.Text(it) }

            blocks += PrinterPrintBlock.Text(
                alignAmount(
                    label = "Belopp",
                    amount = "${formatAmount(item.totalPrice)} kr"
                )
            )
        }

        if (appliedCode.isNotBlank() && discountAmount > 0) {
            blocks += PrinterPrintBlock.Text(
                alignAmount(
                    label = "Rabatt $appliedCode",
                    amount = "-${formatAmount(discountAmount)} kr"
                )
            )
        }

        blocks += PrinterPrintBlock.Text(separator())
        blocks += PrinterPrintBlock.Text(
            alignAmount(
                label = "Moms (25%)",
                amount = "${formatAmount(vatAmount)} kr"
            )
        )
        blocks += PrinterPrintBlock.Text(
            alignAmount(
                label = "TOTALT",
                amount = "${formatAmount(totalAmount)} kr"
            )
        )
        blocks += PrinterPrintBlock.Feed(1)
        blocks += PrinterPrintBlock.Text("Betalsätt: ${sanitizeForReceiptPrinter(order.paymentMethod.displayName)}")

        val normalizedBankSlip = ReceiptFormatter.cleanHtmlToPlainText(bankSlip)
        if (normalizedBankSlip.isNotBlank()) {
            blocks += PrinterPrintBlock.Feed(1)
            blocks += PrinterPrintBlock.Text("KORTINFORMATION")
            normalizedBankSlip.lineSequence()
                .filter { it.isNotBlank() }
                .forEach { blocks += PrinterPrintBlock.Text(sanitizeForReceiptPrinter(it)) }
        }

        blocks += PrinterPrintBlock.Feed(1)
        blocks += PrinterPrintBlock.Text(sanitizeForReceiptPrinter("$greeting!"), PrinterTextAlign.CENTER)
        blocks += PrinterPrintBlock.Text("Tack for besöket.", PrinterTextAlign.CENTER)
        blocks += PrinterPrintBlock.Feed(2)
        blocks += PrinterPrintBlock.Barcode(order.receiptNumber)
        blocks += PrinterPrintBlock.Cut()

        return PrinterPrintData(blocks)
    }

    private fun buildVariantLines(variantValue: String?): List<String> {
        return variantValue
            ?.takeIf { it.isNotBlank() && !it.equals("STANDARD", ignoreCase = true) }
            ?.let { listOf(sanitizeForReceiptPrinter("Storlek: $it")) }
            ?: emptyList()
    }

    private fun alignAmount(label: String, amount: String, lineWidth: Int = 42): String {
        val safeLabel = sanitizeForReceiptPrinter(label)
        val safeAmount = sanitizeForReceiptPrinter(amount)
        val spaces = (lineWidth - safeLabel.length - safeAmount.length).coerceAtLeast(1)
        return (safeLabel + " ".repeat(spaces) + safeAmount).take(lineWidth)
    }

    private fun formatAmount(amount: Int): String = "%.2f".format(amount.toDouble())

    private fun formatAmount(amount: Double): String = "%.2f".format(amount)

    private fun separator(width: Int = 42): String = "-".repeat(width)

    private fun sanitizeForReceiptPrinter(text: String): String {
        return text
            .replace('\u00A0', ' ')                 // NBSP → space
            .replace(Regex("[\\x00-\\x1F]"), "")    // control chars bort
            .replace(Regex("[^\\x20-\\x7EåäöÅÄÖ]"), "") // tillåt basic + svenska
            .take(48) // skydda mot för långa rader (justera efter printer)
    }
}