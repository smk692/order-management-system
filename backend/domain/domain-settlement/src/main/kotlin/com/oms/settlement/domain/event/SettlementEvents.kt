package com.oms.settlement.domain.event

import com.oms.core.event.DomainEvent
import com.oms.settlement.domain.vo.SettlementPeriod
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Event fired when a new settlement is created
 */
class SettlementCreatedEvent(
    val settlementId: UUID,
    val companyId: UUID,
    val channelId: String,
    val period: SettlementPeriod,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : DomainEvent() {
    override val aggregateId: String = settlementId.toString()
    override val aggregateType: String = "Settlement"
}

/**
 * Event fired when a settlement is confirmed
 */
class SettlementConfirmedEvent(
    val settlementId: UUID,
    val companyId: UUID,
    val channelId: String,
    val period: SettlementPeriod,
    val netSettlement: BigDecimal,
    val confirmedAt: LocalDateTime = LocalDateTime.now()
) : DomainEvent() {
    override val aggregateId: String = settlementId.toString()
    override val aggregateType: String = "Settlement"
}

/**
 * Event fired when a settlement is paid
 */
class SettlementPaidEvent(
    val settlementId: UUID,
    val companyId: UUID,
    val channelId: String,
    val netSettlement: BigDecimal,
    val paidAt: LocalDateTime = LocalDateTime.now()
) : DomainEvent() {
    override val aggregateId: String = settlementId.toString()
    override val aggregateType: String = "Settlement"
}
