package com.example.test_design.data.repository

import com.example.test_design.data.dao.OrderDao
import com.example.test_design.data.entity.OrderEntity

class OrderRepository(private val orderDao: OrderDao) {
    suspend fun findOrderByReceipt(receipt: String): OrderEntity? {
        return orderDao.getOrderByReceipt(receipt)
    }
}
