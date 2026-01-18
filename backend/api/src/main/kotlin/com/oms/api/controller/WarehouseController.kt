package com.oms.api.controller

import com.oms.application.channel.WarehouseService
import com.oms.application.channel.dto.CreateWarehouseCommand
import com.oms.application.channel.dto.UpdateWarehouseCommand
import com.oms.application.channel.dto.WarehouseResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST API controller for Warehouse management
 */
@RestController
@RequestMapping("/api/v1/warehouses")
class WarehouseController(
    private val warehouseService: WarehouseService
) {

    /**
     * Create a new warehouse
     */
    @PostMapping
    fun createWarehouse(@RequestBody command: CreateWarehouseCommand): ResponseEntity<WarehouseResult> {
        val result = warehouseService.createWarehouse(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Get warehouse by ID
     */
    @GetMapping("/{id}")
    fun getWarehouse(@PathVariable id: String): ResponseEntity<WarehouseResult> {
        val result = warehouseService.getWarehouse(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Get all warehouses for a company
     */
    @GetMapping("/company/{companyId}")
    fun getWarehousesByCompany(@PathVariable companyId: String): ResponseEntity<List<WarehouseResult>> {
        val result = warehouseService.getWarehousesByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    /**
     * Get warehouses by company and region
     */
    @GetMapping("/company/{companyId}/region/{region}")
    fun getWarehousesByCompanyAndRegion(
        @PathVariable companyId: String,
        @PathVariable region: String
    ): ResponseEntity<List<WarehouseResult>> {
        val result = warehouseService.getWarehousesByCompanyAndRegion(UUID.fromString(companyId), region)
        return ResponseEntity.ok(result)
    }

    /**
     * Update warehouse
     */
    @PutMapping("/{id}")
    fun updateWarehouse(
        @PathVariable id: String,
        @RequestBody command: UpdateWarehouseCommand
    ): ResponseEntity<WarehouseResult> {
        val result = warehouseService.updateWarehouse(id, command)
        return ResponseEntity.ok(result)
    }

    /**
     * Activate warehouse
     */
    @PostMapping("/{id}/activate")
    fun activateWarehouse(@PathVariable id: String): ResponseEntity<WarehouseResult> {
        val result = warehouseService.activateWarehouse(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Deactivate warehouse
     */
    @PostMapping("/{id}/deactivate")
    fun deactivateWarehouse(@PathVariable id: String): ResponseEntity<WarehouseResult> {
        val result = warehouseService.deactivateWarehouse(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Set warehouse to maintenance mode
     */
    @PostMapping("/{id}/maintenance")
    fun setWarehouseMaintenance(@PathVariable id: String): ResponseEntity<WarehouseResult> {
        val result = warehouseService.setWarehouseMaintenance(id)
        return ResponseEntity.ok(result)
    }

    /**
     * Update warehouse stock level
     */
    @PutMapping("/{id}/stock")
    fun updateStock(
        @PathVariable id: String,
        @RequestParam newStock: Int
    ): ResponseEntity<WarehouseResult> {
        val result = warehouseService.updateStock(id, newStock)
        return ResponseEntity.ok(result)
    }
}
