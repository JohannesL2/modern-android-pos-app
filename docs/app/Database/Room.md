---
sidebar_label: 'Database (Room)'
sidebar_position: 2
---

# Room Database

We use **Room** as a persistence library to provide an abstraction layer over SQLite. This allows for more robust database access while leveraging the full power of SQLite.

## Database Architecture

The database is structured into three main components: **Entities**, **DAOs**, and the **Database** class itself.

---

### Schema Overview

Below is a high-level view of how the local data is structured.



---

### Entity: ProductEntity

This entity represents the `products` table in the database. It is used to cache products from the API for office use.

```kotlin
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
```

### Type Converters

Since SQLite does not support storing lists natively, we use a `Converters` class to map the `category` list to a JSON string and back.

#### Handled Data Types:
* **Complex Objects:** `List<VariantSelection>` (mapped via JSON).
* **Lists:** `List<String>` (mapped via JSON).
* **Enums:** `OrderStatus`, `PaymentMethod`, `PaymentStatus`, and `PaymentType`.

#### Example (JSON Mapping)
```kotlin
@TypeConverter
fun fromVariantSelections(values: List<VariantSelection>): String {
    val jsonArray = JSONArray()
    values.forEach {
        val obj = JSONObject()
        obj.put("name", it.name)
        obj.put("value", it.value)
        obj.put("priceModifier", it.priceModifier)
        obj.put("ean", it.ean)
        jsonArray.put(obj)
    }
    return jsonArray.toString()
}
```

---

### DAO: ProductDao

The Data Access Object (DAO) contains the methods used for accessing the database.

```kotlin
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
```

### Database Configuration

The `AppDatabase` class is the central hub of the database. It uses the **Singleton pattern** to ensure only one instance of the database exists at a time, preventing memory leaks and resource conflicts.

---

### Entities & Versioning
The database currently tracks the following entities:
* `ProductEntity`
* `OrderEntity`
* `OrderRow`
* `PaymentEntity`
* `PaymentCardDetailsEntity`

> **Warning:** The current version is **12**. Any changes to the Entity schemas require an update to the version number. Due to `.fallbackToDestructiveMigration(true)`, the database will be cleared on schema changes during development.

---

### Initialization & Seeding

When the database is created for the first time (`onCreate`), it triggers a **DatabaseSeeder**. This ensures the app has a set of default products and test data available immediately.

```kotlin
.addCallback(object : Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Seeding runs in a background thread to avoid blocking the UI
        CoroutineScope(Dispatchers.IO).launch {
            INSTANCE?.let { database ->
                prepopulate(database)
            }
        }
    }
})
.build()
```