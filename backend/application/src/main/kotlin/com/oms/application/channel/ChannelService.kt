package com.oms.application.channel

import com.oms.application.channel.dto.ChannelResult
import com.oms.application.channel.dto.CreateChannelCommand
import com.oms.application.channel.dto.UpdateChannelCommand
import com.oms.channel.domain.Channel
import com.oms.channel.domain.vo.ChannelCredentials
import com.oms.channel.domain.vo.ChannelStatus
import com.oms.channel.repository.ChannelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for Channel management
 */
@Service
@Transactional
class ChannelService(
    private val channelRepository: ChannelRepository
) {

    /**
     * Create a new channel
     */
    fun createChannel(command: CreateChannelCommand): ChannelResult {
        val companyId = UUID.fromString(command.companyId)

        // Check if channel name already exists for this company
        require(!channelRepository.existsByNameAndCompanyId(command.name, companyId)) {
            "Channel with name '${command.name}' already exists for this company"
        }

        val credentials = ChannelCredentials(
            apiKey = command.apiKey,
            secretKey = command.secretKey,
            additionalConfig = command.additionalConfig
        )

        val channel = Channel.create(
            companyId = command.companyId,
            name = command.name,
            type = command.type,
            credentials = credentials,
            apiEndpoint = command.apiEndpoint,
            description = command.description
        )

        val savedChannel = channelRepository.save(channel)
        return toChannelResult(savedChannel)
    }

    /**
     * Get channel by ID
     */
    @Transactional(readOnly = true)
    fun getChannel(id: String): ChannelResult {
        val channel = channelRepository.findById(id)
            ?: throw IllegalArgumentException("Channel not found: $id")
        return toChannelResult(channel)
    }

    /**
     * Get all channels for a company
     */
    @Transactional(readOnly = true)
    fun getChannelsByCompany(companyId: UUID): List<ChannelResult> {
        return channelRepository.findByCompanyId(companyId)
            .map { toChannelResult(it) }
    }

    /**
     * Get channels by company and status
     */
    @Transactional(readOnly = true)
    fun getChannelsByCompanyAndStatus(companyId: UUID, status: ChannelStatus): List<ChannelResult> {
        return channelRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toChannelResult(it) }
    }

    /**
     * Update channel
     */
    fun updateChannel(id: String, command: UpdateChannelCommand): ChannelResult {
        val channel = channelRepository.findById(id)
            ?: throw IllegalArgumentException("Channel not found: $id")

        // Update basic info
        channel.update(
            name = command.name,
            apiEndpoint = command.apiEndpoint,
            description = command.description
        )

        // Update credentials if provided
        if (command.apiKey != null && command.secretKey != null) {
            val newCredentials = ChannelCredentials(
                apiKey = command.apiKey,
                secretKey = command.secretKey,
                additionalConfig = command.additionalConfig
            )
            channel.updateCredentials(newCredentials)
        }

        val savedChannel = channelRepository.save(channel)
        return toChannelResult(savedChannel)
    }

    /**
     * Connect a channel
     */
    fun connectChannel(id: String): ChannelResult {
        val channel = channelRepository.findById(id)
            ?: throw IllegalArgumentException("Channel not found: $id")

        channel.connect()
        val savedChannel = channelRepository.save(channel)
        return toChannelResult(savedChannel)
    }

    /**
     * Disconnect a channel
     */
    fun disconnectChannel(id: String): ChannelResult {
        val channel = channelRepository.findById(id)
            ?: throw IllegalArgumentException("Channel not found: $id")

        channel.disconnect()
        val savedChannel = channelRepository.save(channel)
        return toChannelResult(savedChannel)
    }

    private fun toChannelResult(channel: Channel): ChannelResult {
        return ChannelResult(
            id = channel.id,
            companyId = channel.companyId,
            name = channel.name,
            type = channel.type,
            status = channel.status,
            apiEndpoint = channel.apiEndpoint,
            description = channel.description,
            createdAt = channel.createdAt.toString(),
            updatedAt = channel.updatedAt.toString()
        )
    }
}
