package com.example.test_design.data.db

import androidx.room.withTransaction
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.entity.*

class DatabaseSeeder(
    private val db: AppDatabase,
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao,
    private val productDao: ProductDao
) {

    private fun createBaseProduct(
        baseCode: String,
        name: String
    ): ProductEntity {
        val basePrice = getBasePrice(name)
        return ProductEntity(
            articleNumber = baseCode,
            ean = "",
            productName = name,
            baseProductCode = baseCode,
            variantType = null,
            variantValue = null,
            isVariant = false,
            category = getCategory(name),
            unitPrice = basePrice,
            vatRate = 0.25,
            imageResName = getImage(name),
            priceModifier = 0
        )
    }

    suspend fun seed() {
        seedProducts()
        seedMockOrder()
    }


    private suspend fun seedProducts() {
        val products = mutableListOf<ProductEntity>()

        // Base products
        products.add(createBaseProduct("100001", "Kaffe"))
        products.add(createBaseProduct("100002", "Latte"))
        products.add(createBaseProduct("100003", "Kaka"))
        products.add(createBaseProduct("100004", "Smörgås"))
        products.add(createBaseProduct("100005", "Smoothie"))
        products.add(createBaseProduct("100006", "Chocolate Chip Cookie"))
        products.add(createBaseProduct("100007", "Vegansk Chocolate Chip Cookie"))
        products.add(createBaseProduct("100008", "Havre latte"))
        products.add(createBaseProduct("100009", "Matcha latte"))

        // Variant products
        products.addAll(listOf(
            createVariant("100001", "2000001000011", "Kaffe", "S", 0),
            createVariant("100001", "2000001000012", "Kaffe", "M", 5),
            createVariant("100001", "2000001000013", "Kaffe", "L", 10),
            createVariant("100002", "2000001000021", "Latte", "S", 0),
            createVariant("100002", "2000001000022", "Latte", "M", 5),
            createVariant("100002", "2000001000023", "Latte", "L", 10),
            createStandard("100003", "2000001000031", "Kaka"),
            createVariant("100004", "2000001000041", "Smörgås", "KIDS", -25),
            createVariant("100004", "2000001000042", "Smörgås", "STANDARD", 0),
            createVariant("100005", "2000001000051", "Smoothie", "S", 0),
            createVariant("100005", "2000001000052", "Smoothie", "M", 5),
            createVariant("100005", "2000001000053", "Smoothie", "L", 10),
            createStandard("100006", "2000001000061", "Chocolate Chip Cookie"),
            createStandard("100007", "2000001000071", "Vegansk Chocolate Chip Cookie"),
            createVariant("100008", "2000001000081", "Havre latte", "S", 0),
            createVariant("100008", "2000001000082", "Havre latte", "M", 5),
            createVariant("100008", "2000001000083", "Havre latte", "L", 10),
            createVariant("100009", "2000001000091", "Matcha latte", "S", 0),
            createVariant("100009", "2000001000092", "Matcha latte", "M", 5),
            createVariant("100009", "2000001000093", "Matcha latte", "L", 10),
        ))

        // Rensa tabellen innan insättning
        productDao.deleteAllProducts()
        products.forEach { productDao.insertProduct(it) }
    }

    // 🔥 Skapa variant med priceModifier
    private fun createVariant(
        baseCode: String,
        ean: String,
        name: String,
        size: String,
        priceModifier: Int? = null
    ): ProductEntity {
        val basePrice = getBasePrice(name)


        val finalModifier = when {
            size.equals("Kids", ignoreCase = true) -> -(basePrice / 2)
            priceModifier != null -> priceModifier
            else -> 0
        }

        return ProductEntity(
            articleNumber = "${baseCode}-$size",
            ean = ean,
            productName = name,
            baseProductCode = baseCode,
            variantType = size,
            variantValue = size,
            isVariant = true,
            category = getCategory(name),
            unitPrice = basePrice + finalModifier,
            vatRate = 0.25,
            imageResName = getImage(name),
            priceModifier = finalModifier
        )
    }

    // 🔥 Skapa standardprodukt
    private fun createStandard(
        baseCode: String,
        ean: String,
        name: String
    ): ProductEntity {
        val basePrice = getBasePrice(name)
        return ProductEntity(
            articleNumber = baseCode,
            ean = ean,
            productName = name,
            baseProductCode = baseCode,
            variantType = "SIZE",
            variantValue = "STANDARD",
            isVariant = false,
            category = getCategory(name),
            unitPrice = basePrice,
            vatRate = 0.25,
            imageResName = getImage(name),
            priceModifier = 0
        )
    }

    private fun getBasePrice(name: String): Int = when (name) {
        "Kaffe" -> 25
        "Latte", "Havre latte" -> 59
        "Matcha latte" -> 69
        "Kaka" -> 39
        "Smörgås" -> 55
        "Smoothie" -> 60
        "Chocolate Chip Cookie" -> 50
        "Vegansk Chocolate Chip Cookie" -> 90
        else -> 0
    }

    private fun getCategory(name: String): List<String> = when (name) {
        "Kaffe", "Latte", "Havre latte", "Matcha latte", "Smoothie" -> listOf("Dryck")
        "Kaka", "Chocolate Chip Cookie", "Vegansk Chocolate Chip Cookie" -> listOf("Snacks")
        "Smörgås" -> listOf("Mat")
        else -> listOf("Övrigt")
    }

    private fun getImage(name: String): String = when (name) {
        "Kaffe" -> "com.example.test_design.R.drawable.coffee"
        "Latte", "Havre latte" -> "com.example.test_design.R.drawable.latte"
        "Matcha latte" -> "com.example.test_design.R.drawable.matcha_latte"
        "Kaka" -> "com.example.test_design.R.drawable.cake"
        "Smörgås" -> "com.example.test_design.R.drawable.sandwich"
        "Smoothie" -> "com.example.test_design.R.drawable.smoothie"
        else -> "com.example.test_design.R.drawable.cookie"
    }

    private suspend fun seedMockOrder() {
        val existingOrder = orderDao.getOrderByReceipt("123456")
        if (existingOrder != null) return

        // Korrekt typning för OrderRow
        val rows = listOf(
            OrderRow(
                orderNumber = "123456",
                productName = "Latte",
                articleNumber = "100002-M",
                unitPrice = 64,
                quantity = 2,
                lineAmount = 128,
                refundedQuantity = 0,
                variantValue = "M"
            ),
            OrderRow(
                orderNumber = "123456",
                productName = "Kaka",
                articleNumber = "100003",
                unitPrice = 39,
                quantity = 1,
                lineAmount = 39,
                refundedQuantity = 0,
                variantValue = "STANDARD"
            ),
            OrderRow(
                orderNumber = "123456",
                productName = "Chocolate Chip Cookie",
                articleNumber = "100006",
                unitPrice = 90,
                quantity = 1,
                lineAmount = 90,
                refundedQuantity = 0,
                variantValue = "STANDARD"
            )
        )

        val totalAmount = rows.sumOf { it.lineAmount }

        val order = OrderEntity(
            orderNumber = "123456",
            totalAmount = totalAmount,
            status = OrderStatus.PAID,
            refundedAmount = 0
        )

        val payment = PaymentEntity(
            orderNumber = "123456",
            type = PaymentType.PAYMENT,
            amount = totalAmount,
            status = PaymentStatus.COMPLETED,
            method = PaymentMethod.CASH,
            terminalId = "001",
            userId = "001",
            paidAmount = totalAmount
        )

        db.withTransaction {
            orderDao.insertFullOrder(order, rows)
            val paymentId = paymentDao.insertPayment(payment)
            paymentDao.insertPaymentCardDetails(
                PaymentCardDetailsEntity(
                    paymentId = paymentId,
                    appSpecificData = "For test"
                )
            )
        }
    }
}
