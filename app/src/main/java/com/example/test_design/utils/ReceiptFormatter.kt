package com.example.test_design.utils

import com.example.test_design.data.entity.OrderRow
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.example.test_design.R
import com.example.test_design.domain.models.Order
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.graphics.Canvas
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * ReceiptFormatter is an object with all receipt related functions and html
 */
object ReceiptFormatter {
    private fun mergeStyleTag(tag: String, inlineStyle: String): String {
        val styleRegex = Regex("""\sstyle\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
        val existingStyle = styleRegex.find(tag)?.groupValues?.get(1)?.trim().orEmpty()
        val mergedStyle =
            listOf(existingStyle, inlineStyle).filter { it.isNotBlank() }.joinToString("; ")

        val tagWithoutStyle = tag.replace(styleRegex, "")
        return tagWithoutStyle.replace(">", " style=\"$mergedStyle\">")
    }

    private fun normalizeSlipTableHtml(value: String): String {
        val normalizedTable = value.replace(
            Regex("""<table\b[^>]*>""", RegexOption.IGNORE_CASE)
        ) { match ->
            mergeStyleTag(
                match.value,
                "width:100%; border-collapse:collapse; table-layout:fixed;"
            )
        }

        return normalizedTable.replace(
            Regex("""<td\b([^>]*)>""", RegexOption.IGNORE_CASE)
        ) { match ->
            val attrs = match.groupValues[1]
            val className = Regex("""class\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
                .find(attrs)
                ?.groupValues
                ?.get(1)
                .orEmpty()
                .lowercase(Locale.ROOT)

            val isRight = className.contains("right")
            val isLeft = className.contains("left")
            val isCenter = className.contains("center")
            val isSubtitle = className.contains("subtitle")
            val hasColspan =
                Regex("""\bcolspan\s*=""", RegexOption.IGNORE_CASE).containsMatchIn(attrs)

            val alignment = when {
                isSubtitle || isCenter -> "center"
                isLeft || hasColspan -> "left"
                isRight -> "right"
                else -> "left"
            }

            val fontSize = if (isSubtitle) "14px" else "12px"
            val fontWeight = if (isSubtitle) "font-weight:bold; " else ""

            mergeStyleTag(
                match.value,
                "font-family:monospace; font-size:$fontSize; ${fontWeight}padding:0; vertical-align:top; white-space:nowrap; text-align:$alignment;"
            )
        }
    }

    private fun bankSlipToHtml(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val trimmed = value.trimStart()

        return if (trimmed.startsWith("<table", ignoreCase = true)) {
            // Printer HTML engine ignores stylesheet classes; inline styles must be on each element.
            normalizeSlipTableHtml(value)
        } else {
            // Fallback for plain-text slips.
            value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br/>")
        }
    }

    fun drawableToBase64(
        context: Context,
        drawableResId: Int
    ): String { //Convert image to Base64 string and compress it into a bitmap
        val drawable = context.getDrawable(drawableResId) ?: return ""
        val bitmap = (drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val bytes = baos.toByteArray()
        return "data:image/png;base64," + android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.NO_WRAP
        )
    }

    fun getReceiptGreeting(timeStr: String): String { //Give different greetings depending on what time the receipt is printed
        val hour = timeStr.substringBefore(":").toIntOrNull() ?: 0
        return when (hour) {
            in 5..10 -> "God morgon" //5 AM to 10 AM
            in 11..12 -> "God förmiddag" //11 AM to 12 PM
            in 13..17 -> "God eftermiddag" //13 PM to 17 PM
            in 18..22 -> "God kväll" //18 PM to 22 PM
            else -> "Ha en trevlig dag"
        }
    }

    private fun buildProductRowHtml(
        name: String,
        quantity: Int,
        price: Double,
        variantValue: String?
    ): String {

        val variantHtml = variantValue
            ?.takeIf { it.isNotBlank() && it.uppercase() != "STANDARD" }
            ?.let {
                """
            <div style="font-size:14px; font-weight:600; color:#333; margin-top: 2px;">
                Storlek: <span style="font-size:14px;">$it</span>
            </div>
            """.trimIndent()
            } ?: ""

        return """
        <table style="width:100%; margin-bottom:6px; font-size:16px; font-family:monospace;">
            <tr>
                <td style="vertical-align:top;">
                <div style="font-weight:bold;">
                    $name x$quantity
                </div>
                $variantHtml
                </td>
                <td style="text-align:right;">
                    ${"%.2f".format(price)} kr
                </td>
            </tr>
        </table>
    """.trimIndent()
    }

