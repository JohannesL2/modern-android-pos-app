package com.example.test_design.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


//När vi har betalat så skapas orderhuvud med kvitto nr, datum, säljare och total kostnad.
//Typ som Gardeco dom skapar order nummer/kvitto nummer direkt när man lägger något i varukorgen
//Om man man cancellar eller och inte betalar så blir ordern annulerad, så det finns order status

@Entity(
    tableName = "order_head",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalOrderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["orderNumber"], unique = true),
        Index(value = ["originalOrderId"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val orderNumber: String,

    val orderDate: Long = System.currentTimeMillis(),

    val originalOrderId: Long? = null,

    val totalAmount: Int,

    val status: OrderStatus,

    val refundedAmount: Int = 0,

    val sellerName: String = "John Doe"
)
