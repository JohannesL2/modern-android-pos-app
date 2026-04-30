package com.example.test_design.data.db

import androidx.room.TypeConverter
import com.example.test_design.data.entity.OrderStatus
import com.example.test_design.data.entity.PaymentMethod
import com.example.test_design.data.entity.PaymentStatus
import com.example.test_design.data.entity.PaymentType
import com.example.test_design.domain.models.VariantSelection
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromVariantSelections(values: List<VariantSelection>): String {
        val jsonArray = JSONArray()

        values.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("value", it.value)
            obj.put("priceModifier", it.priceModifier)
            obj.put("ean", it.ean) // ✅ LÄGG TILL HÄR
            jsonArray.put(obj)
        }

        return jsonArray.toString()
    }

    @TypeConverter
    fun toVariantSelections(value: String): List<VariantSelection> {
        val jsonArray = JSONArray(value)

        return List(jsonArray.length()) { index ->
            val obj = jsonArray.getJSONObject(index)
            VariantSelection(
                name = obj.getString("name"),
                value = obj.getString("value"),
                priceModifier = obj.optInt("priceModifier", 0),
                ean = obj.optString("ean", null) // ✅ LÄGG TILL HÄR
            )
        }
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? =
        value?.let { PaymentMethod.valueOf(it) }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus =
        PaymentStatus.valueOf(value)

    @TypeConverter
    fun fromPaymentType(type: PaymentType): String = type.name

    @TypeConverter
    fun toPaymentType(value: String): PaymentType =
        PaymentType.valueOf(value)

    @TypeConverter
    fun fromStringList(values: List<String>): String {
        return JSONArray(values).toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val jsonArray = JSONArray(value)
        return List(jsonArray.length()) { index -> jsonArray.getString(index) }
    }
}