    /**
     * Generates a receipt after successful payment using information from the order in the database
     */
    fun generateReceiptHtml(
        context: Context,
        order: Order,
        slipHtml: String? = "",
        appliedCode: String = "",
        discountAmount: Int = 0,
        showWatermark: Boolean = false
    ): String {
        val logoBase64 = drawableToBase64(context, R.drawable.receiptcoffeelogo)
        val barcodeBitmap = generateReceiptBarcodeBitmap(order.receiptNumber, 300, 120)
        val barcodeBase64 = bitmapToBase64(barcodeBitmap)

        val slipSection = bankSlipToHtml(slipHtml)
        val greeting = getReceiptGreeting(order.time)

        val productsHtml = order.items.joinToString("") { item ->

            buildProductRowHtml(
                name = item.product.name,
                quantity = item.quantity,
                price = (item.pricePerItem * item.quantity).toDouble(),
                variantValue = item.product.variantValue
            )
        }

        val discountHtml =
            if (appliedCode.isNotEmpty() && discountAmount > 0) { //HTML for discount, only works when discount code is not empty
                """
            <table style="width:100%; margin-bottom:4px; font-size:16px;">
                <tr>
                    <td>Rabatt $appliedCode</td>
                    <td style="text-align:right;">-${"%.2f".format(discountAmount.toDouble())} kr</td>
                </tr>
            </table>
            """.trimIndent()
            } else ""

        val watermarkHtml = if (showWatermark) {
            """
        <div style="
            position: absolute;
            top: 0; left: 0; width: 100%; height: 100%;
            background-image: url('$logoBase64');
            background-repeat: repeat-y;
            background-position: center;
            background-size: 80%;
            opacity: 0.12; /* Väldigt blekt */
            z-index: -1;
        "></div>
        """.trimIndent()
        } else ""

        val subtotal = order.items.sumOf { item ->
            val modifiers = item.variantSelections.sumOf { it.priceModifier }
            (item.product.price + modifiers) * item.quantity
        }.toDouble()//Calculate prices multiplied by their quantity
        val totalAmount = subtotal - discountAmount //Subtract discount from the total price
        val vatAmount = totalAmount * 0.25 / 1.25 //Calculate vat
        //Complete HTML for successful payment receipts using logoBase64, barcodeBase64, productsHtml, discountHtml and slipSection
        return """
            <html>
            <body style="margin:0; padding:10px; background:#fff; font-family:sans-serif;">
                $watermarkHtml
                <div style="width: 310px; margin: 0 auto; color: #000;">
                    <div style="text-align:center;">
                        <img src="$logoBase64" style="width:120px; margin-bottom:10px;" />
                        <h2 style="margin:0; font-size:22px;">KVITTO</h2>
                        <p style="font-size:14px; margin: 4px 0;">Café Kvarnen<br/>Hornstulls Strand 5</p>
                    </div>

                    <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>

                    <div style="font-size:14px; font-family:monospace;">
                        Datum: ${order.date}<br/>
                        Tid: ${order.time}<br/>
                        Kvittonr: #${order.receiptNumber}<br/>
                        Säljare: ${order.seller}
                    </div>

                    <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>

                    <div style="margin: 10px 0;">
                        $productsHtml
                        $discountHtml
                    </div>

                    <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>

                    <table style="width:100%; font-family:monospace;">
                        <tr>
                            <td style="font-size:16px;">Moms (25%)</td>
                            <td style="text-align:right; font-size:16px;">${"%.2f".format(vatAmount)} kr</td>
                        </tr>
                        <tr style="font-size:22px; font-weight:bold;">
                            <td>TOTALT</td>
                            <td style="text-align:right;">${"%.2f".format(totalAmount)} kr</td>
                        </tr>
                    </table>

                    <div style="margin-top:15px; font-size:14px; font-family:monospace;">
                        Betalsätt: ${order.paymentMethod.displayName}
                    </div>

                    <div class="bank-slip" style="
    margin-top:10px;
    border-top:1px dashed #000;
    padding-top:8px;
    font-size:12px;
">$slipSection</div>

                    <div style="text-align:center; margin-top:20px;">
                        <p style="font-size:16px; margin-bottom:10px;">$greeting!<br/>Tack för besöket.</p>
                        <img src="$barcodeBase64" style="width:250px; height:auto;" />
                        <div style="font-size:12px; margin-top:4px;">${order.receiptNumber}</div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Generates a specific receipt just for refunds
     * The receipt is a lot smaller than the normal payment receipt
     * The text under the barcode now contains the letter R before the receiptnumber
     * This could be used to identify that this is a refund receipt and not receipt from a payment
     */
    fun generateRefundHtml(
        refundRows: List<OrderRow>,
        originalReceiptNumber: String,
        receiptNumber: String,
        logoBase64: String,
        barcodeBase64: String,
        paymentInfo: String?,
    ): String {
        val now = Date() //Initializes a date object to get the current time
        val dateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now) //Year, month, day
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) //Hours, minutes

        val totalAmount = refundRows.sumOf { it.lineAmount }
            .toDouble() //Price of chosen items from their rows in the database
        val vatAmount = totalAmount * 0.25 / 1.25 //Calculate vat

        val productsHtml = refundRows.joinToString("") { item ->

            Log.d("REFUND_DEBUG", "Variant: ${item.variantValue}")

            buildProductRowHtml(
                name = item.productName,
                quantity = item.quantity,
                price = item.lineAmount.toDouble(),
                variantValue = item.variantValue
            )
        }
        val bankSlip = bankSlipToHtml(paymentInfo)

        /**
         * Complete HTML for refund receipts
         */
        return """
            <html>
<body style="margin:0; padding:10px; background:#fff; font-family:sans-serif;">
    <div style="width: 310px; margin: 0 auto; color: #000;">
        <div style="text-align:center;">
            <img src="$logoBase64" style="width:120px; margin-bottom:10px;" />
            <h2 style="margin:0; font-size:22px; color: red;">RETURKVITTO</h2>
            <p style="font-size:14px; margin: 4px 0;">Café Kvarnen</p>
        </div>
        
        <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>
        
        <div style="font-size:14px; font-family:monospace;">
            Datum: $dateFormat<br/>
            Tid: $timeFormat<br/>
            Kvittonr: $receiptNumber<br/>
            Retur på order: $originalReceiptNumber
        </div>
        
        <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>
        
        <div style="margin: 10px 0;">
            $productsHtml
        </div>
        
        <hr style="border:none; border-top:1px dashed #000; margin:10px 0;"/>
        
        <table style="width:100%; font-family:monospace;">
            <tr>
                <td style="font-size:16px;">Moms (25%)</td>
                <td style="text-align:right; font-size:16px;">${"%.2f".format(vatAmount)} kr</td>
            </tr>
            <tr style="font-size:22px; font-weight:bold;">
                <td>ÅTERBETALAT</td>
                <td style="text-align:right;">${"%.2f".format(totalAmount)} kr</td>
            </tr>
        </table>
        
        <div class="bank-slip" style="margin-top:10px; border: 1px solid #ccc; padding: 5px; font-size: 12px;">$bankSlip</div>
        
        <div style="text-align:center; margin-top:30px;">
            <img src="$barcodeBase64" style="width:250px; height:auto;" />
            <div style="font-size:12px; margin-top:4px; font-family:monospace;">$receiptNumber</div>
            <p style="font-size:14px; margin-top:10px;">Välkommen åter!</p>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * This function is used for generating a mock list of all products with their EAN codes
     * This doesn't run anywhere, it has to be called manually using printEanCodes()
     */
    fun generateEanListHtml(order: Order, logoBase64: String): String {
        val productsHtml = order.items.joinToString("") { item ->
            val productBarcodeBitmap = generateReceiptBarcodeBitmap(item.product.ean, 500, 120)
            val productBarcodeBase64 = bitmapToBase64(productBarcodeBitmap)

            """
        <div style="margin-bottom:60px; page-break-inside: avoid; text-align:center; border-bottom:1px solid #eee; padding-bottom:20px;">
            <div style="font-size:20px; font-weight:bold;">${item.product.name}</div>
            <img src="$productBarcodeBase64" style="width:280px; height:auto; image-rendering: pixelated; margin-top:10px;" />
            <div style="font-size:16px; color:#555; margin-top:5px;">${item.product.ean}</div>
        </div>  
        """.trimIndent()
        }

        return """
        <html>
            <body style="margin:0; padding:10px; background:#fff; font-family:sans-serif;">
                <div style="width: 300px; margin: 0 auto">
                    <div style="text-align:center;">
                        <img src="$logoBase64" style="width:100px; margin-bottom:10px;" />
                        <h2 style="margin:0; font-size:20px;">PRODUKTLISTA</h2>
                    </div>
                    <hr style="border:none; border-top:2px solid #000; margin:15px 0;" />
                    $productsHtml
                </div>
            </body>
        </html>
    """.trimIndent()
    }

    fun bitmapToPixels(bitmap: Bitmap): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        return pixels
    }

    fun captureHtmlToBitmap(context: Context, html: String, onBitmapReady: (Bitmap) -> Unit) {
        val webView = WebView(context)
        webView.layout(0, 0, 384, 10)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Mät hur högt kvittot blev
                val widthSpec = View.MeasureSpec.makeMeasureSpec(384, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                view.measure(widthSpec, heightSpec)
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)

                val bitmap = Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                view.draw(canvas)

                onBitmapReady(bitmap)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }


// External printer

    fun cleanHtmlToPlainText(value: String?): String {
        if (value.isNullOrBlank()) return ""

        return value
            // Ersätt tabell-celler med mellanrum eller tabbar
            .replace("</tr>", "\n")
            .replace("</TR>", "\n")
            .replace("</td>", "  ")
            .replace("</TD>", "  ")
            .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
            // Ta bort alla andra HTML-taggar
            .replace(Regex("<[^>]*>"), "")
            // Snygga till entiteter
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .lineSequence()
            .map { it.replace(Regex("""[ \t]+"""), " ").trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }
}
