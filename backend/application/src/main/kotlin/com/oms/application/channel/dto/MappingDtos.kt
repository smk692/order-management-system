package com.oms.application.channel.dto

import com.oms.channel.domain.vo.MappingRole

/**
 * Command to assign a warehouse to a channel
 */
data class AssignWarehouseCommand(
    val warehouseId: String,
    val role: MappingRole,
    val priority: Int = 0
)

/**
 * Result DTO for channel-warehouse mapping
 */
data class MappingResult(
    val id: String,
    val channelId: String,
    val warehouseId: String,
    val role: MappingRole,
    val priority: Int,
    val createdAt: String
)
