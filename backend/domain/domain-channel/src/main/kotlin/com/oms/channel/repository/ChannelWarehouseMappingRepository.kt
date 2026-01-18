package com.oms.channel.repository

import com.oms.channel.domain.ChannelWarehouseMapping

/**
 * Repository interface for ChannelWarehouseMapping aggregate
 */
interface ChannelWarehouseMappingRepository {
    /**
     * Save a channel-warehouse mapping
     */
    fun save(mapping: ChannelWarehouseMapping): ChannelWarehouseMapping

    /**
     * Find all mappings for a channel
     */
    fun findByChannelId(channelId: String): List<ChannelWarehouseMapping>

    /**
     * Find all mappings for a warehouse
     */
    fun findByWarehouseId(warehouseId: String): List<ChannelWarehouseMapping>

    /**
     * Delete a mapping between a channel and warehouse
     */
    fun deleteByChannelIdAndWarehouseId(channelId: String, warehouseId: String)
}
