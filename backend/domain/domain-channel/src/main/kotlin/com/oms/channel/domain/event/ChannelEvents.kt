package com.oms.channel.domain.event

import com.oms.channel.domain.vo.ChannelStatus
import com.oms.channel.domain.vo.ChannelType
import com.oms.channel.domain.vo.MappingRole
import com.oms.core.event.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Sealed class hierarchy for Channel domain events
 */
sealed class ChannelEvent : DomainEvent() {
    override val aggregateType: String = "Channel"
}

/**
 * Event emitted when a new channel is created
 */
data class ChannelCreatedEvent(
    override val aggregateId: String,
    val companyId: UUID,
    val type: ChannelType
) : ChannelEvent()

/**
 * Event emitted when a channel's status changes
 */
data class ChannelStatusChangedEvent(
    override val aggregateId: String,
    val oldStatus: ChannelStatus,
    val newStatus: ChannelStatus
) : ChannelEvent()

/**
 * Event emitted when a new warehouse is created
 */
data class WarehouseCreatedEvent(
    override val aggregateId: String,
    val companyId: UUID
) : ChannelEvent()

/**
 * Event emitted when a channel-warehouse mapping changes
 */
data class WarehouseMappingChangedEvent(
    override val aggregateId: String,
    val channelId: String,
    val warehouseId: String,
    val role: MappingRole
) : ChannelEvent()
