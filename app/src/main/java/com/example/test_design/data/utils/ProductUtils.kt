package com.example.test_design.data.utils

fun generateEAN(): String {
    return (1000000000000..9999999999999).random().toString()
}

fun generateArticleNumber(): String {
    return "ART-" + (100000..999999).random()
}

fun generateOrderNumber(): String {
    return (100000..999999).random().toString()
}