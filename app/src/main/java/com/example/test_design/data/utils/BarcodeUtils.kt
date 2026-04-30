package com.example.test_design.data.utils

import java.util.Locale

fun normalizeBarcode(value: String): String {
    return value.trim().filter { it.isLetterOrDigit() }.uppercase(Locale.ROOT)
}
