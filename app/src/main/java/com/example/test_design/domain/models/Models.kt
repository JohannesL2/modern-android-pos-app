package com.example.test_design.domain.models

import com.example.test_design.data.entity.PaymentMethod

/**
 * Ritning för hur en produkt ser ut i gränssnittet och varukorgen.
 */
data class UiProduct(
    val name: String,
    val variantValue: String?,
    val price: Int,
    val category: List<String>,
    val imageRes: String,
    val articleNumber: String,
    val ean: String
)
/**
 * Representerar en rad i varukorgen (en produkt + hur många man köper).
 */
data class CartItem(
    val product: UiProduct,
    var quantity: Int,
    val variantSelections: List<VariantSelection> = emptyList()
) {
    val pricePerItem: Int
        get() = product.price + variantSelections.sumOf { it.priceModifier }

    val totalPrice: Int
        get() = pricePerItem * quantity
}

data class VariantSelection(
    val name: String,
    val value: String,
    val priceModifier: Int = 0,
    val ean: String? = null
)

data class UiProductWithVariants(
    val baseProductCode: String,
    val name: String,
    val basePrice: Int,
    val category: List<String>,
    val imageRes: String,
    val variantGroups: List<VariantGroup>
)

data class VariantGroup(
    val name: String, // "Storlek", "Tillbehör"
    val options: List<VariantOption>
)

data class VariantOption(
    val value: String, // "Mellan"
    val priceModifier: Int,
    val ean: String? = null
)
/**
 * Data model for creating order in database
 */
data class Order(
    val receiptNumber: String,
    val items: List<CartItem>,
    val paymentMethod: PaymentMethod,
    val date: String,
    val time: String,
    val seller: String
)