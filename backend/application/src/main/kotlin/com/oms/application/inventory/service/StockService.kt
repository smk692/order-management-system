package com.oms.application.inventory.service

import com.oms.application.inventory.dto.*
import com.oms.inventory.domain.Stock
import com.oms.inventory.domain.StockMovement
import com.oms.inventory.domain.vo.MovementType
import com.oms.inventory.domain.vo.StockStatus
import com.oms.inventory.repository.StockRepository
import com.oms.inventory.repository.StockMovementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for Stock management
 */
@Service
@Transactional
class StockService(
    private val stockRepository: StockRepository,
    private val movementRepository: StockMovementRepository
) {

    /**
     * Create a new stock entry
     */
    fun createStock(command: CreateStockCommand): StockResult {
        // Check if stock already exists for this product and warehouse
        val existing = stockRepository.findByProductIdAndWarehouseId(
            command.productId,
            command.warehouseId
        )
        require(existing == null) {
            "Stock already exists for product ${command.productId} in warehouse ${command.warehouseId}"
        }

        val stock = Stock.create(
            companyId = command.companyId,
            productId = command.productId,
            warehouseId = command.warehouseId,
            initialQuantity = command.initialQuantity,
            safetyStock = command.safetyStock
        )

        val savedStock = stockRepository.save(stock)

        // Record initial stock if quantity > 0
        if (command.initialQuantity > 0) {
            recordMovement(
                stock = savedStock,
                type = MovementType.RECEIVE,
                quantity = command.initialQuantity,
                beforeTotal = 0,
                afterTotal = command.initialQuantity,
                reason = "Initial stock"
            )
        }

        return toStockResult(savedStock)
    }

    /**
     * Get stock by ID
     */
    @Transactional(readOnly = true)
    fun getStock(stockId: UUID): StockResult {
        val stock = stockRepository.findById(stockId)
            ?: throw IllegalArgumentException("Stock not found: $stockId")
        return toStockResult(stock)
    }

    /**
     * Get stock by product and warehouse
     */
    @Transactional(readOnly = true)
    fun getStockByProductAndWarehouse(productId: String, warehouseId: String): StockResult? {
        val stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            ?: return null
        return toStockResult(stock)
    }

    /**
     * Get all stocks for a company
     */
    @Transactional(readOnly = true)
    fun getStocksByCompany(companyId: UUID): List<StockResult> {
        return stockRepository.findByCompanyId(companyId)
            .map { toStockResult(it) }
    }

    /**
     * Get stocks by status
     */
    @Transactional(readOnly = true)
    fun getStocksByCompanyAndStatus(companyId: UUID, status: StockStatus): List<StockResult> {
        return stockRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toStockResult(it) }
    }

    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    fun getLowStockByCompany(companyId: UUID): List<StockResult> {
        return stockRepository.findLowStockByCompanyId(companyId)
            .map { toStockResult(it) }
    }

    /**
     * Receive inventory (입고)
     */
    fun receiveStock(command: ReceiveStockCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        val beforeTotal = stock.total
        stock.receive(command.quantity)
        val savedStock = stockRepository.save(stock)

        recordMovement(
            stock = savedStock,
            type = MovementType.RECEIVE,
            quantity = command.quantity,
            beforeTotal = beforeTotal,
            afterTotal = savedStock.total,
            reason = "Stock received"
        )

        return toStockResult(savedStock)
    }

    /**
     * Reserve stock for an order
     */
    fun reserveStock(command: ReserveStockCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        val beforeTotal = stock.total
        stock.reserve(command.quantity, command.orderId)
        val savedStock = stockRepository.save(stock)

        recordMovement(
            stock = savedStock,
            type = MovementType.RESERVE,
            quantity = command.quantity,
            beforeTotal = beforeTotal,
            afterTotal = savedStock.total,
            referenceId = command.orderId,
            reason = "Reserved for order"
        )

        return toStockResult(savedStock)
    }

    /**
     * Release stock reservation
     */
    fun releaseStock(command: ReleaseStockCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        val beforeTotal = stock.total
        stock.release(command.quantity)
        val savedStock = stockRepository.save(stock)

        recordMovement(
            stock = savedStock,
            type = MovementType.RELEASE,
            quantity = command.quantity,
            beforeTotal = beforeTotal,
            afterTotal = savedStock.total,
            reason = "Reservation released"
        )

        return toStockResult(savedStock)
    }

    /**
     * Ship stock (출고)
     */
    fun shipStock(command: ShipStockCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        val beforeTotal = stock.total
        stock.ship(command.quantity, command.orderId)
        val savedStock = stockRepository.save(stock)

        recordMovement(
            stock = savedStock,
            type = MovementType.SHIP,
            quantity = command.quantity,
            beforeTotal = beforeTotal,
            afterTotal = savedStock.total,
            referenceId = command.orderId,
            reason = "Stock shipped"
        )

        return toStockResult(savedStock)
    }

    /**
     * Adjust stock manually
     */
    fun adjustStock(command: AdjustStockCommand): StockResult {
        val stock = stockRepository.findById(command.stockId)
            ?: throw IllegalArgumentException("Stock not found: ${command.stockId}")

        val beforeTotal = stock.total
        stock.adjust(command.quantity, command.reason)
        val savedStock = stockRepository.save(stock)

        recordMovement(
            stock = savedStock,
            type = MovementType.ADJUST,
            quantity = command.quantity,
            beforeTotal = beforeTotal,
            afterTotal = savedStock.total,
            reason = command.reason
        )

        return toStockResult(savedStock)
    }

    private fun recordMovement(
        stock: Stock,
        type: MovementType,
        quantity: Int,
        beforeTotal: Int,
        afterTotal: Int,
        referenceId: String? = null,
        reason: String? = null
    ) {
        val movement = StockMovement.record(
            companyId = stock.companyId,
            stockId = UUID.fromString(stock.id),
            type = type,
            quantity = quantity,
            beforeTotal = beforeTotal,
            afterTotal = afterTotal,
            referenceId = referenceId,
            reason = reason
        )
        movementRepository.save(movement)
    }

    private fun toStockResult(stock: Stock): StockResult {
        return StockResult(
            id = stock.id,
            companyId = stock.companyId,
            productId = stock.productId,
            warehouseId = stock.warehouseId,
            total = stock.total,
            available = stock.available,
            reserved = stock.reserved,
            safetyStock = stock.safetyStock,
            status = stock.status,
            channelAllocations = stock.channelAllocations.toMap()
        )
    }
}
