package com.example.test_design.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.dao.PaymentDao


class OrderViewModelFactory(
    private val orderDao: OrderDao,
    private val paymentDao: PaymentDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(orderDao, paymentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}