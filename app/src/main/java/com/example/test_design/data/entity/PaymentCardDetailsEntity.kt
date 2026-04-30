package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_card_details",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paymentId"])]
)
data class PaymentCardDetailsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paymentId: Long,
    val cardBrand: String? = null,
    val maskedPan: String? = null,
    val paymentInfo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val appSpecificData: String? = null,
)
