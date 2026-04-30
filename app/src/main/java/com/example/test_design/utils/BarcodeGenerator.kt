package com.example.test_design.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import android.graphics.Color
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.google.zxing.EncodeHintType

fun generateReceiptBarcodeBitmap(
    receiptNumber: String,
    width: Int = 400,
    height: Int = 200
): Bitmap {

    val hints = hashMapOf<EncodeHintType, Any>()
    hints[EncodeHintType.MARGIN] = 0

    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        receiptNumber,
        BarcodeFormat.CODE_128,
        width,
        height,
        hints
    )

    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x,
                y,
                if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            )
        }
    }

    return bitmap
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val bytes = baos.toByteArray()
    return "data:image/png;base64," +
            Base64.encodeToString(bytes, Base64.NO_WRAP)
}

//fun generateEAN13FromReceipt(receiptNumber: String): String {
//    val prefix = ""
//    val partial = prefix + receiptNumber.padStart(6, '0')
//    return partial + calculateEAN13CheckDigit(partial)
//}
//
//fun calculateEAN13CheckDigit(code12: String): String {
//    require(code12.length == 12)
//    val sum = code12.mapIndexed { index, c ->
//        val digit = c.digitToInt()
//        if (index % 2 == 0) digit else digit * 3
//    }.sum()
//    val check = (10 - (sum % 10)) % 10
//    return check.toString()
//}