package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [Index(value = ["orderNumber"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val type: PaymentType,
    val amount: Int,
    val paidAmount: Int,
    val currency: String = "SEK",
    val status: PaymentStatus,
    val method: PaymentMethod,
    val terminalId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
)
