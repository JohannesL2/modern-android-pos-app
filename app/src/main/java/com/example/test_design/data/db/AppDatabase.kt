package com.example.test_design.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao
import com.example.test_design.data.entity.ProductEntity
import com.example.test_design.data.dao.ProductDao
import com.example.test_design.data.entity.OrderEntity
import com.example.test_design.data.entity.OrderRow
import com.example.test_design.data.entity.PaymentCardDetailsEntity
import com.example.test_design.data.entity.PaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderRow::class,
        PaymentEntity::class,
        PaymentCardDetailsEntity::class
    ],
    version = 12, //If you change anything in Database, update this!
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile

        private var INSTANCE: AppDatabase? = null

        private suspend fun prepopulate(database: AppDatabase) {
            DatabaseSeeder(
                db = database,
                orderDao = database.orderDao(),
                paymentDao = database.paymentDao(),
                productDao = database.productDao()
            ).seed()
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulate(INSTANCE!!)
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
