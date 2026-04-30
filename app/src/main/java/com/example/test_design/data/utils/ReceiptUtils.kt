package com.example.test_design.data.utils

fun String.isValidReceipt(): Boolean =
    length == 6 && all { it.isDigit() }