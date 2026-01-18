package com.oms.application.channel.dto

import com.oms.channel.domain.vo.ChannelStatus
import com.oms.channel.domain.vo.ChannelType

/**
 * Command to create a new channel
 */
data class CreateChannelCommand(
    val companyId: String,
    val name: String,
    val type: ChannelType,
    val apiKey: String,
    val secretKey: String,
    val additionalConfig: String? = null,
    val apiEndpoint: String? = null,
    val description: String? = null
)

/**
 * Command to update a channel
 */
data class UpdateChannelCommand(
    val name: String? = null,
    val apiKey: String? = null,
    val secretKey: String? = null,
    val additionalConfig: String? = null,
    val apiEndpoint: String? = null,
    val description: String? = null
)

/**
 * Result DTO for channel
 */
data class ChannelResult(
    val id: String,
    val companyId: String,
    val name: String,
    val type: ChannelType,
    val status: ChannelStatus,
    val apiEndpoint: String?,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)
