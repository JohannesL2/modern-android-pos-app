package com.example.test_design.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.test_design.data.entity.PaymentCardDetailsEntity
import com.example.test_design.data.entity.PaymentEntity
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.data.entity.PaymentStatus

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentCardDetails(cardDetails: PaymentCardDetailsEntity)

    @Query("SELECT * FROM payments WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getPaymentByOrderNumber(orderNumber: String): PaymentEntity?

    @Query("SELECT * FROM payment_card_details WHERE paymentId = :paymentId LIMIT 1")
    suspend fun getCardDetailsByPaymentId(paymentId: Long): PaymentCardDetailsEntity?

    @Query(
        """
    UPDATE payments
    SET status = :status,
        method = :paymentMethod
    WHERE orderNumber = :orderNumber
    """
    )
    suspend fun updatePaymentStatusAndMethod(
        orderNumber: String,
        status: PaymentStatus,
        paymentMethod: PaymentMethod?
    )

    @Query(
        """
        UPDATE payment_card_details
        SET appSpecificData = :appSpecificData
        WHERE paymentId IN (
            SELECT id FROM payments WHERE orderNumber = :orderNumber
        )
        """
    )
    suspend fun updatePaymentAppSpecificData(orderNumber: String, appSpecificData: String?)

    @Query("DELETE FROM payment_card_details WHERE paymentId = :paymentId")
    suspend fun deleteCardDetailsByPaymentId(paymentId: Long)

    @Query(
        """
            UPDATE payments
            SET amount = :amount
            WHERE orderNumber = :orderNumber
        """
    )
    suspend fun updatePaymentAmount(orderNumber: String, amount: Int)

    @Query(
        """
            UPDATE payments
            SET paidAmount = MIN(amount, paidAmount + :paidAmount)
            WHERE orderNumber = :orderNumber
        """
    )
    suspend fun incrementPaymentPaidAmount(orderNumber: String, paidAmount: Int)

    @Transaction
    suspend fun insertPaymentCardDetailsForOrderAndIncrementPaidAmount(
        orderNumber: String,
        paidAmount: Int,
        cardBrand: String?,
        maskedPan: String?,
        paymentInfo: String?,
        appSpecificData: String? = null
    ) {
        val payment = getPaymentByOrderNumber(orderNumber)
            ?: throw IllegalArgumentException("Payment not found for order $orderNumber")

        insertPaymentCardDetails(
            PaymentCardDetailsEntity(
                paymentId = payment.id,
                createdAt = System.currentTimeMillis(),
                cardBrand = cardBrand,
                maskedPan = maskedPan,
                paymentInfo = paymentInfo,
                appSpecificData = appSpecificData
            )
        )
        incrementPaymentPaidAmount(orderNumber, paidAmount)
    }

    @Transaction
    suspend fun upsertCardDetailsForOrder(
        orderNumber: String,
        cardBrand: String?,
        maskedPan: String?,
        paymentInfo: String?,
        appSpecificData: String? = null
    ) {
        val payment = getPaymentByOrderNumber(orderNumber)
            ?: throw IllegalArgumentException("Payment not found for order $orderNumber")

        deleteCardDetailsByPaymentId(payment.id)
        insertPaymentCardDetails(
            PaymentCardDetailsEntity(
                paymentId = payment.id,
                cardBrand = cardBrand,
                maskedPan = maskedPan,
                paymentInfo = paymentInfo,
                appSpecificData = appSpecificData
            )
        )
    }
}
