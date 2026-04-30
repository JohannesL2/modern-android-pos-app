package com.example.test_design.events

sealed interface CartUiEvent {
    data class ProductAdded(val productName: String) : CartUiEvent
    object InvalidScan : CartUiEvent
}