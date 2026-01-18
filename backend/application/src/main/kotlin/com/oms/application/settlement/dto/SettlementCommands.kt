package com.oms.application.settlement.dto

import java.math.BigDecimal

data class CreateSettlementCommand(
    val companyId: String,
    val channelId: String,
    val year: Int,
    val month: Int
)

data class AddSettlementItemCommand(
    val orderId: String,
    val orderAmount: BigDecimal,
    val commissionRate: BigDecimal
)
