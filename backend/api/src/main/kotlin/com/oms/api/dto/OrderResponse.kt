package com.oms.api.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderResponse(
    val id: String,
    val channel: String,
    val customerName: String,
    val customerEmail: String?,
    val totalAmount: BigDecimal,
    val currency: String,
    val status: String,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)
