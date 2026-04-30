package com.example.test_design.data.repository

import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.entity.ProductEntity
import com.example.test_design.data.utils.normalizeBarcode
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {
    fun observeProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun findByScannedCode(scannedCode: String): ProductEntity? {
        val normalizedScannedCode = normalizeBarcode(scannedCode)
        if (normalizedScannedCode.isBlank()) return null

        return productDao.getAllProductsOnce().firstOrNull { product ->
            normalizeBarcode(product.ean) == normalizedScannedCode
        }
    }
}
