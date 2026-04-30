package com.example.test_design.domain.models

import java.util.UUID

data class PartialPayment(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int,
    val lastFour: String,
    val time: String
)