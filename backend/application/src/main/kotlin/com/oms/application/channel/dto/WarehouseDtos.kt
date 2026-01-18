package com.oms.application.channel.dto

import com.oms.channel.domain.vo.WarehouseStatus
import com.oms.channel.domain.vo.WarehouseType

/**
 * Command to create a new warehouse
 */
data class CreateWarehouseCommand(
    val companyId: String,
    val code: String,
    val name: String,
    val type: WarehouseType,
    val zipCode: String,
    val address1: String,
    val address2: String? = null,
    val city: String,
    val country: String,
    val region: String,
    val capacity: Int
)

/**
 * Command to update a warehouse
 */
data class UpdateWarehouseCommand(
    val name: String? = null,
    val zipCode: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val city: String? = null,
    val country: String? = null,
    val region: String? = null,
    val capacity: Int? = null
)

/**
 * Result DTO for warehouse
 */
data class WarehouseResult(
    val id: String,
    val companyId: String,
    val code: String,
    val name: String,
    val type: WarehouseType,
    val status: WarehouseStatus,
    val zipCode: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val country: String,
    val region: String,
    val capacity: Int,
    val currentStock: Int,
    val availableCapacity: Int,
    val utilizationPercentage: Double,
    val createdAt: String,
    val updatedAt: String
)
