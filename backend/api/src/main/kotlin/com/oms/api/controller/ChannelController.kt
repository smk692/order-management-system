package com.oms.api.controller

import com.oms.application.channel.ChannelService
import com.oms.application.channel.ChannelWarehouseMappingService
import com.oms.application.channel.dto.AssignWarehouseCommand
import com.oms.application.channel.dto.ChannelResult
import com.oms.application.channel.dto.CreateChannelCommand
import com.oms.application.channel.dto.MappingResult
import com.oms.application.channel.dto.UpdateChannelCommand
import com.oms.channel.domain.vo.ChannelStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST API controller for Channel management
 */
@RestController
@RequestMapping("/api/v1/channels")
class ChannelController(
    private val channelService: ChannelService,
    private val mappingService: ChannelWarehouseMappingService
) {

    /**
     * Create a new channel
     */
    @PostMapping
    fun createChannel(@RequestBody command: CreateChannelCommand): ResponseEntity<ChannelResult> {
        val result = channelService.createChannel(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Get channel by ID
     */
    @GetMapping("/{id}")
    fun getChannel(@PathVariable id: String): ResponseEntity<ChannelResult> {
        val result = channelService.getChannel(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Get all channels for a company
     */
    @GetMapping("/company/{companyId}")
    fun getChannelsByCompany(@PathVariable companyId: String): ResponseEntity<List<ChannelResult>> {
        val result = channelService.getChannelsByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    /**
     * Get channels by company and status
     */
    @GetMapping("/company/{companyId}/status/{status}")
    fun getChannelsByCompanyAndStatus(
        @PathVariable companyId: String,
        @PathVariable status: ChannelStatus
    ): ResponseEntity<List<ChannelResult>> {
        val result = channelService.getChannelsByCompanyAndStatus(UUID.fromString(companyId), status)
        return ResponseEntity.ok(result)
    }

    /**
     * Update channel
     */
    @PutMapping("/{id}")
    fun updateChannel(
        @PathVariable id: String,
        @RequestBody command: UpdateChannelCommand
    ): ResponseEntity<ChannelResult> {
        val result = channelService.updateChannel(id, command)
        return ResponseEntity.ok(result)
    }

    /**
     * Connect channel
     */
    @PostMapping("/{id}/connect")
    fun connectChannel(@PathVariable id: String): ResponseEntity<ChannelResult> {
        val result = channelService.connectChannel(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Disconnect channel
     */
    @PostMapping("/{id}/disconnect")
    fun disconnectChannel(@PathVariable id: String): ResponseEntity<ChannelResult> {
        val result = channelService.disconnectChannel(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Assign a warehouse to a channel
     */
    @PostMapping("/{id}/warehouses")
    fun assignWarehouse(
        @PathVariable id: String,
        @RequestBody command: AssignWarehouseCommand
    ): ResponseEntity<MappingResult> {
        val result = mappingService.assignWarehouse(id, command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Get all warehouses assigned to a channel
     */
    @GetMapping("/{id}/warehouses")
    fun getWarehousesByChannel(@PathVariable id: String): ResponseEntity<List<MappingResult>> {
        val result = mappingService.getWarehousesByChannel(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Unassign a warehouse from a channel
     */
    @DeleteMapping("/{id}/warehouses/{warehouseId}")
    fun unassignWarehouse(
        @PathVariable id: String,
        @PathVariable warehouseId: String
    ): ResponseEntity<Void> {
        mappingService.unassignWarehouse(id, warehouseId)
        return ResponseEntity.noContent().build()
    }
}
