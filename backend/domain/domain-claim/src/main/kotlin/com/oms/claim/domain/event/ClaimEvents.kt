package com.oms.claim.domain.event

import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import com.oms.core.event.DomainEvent
import java.math.BigDecimal
import java.util.UUID

sealed class ClaimEvent : DomainEvent() {
    override val aggregateType: String = "Claim"

    data class ClaimCreatedEvent(
        override val aggregateId: String,
        val orderId: String,
        val type: ClaimType,
        val companyId: UUID
    ) : ClaimEvent()

    data class ClaimStatusChangedEvent(
        override val aggregateId: String,
        val oldStatus: ClaimStatus,
        val newStatus: ClaimStatus
    ) : ClaimEvent()

    data class ClaimCompletedEvent(
        override val aggregateId: String,
        val refundAmount: BigDecimal
    ) : ClaimEvent()

    data class ClaimRejectedEvent(
        override val aggregateId: String,
        val reason: String
    ) : ClaimEvent()
}
