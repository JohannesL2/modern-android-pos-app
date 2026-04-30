package com.example.test_design.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_design.domain.models.CartItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    var loadingMessage by mutableStateOf(com.example.test_design.R.string.desc_security_processing)

    // Låsskärm & Säkerhet
    var isLocked by mutableStateOf(savedStateHandle.get<Boolean>("isLocked") ?: true)
        private set

    var isSellerLoggedIn by mutableStateOf(savedStateHandle.get<Boolean>("isSellerLoggedIn") ?: false)
        private set

    fun unlockApp() {
        viewModelScope.launch {
            loadingMessage = com.example.test_design.R.string.desc_unlocking
            showLockLoading = true

            delay(1500)

            isLocked = false
            savedStateHandle["isLocked"] = false
            isSellerLoggedIn = true
            savedStateHandle["isSellerLoggedIn"] = true
            showSellerLogin = false
            showLockLoading = false
        }
    }

    var showSellerLogin by mutableStateOf(true)

    var showLockLoading by mutableStateOf(false)

    val sellerPin = "1234"


    // Rabatt & Betalningar
    var discountAmount by mutableStateOf(0)
        private set

    var appliedCode by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)

    // Funktioner

    fun calculateTotal(cartItems: List<CartItem>): Int {
        val subtotal = cartItems.sumOf { it.product.price * it.quantity }
        return maxOf(0, subtotal - discountAmount)
    }

    fun getFinalTotal(subTotal: Int): Int {
        return (subTotal - discountAmount).coerceAtLeast(0)
    }

    fun verifyPin(pin: String): Boolean {
        return pin == sellerPin
    }

    fun lockApp() {
        viewModelScope.launch {
            loadingMessage = com.example.test_design.R.string.desc_locking
            showLockLoading = true

            delay(1500)

            isLocked = true
            showSellerLogin  = true
            isSellerLoggedIn = false
            showLockLoading = false
        }
    }

    fun applyDiscount(amount: Int, code: String) {
        discountAmount = amount
        appliedCode = code
    }

    fun resetDiscount() {
        discountAmount = 0
        appliedCode = ""
    }
}