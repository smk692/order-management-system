package com.oms.identity.domain.event

import com.oms.core.event.DomainEvent
import com.oms.identity.domain.UserRole
import com.oms.identity.domain.UserStatus

/**
 * Event fired when a new user is created
 */
class UserCreatedEvent(
    val userId: String,
    val companyId: String,
    val email: String,
    val name: String,
    val role: UserRole
) : DomainEvent() {
    override val aggregateId: String = userId
    override val aggregateType: String = "User"
}

/**
 * Event fired when user status changes
 */
class UserStatusChangedEvent(
    val userId: String,
    val companyId: String,
    val previousStatus: UserStatus,
    val newStatus: UserStatus
) : DomainEvent() {
    override val aggregateId: String = userId
    override val aggregateType: String = "User"
}

/**
 * Event fired when user role changes
 */
class UserRoleChangedEvent(
    val userId: String,
    val companyId: String,
    val previousRole: UserRole,
    val newRole: UserRole
) : DomainEvent() {
    override val aggregateId: String = userId
    override val aggregateType: String = "User"
}
