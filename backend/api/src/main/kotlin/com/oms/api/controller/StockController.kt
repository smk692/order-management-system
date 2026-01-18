package com.oms.api.controller

import com.oms.application.inventory.dto.*
import com.oms.application.inventory.service.StockAllocationService
import com.oms.application.inventory.service.StockService
import com.oms.inventory.domain.vo.StockStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST API controller for Stock management
 */
@RestController
@RequestMapping("/api/v1/stocks")
class StockController(
    private val stockService: StockService,
    private val allocationService: StockAllocationService
) {

    /**
     * Create a new stock entry
     */
    @PostMapping
    fun createStock(@RequestBody command: CreateStockCommand): ResponseEntity<StockResult> {
        val result = stockService.createStock(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Get stock by ID
     */
    @GetMapping("/{id}")
    fun getStock(@PathVariable id: String): ResponseEntity<StockResult> {
        val result = stockService.getStock(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    /**
     * Get stock by product and warehouse
     */
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    fun getStockByProductAndWarehouse(
        @PathVariable productId: String,
        @PathVariable warehouseId: String
    ): ResponseEntity<StockResult> {
        val result = stockService.getStockByProductAndWarehouse(productId, warehouseId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(result)
    }

    /**
     * Get all stocks for a company
     */
    @GetMapping("/company/{companyId}")
    fun getStocksByCompany(@PathVariable companyId: String): ResponseEntity<List<StockResult>> {
        val result = stockService.getStocksByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    /**
     * Get stocks by company and status
     */
    @GetMapping("/company/{companyId}/status/{status}")
    fun getStocksByCompanyAndStatus(
        @PathVariable companyId: String,
        @PathVariable status: StockStatus
    ): ResponseEntity<List<StockResult>> {
        val result = stockService.getStocksByCompanyAndStatus(UUID.fromString(companyId), status)
        return ResponseEntity.ok(result)
    }

    /**
     * Get low stock items for a company
     */
    @GetMapping("/company/{companyId}/low-stock")
    fun getLowStockByCompany(@PathVariable companyId: String): ResponseEntity<List<StockResult>> {
        val result = stockService.getLowStockByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    /**
     * Receive inventory (입고)
     */
    @PostMapping("/{id}/receive")
    fun receiveStock(
        @PathVariable id: String,
        @RequestBody request: ReceiveStockRequest
    ): ResponseEntity<StockResult> {
        val command = ReceiveStockCommand(
            stockId = UUID.fromString(id),
            quantity = request.quantity
        )
        val result = stockService.receiveStock(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Reserve stock for an order
     */
    @PostMapping("/{id}/reserve")
    fun reserveStock(
        @PathVariable id: String,
        @RequestBody request: ReserveStockRequest
    ): ResponseEntity<StockResult> {
        val command = ReserveStockCommand(
            stockId = UUID.fromString(id),
            quantity = request.quantity,
            orderId = request.orderId
        )
        val result = stockService.reserveStock(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Release stock reservation
     */
    @PostMapping("/{id}/release")
    fun releaseStock(
        @PathVariable id: String,
        @RequestBody request: ReleaseStockRequest
    ): ResponseEntity<StockResult> {
        val command = ReleaseStockCommand(
            stockId = UUID.fromString(id),
            quantity = request.quantity
        )
        val result = stockService.releaseStock(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Ship stock (출고)
     */
    @PostMapping("/{id}/ship")
    fun shipStock(
        @PathVariable id: String,
        @RequestBody request: ShipStockRequest
    ): ResponseEntity<StockResult> {
        val command = ShipStockCommand(
            stockId = UUID.fromString(id),
            quantity = request.quantity,
            orderId = request.orderId
        )
        val result = stockService.shipStock(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Manual stock adjustment
     */
    @PostMapping("/{id}/adjust")
    fun adjustStock(
        @PathVariable id: String,
        @RequestBody request: AdjustStockRequest
    ): ResponseEntity<StockResult> {
        val command = AdjustStockCommand(
            stockId = UUID.fromString(id),
            quantity = request.quantity,
            reason = request.reason
        )
        val result = stockService.adjustStock(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Allocate stock to a channel
     */
    @PostMapping("/{id}/allocate")
    fun allocateToChannel(
        @PathVariable id: String,
        @RequestBody request: AllocateToChannelRequest
    ): ResponseEntity<StockResult> {
        val command = AllocateToChannelCommand(
            stockId = UUID.fromString(id),
            channelId = request.channelId,
            quantity = request.quantity
        )
        val result = allocationService.allocateToChannel(command)
        return ResponseEntity.ok(result)
    }

    /**
     * Deallocate stock from a channel
     */
    @PostMapping("/{id}/deallocate")
    fun deallocateFromChannel(
        @PathVariable id: String,
        @RequestBody request: DeallocateFromChannelRequest
    ): ResponseEntity<StockResult> {
        val command = DeallocateFromChannelCommand(
            stockId = UUID.fromString(id),
            channelId = request.channelId,
            quantity = request.quantity
        )
        val result = allocationService.deallocateFromChannel(command)
        return ResponseEntity.ok(result)
    }
}

// Request DTOs for API endpoints
data class ReceiveStockRequest(val quantity: Int)
data class ReserveStockRequest(val quantity: Int, val orderId: String? = null)
data class ReleaseStockRequest(val quantity: Int)
data class ShipStockRequest(val quantity: Int, val orderId: String? = null)
data class AdjustStockRequest(val quantity: Int, val reason: String)
data class AllocateToChannelRequest(val channelId: String, val quantity: Int)
data class DeallocateFromChannelRequest(val channelId: String, val quantity: Int)
