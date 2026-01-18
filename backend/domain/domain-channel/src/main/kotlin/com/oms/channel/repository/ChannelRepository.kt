package com.oms.channel.repository

import com.oms.channel.domain.Channel
import com.oms.channel.domain.vo.ChannelStatus
import java.util.UUID

/**
 * Repository interface for Channel aggregate
 */
interface ChannelRepository {
    /**
     * Save a channel
     */
    fun save(channel: Channel): Channel

    /**
     * Find a channel by its ID
     */
    fun findById(id: String): Channel?

    /**
     * Find all channels for a company
     */
    fun findByCompanyId(companyId: UUID): List<Channel>

    /**
     * Find channels by company ID and status
     */
    fun findByCompanyIdAndStatus(companyId: UUID, status: ChannelStatus): List<Channel>

    /**
     * Check if a channel with the given name exists for a company
     */
    fun existsByNameAndCompanyId(name: String, companyId: UUID): Boolean
}
