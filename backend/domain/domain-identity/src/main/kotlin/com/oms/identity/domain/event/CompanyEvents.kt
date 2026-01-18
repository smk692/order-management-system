package com.oms.identity.domain.event

import com.oms.core.event.DomainEvent
import com.oms.identity.domain.CompanyStatus

/**
 * Event fired when a new company is created
 */
class CompanyCreatedEvent(
    val companyId: String,
    val name: String,
    val businessNumber: String
) : DomainEvent() {
    override val aggregateId: String = companyId
    override val aggregateType: String = "Company"
}

/**
 * Event fired when company status changes
 */
class CompanyStatusChangedEvent(
    val companyId: String,
    val previousStatus: CompanyStatus,
    val newStatus: CompanyStatus
) : DomainEvent() {
    override val aggregateId: String = companyId
    override val aggregateType: String = "Company"
}
