package com.oms.application.channel

import com.oms.application.channel.dto.AssignWarehouseCommand
import com.oms.application.channel.dto.MappingResult
import com.oms.channel.domain.ChannelWarehouseMapping
import com.oms.channel.repository.ChannelRepository
import com.oms.channel.repository.ChannelWarehouseMappingRepository
import com.oms.channel.repository.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for Channel-Warehouse mapping management
 */
@Service
@Transactional
class ChannelWarehouseMappingService(
    private val mappingRepository: ChannelWarehouseMappingRepository,
    private val channelRepository: ChannelRepository,
    private val warehouseRepository: WarehouseRepository
) {

    /**
     * Assign a warehouse to a channel
     */
    fun assignWarehouse(channelId: String, command: AssignWarehouseCommand): MappingResult {
        // Verify channel exists
        val channel = channelRepository.findById(channelId)
            ?: throw IllegalArgumentException("Channel not found: $channelId")

        // Verify warehouse exists
        val warehouse = warehouseRepository.findById(command.warehouseId)
            ?: throw IllegalArgumentException("Warehouse not found: ${command.warehouseId}")

        // Verify both belong to the same company
        require(channel.companyId == warehouse.companyId) {
            "Channel and warehouse must belong to the same company"
        }

        val mapping = ChannelWarehouseMapping.create(
            companyId = channel.companyId,
            channelId = channelId,
            warehouseId = command.warehouseId,
            role = command.role,
            priority = command.priority
        )

        val savedMapping = mappingRepository.save(mapping)
        return toMappingResult(savedMapping)
    }

    /**
     * Get all warehouses assigned to a channel
     */
    @Transactional(readOnly = true)
    fun getWarehousesByChannel(channelId: String): List<MappingResult> {
        return mappingRepository.findByChannelId(channelId)
            .map { toMappingResult(it) }
    }

    /**
     * Get all channels assigned to a warehouse
     */
    @Transactional(readOnly = true)
    fun getChannelsByWarehouse(warehouseId: String): List<MappingResult> {
        return mappingRepository.findByWarehouseId(warehouseId)
            .map { toMappingResult(it) }
    }

    /**
     * Unassign a warehouse from a channel
     */
    fun unassignWarehouse(channelId: String, warehouseId: String) {
        mappingRepository.deleteByChannelIdAndWarehouseId(channelId, warehouseId)
    }

    private fun toMappingResult(mapping: ChannelWarehouseMapping): MappingResult {
        return MappingResult(
            id = mapping.id,
            channelId = mapping.channelId,
            warehouseId = mapping.warehouseId,
            role = mapping.role,
            priority = mapping.priority,
            createdAt = mapping.createdAt.toString()
        )
    }
}
