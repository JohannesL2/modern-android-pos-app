package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "product")
data class ProductEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val articleNumber: String,

    val ean: String,

    val productName: String,

    val baseProductCode: String,

    val variantType: String? = null,
    val variantValue: String? = null,

    val isVariant: Boolean,

    val category: List<String>,

    val unitPrice: Int,

    val vatRate: Double,

    val imageResName: String,
    val priceModifier: Int = 0,
)