package com.example.test_design.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.example.test_design.data.entity.OrderEntity
import com.example.test_design.data.entity.OrderRow
import com.example.test_design.data.entity.OrderStatus

data class OrderWithRows(
    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "orderNumber",
        entityColumn = "orderNumber"
    )
    val rows: List<OrderRow>
)
// Hämta tidigare ordrar/kvitton
@Dao
interface OrderDao {
    @Query("SELECT * FROM order_head WHERE orderNumber = :receiptNumber")
    suspend fun getOrderByReceipt(receiptNumber: String): OrderEntity?

    @Query(
        """
        SELECT MAX(CAST(orderNumber AS INTEGER))
        FROM order_head
        WHERE orderNumber GLOB '[0-9][0-9][0-9][0-9][0-9][0-9]'
        """
    )
    suspend fun getHighestSequentialReceiptNumber(): Int?

    @Query("""
    UPDATE order_head
    SET refundedAmount = :refundedAmount,
        status = :status
    WHERE orderNumber = :receiptNumber
""")
    suspend fun updateRefund(
        receiptNumber: String,
        refundedAmount: Int,
        status: OrderStatus
    )


    @Query("UPDATE order_head SET status = :status WHERE orderNumber = :receiptNumber")
    suspend fun updateOrderStatus(receiptNumber: String, status: OrderStatus)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderRows(rows: List<OrderRow>)

    @Transaction
    @Query("SELECT * FROM order_head WHERE orderNumber = :receiptNumber")
    suspend fun getOrderWithRows(receiptNumber: String): OrderWithRows

    @Transaction
    suspend fun insertFullOrder(order: OrderEntity, rows: List<OrderRow>) {
        insertOrder(order)
        insertOrderRows(rows)
    }

    @Transaction
    @Query("SELECT * FROM order_head")
    suspend fun getAllOrdersWithRows(): List<OrderWithRows>

    @Query("SELECT * FROM order_row WHERE orderNumber = :orderNumber")
    suspend fun getRowsByReceipt(orderNumber: String): List<OrderRow>

    @Query("DELETE FROM order_row WHERE orderNumber = :orderNumber")
    suspend fun deleteRowsForOrder(orderNumber: String)

    @Query("""
        UPDATE order_head
        SET totalAmount = :totalAmount
        WHERE orderNumber = :receiptNumber
    """)
    suspend fun updateOrderTotal(
        receiptNumber: String,
        totalAmount: Int
    )

    @Transaction
    suspend fun replaceOrder(
        receiptNumber: String,
        newTotal: Int,
        newRows: List<OrderRow>
    ) {
        deleteRowsForOrder(receiptNumber)
        insertOrderRows(newRows)
        updateOrderTotal(receiptNumber, newTotal)
    }

    @Query(
        """
        UPDATE order_row
        SET refundedQuantity = :refundedQuantity,
            refundedAmount = :refundedAmount
        WHERE id = :rowId
        """
    )
    suspend fun updateOrderRowRefund(
        rowId: Int,
        refundedQuantity: Int,
        refundedAmount: Double
    )

//    @Transaction
//    suspend fun insertFullOrderWithEAN(order: OrderEntity, rows: List<OrderRow>) {
//        val orderEAN = generateEAN()
//        val orderWithEan = order.copy(ean13 = orderEAN)
//
//        insertOrder(orderWithEan)
//        insertOrderRows(rows)
//    }

    @Transaction
    suspend fun markOrderVoided(receiptNumber: String) {
        val order = getOrderByReceipt(receiptNumber)
            ?: throw IllegalArgumentException("Order not found")

        if (order.status == OrderStatus.VOIDED) {
            throw IllegalStateException("Order already voided")
        }

        updateRefund(
            receiptNumber = receiptNumber,
            refundedAmount = 0,
            status = OrderStatus.VOIDED
        )
    }

    @Query("SELECT status FROM order_head WHERE orderNumber = :receiptNumber")
    suspend fun getOrderStatus(receiptNumber: String): OrderStatus?
}
