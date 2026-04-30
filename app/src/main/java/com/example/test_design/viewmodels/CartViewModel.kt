package com.example.test_design.viewmodels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.test_design.domain.models.CartItem
import com.example.test_design.domain.models.UiProduct
import com.example.test_design.data.entity.ProductEntity
import com.example.test_design.data.repository.ProductRepository
import com.example.test_design.data.utils.normalizeBarcode
import com.example.test_design.events.CartUiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.setValue

class CartViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _cart = mutableStateListOf<CartItem>()
    val cart: SnapshotStateList<CartItem> = _cart

    var appliedDiscount by mutableStateOf(0)

    private val _uiEvents = MutableSharedFlow<CartUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    suspend fun addProductByScannedCode(scannedCode: String): Boolean {
        val product = productRepository.findByScannedCode(scannedCode)
        return if (product != null) {
            addProduct(product)
            _uiEvents.emit(CartUiEvent.ProductAdded(product.productName))
            true
        } else {
            _uiEvents.emit(CartUiEvent.InvalidScan)
            false
        }
    }

    fun addProductToCart(uiProduct: UiProduct) {
        val productKey = normalizeBarcode(uiProduct.ean)
        val existingIndex = _cart.indexOfFirst { normalizeBarcode(it.product.ean) == productKey }

        if (existingIndex >= 0) {
            _cart[existingIndex] = _cart[existingIndex].copy(
                quantity = _cart[existingIndex].quantity + 1
            )
        } else {
            _cart.add(CartItem(uiProduct, 1))
        }
    }

    // En hjälpfunktion för att slippa duplicera kod (Extension function)
    private fun ProductEntity.toUiProduct() = UiProduct(
        name = this.productName,
        price = this.unitPrice,
        category = this.category ?: emptyList(),
        imageRes = this.imageResName,
        articleNumber = this.articleNumber,
        ean = this.ean ?: "",
        variantValue = this.variantValue,
    )

    val totalSum by derivedStateOf {
        cart.sumOf { it.product.price * it.quantity }
    }

    private fun addProduct(productEntity: ProductEntity) {
        val uiProduct = UiProduct(
            name = productEntity.productName,
            price = productEntity.unitPrice,
            category = productEntity.category,
            imageRes = productEntity.imageResName,
            articleNumber = productEntity.articleNumber,
            ean = productEntity.ean,
            variantValue = productEntity.variantValue,
        )

        val productKey = normalizeBarcode(uiProduct.ean)

        val existingIndex = _cart.indexOfFirst { normalizeBarcode(it.product.ean) == productKey }

        if (existingIndex >= 0) {
            _cart[existingIndex] = _cart[existingIndex].copy(quantity = _cart[existingIndex].quantity + 1)
        } else {
            _cart.add(CartItem(uiProduct, 1))
        }
    }

}
