package com.oms.api.dto

import java.math.BigDecimal

data class CreateOrderRequest(
    val channelId: String,
    val customerName: String,
    val customerEmail: String?,
    val items: List<CreateOrderItemRequest>,
)

data class CreateOrderItemRequest(
    val productId: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
)
