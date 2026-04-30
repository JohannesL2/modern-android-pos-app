package com.example.test_design.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.test_design.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM product")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product")
    suspend fun getAllProductsOnce(): List<ProductEntity>

    @Query("SELECT * FROM product WHERE productName = :name LIMIT 1")
    suspend fun getByName(name: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM product")
    suspend fun deleteAllProducts()

}