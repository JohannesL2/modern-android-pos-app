package com.example.test_design.data.utils

import com.example.test_design.data.dao.OrderDao

object ReceiptNumberGenerator {
    private const val MIN_RECEIPT_NUMBER = 100000
    private const val MAX_RECEIPT_NUMBER = 999999

    suspend fun receiptNumber(orderDao: OrderDao): String {
        val highestExisting = orderDao.getHighestSequentialReceiptNumber() ?: (MIN_RECEIPT_NUMBER - 1)
        val nextReceiptNumber = highestExisting + 1

        check(nextReceiptNumber <= MAX_RECEIPT_NUMBER) {
            "Receipt number sequence exceeded $MAX_RECEIPT_NUMBER"
        }

        return nextReceiptNumber.toString().padStart(6, '0')
    }
}
