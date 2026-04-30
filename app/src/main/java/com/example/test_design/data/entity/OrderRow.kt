package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.test_design.domain.models.VariantSelection


@Entity(
        tableName = "order_row",
        foreignKeys = [ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderNumber"],
            childColumns = ["orderNumber"],
            onDelete = ForeignKey.CASCADE
        )],
        indices = [Index("orderNumber")]
    )
    data class OrderRow(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val orderNumber: String,
        val articleNumber: String,
        val productName: String,
        val variantValue: String?,
        val unitPrice: Int,
        val lineAmount: Int,
        val quantity: Int,
        val refundedQuantity: Int,
        val refundedAmount: Double = 0.0,
        val originalOrderItemId: String? = null,
)
