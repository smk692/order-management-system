package com.oms.application.settlement.dto

import com.oms.settlement.domain.vo.SettlementPeriod
import com.oms.settlement.domain.vo.SettlementStatus
import java.math.BigDecimal

data class SettlementResult(
    val id: String,
    val companyId: String,
    val channelId: String,
    val period: SettlementPeriod,
    val status: SettlementStatus,
    val totalSales: BigDecimal,
    val totalCommission: BigDecimal,
    val netSettlement: BigDecimal,
    val confirmedAt: String?,
    val paidAt: String?,
    val items: List<SettlementItemResult>,
    val createdAt: String,
    val updatedAt: String
)

data class SettlementItemResult(
    val id: Long?,
    val orderId: String,
    val orderAmount: BigDecimal,
    val commissionRate: BigDecimal,
    val commissionAmount: BigDecimal,
    val settlementAmount: BigDecimal
)
