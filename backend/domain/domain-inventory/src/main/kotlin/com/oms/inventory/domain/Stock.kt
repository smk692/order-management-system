package com.oms.inventory.domain

import com.oms.inventory.domain.vo.StockStatus
import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.event.DomainEvent
import com.oms.inventory.domain.event.StockEvent
import jakarta.persistence.*
import java.util.UUID

/**
 * Stock aggregate root
 * Represents inventory stock in a warehouse
 */
@Entity
@Table(
    name = "stocks",
    indexes = [
        Index(name = "idx_stock_company", columnList = "company_id"),
        Index(name = "idx_stock_product_warehouse", columnList = "product_id,warehouse_id"),
        Index(name = "idx_stock_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_stock_product_warehouse", columnNames = ["product_id", "warehouse_id"])
    ]
)
class Stock private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "product_id", nullable = false, length = 100)
    val productId: String,

    @Column(name = "warehouse_id", nullable = false, length = 100)
    val warehouseId: String,

    @Column(name = "total", nullable = false)
    var total: Int = 0,

    @Column(name = "available", nullable = false)
    var available: Int = 0,

    @Column(name = "reserved", nullable = false)
    var reserved: Int = 0,

    @Column(name = "safety_stock", nullable = false)
    var safetyStock: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: StockStatus = StockStatus.NORMAL

) : CompanyAwareEntity() {

    @ElementCollection
    @CollectionTable(
        name = "stock_channel_allocations",
        joinColumns = [JoinColumn(name = "stock_id")]
    )
    @MapKeyColumn(name = "channel_id")
    @Column(name = "quantity")
    val channelAllocations: MutableMap<String, Int> = mutableMapOf()

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(
            companyId: String,
            productId: String,
            warehouseId: String,
            initialQuantity: Int = 0,
            safetyStock: Int = 0
        ): Stock {
            require(initialQuantity >= 0) { "Initial quantity cannot be negative" }
            require(safetyStock >= 0) { "Safety stock cannot be negative" }

            val stockId = UUID.randomUUID().toString()
            val stock = Stock(
                id = stockId,
                productId = productId,
                warehouseId = warehouseId,
                total = initialQuantity,
                available = initialQuantity,
                reserved = 0,
                safetyStock = safetyStock,
                status = StockStatus.NORMAL
            )
            stock.assignToCompany(companyId)
            stock.updateStatus()
            stock.registerEvent(
                StockEvent.StockCreatedEvent(
                    stockId = UUID.fromString(stockId),
                    productId = productId,
                    warehouseId = warehouseId
                )
            )
            return stock
        }
    }

    /**
     * Receive inventory (입고)
     */
    fun receive(quantity: Int) {
        require(quantity > 0) { "Receive quantity must be positive" }

        val beforeTotal = total
        total += quantity
        available += quantity

        updateStatus()
        registerEvent(
            StockEvent.StockReceivedEvent(
                stockId = UUID.fromString(id),
                quantity = quantity,
                newTotal = total
            )
        )
    }

    /**
     * Reserve stock (예약 - available → reserved)
     */
    fun reserve(quantity: Int, orderId: String? = null) {
        require(quantity > 0) { "Reserve quantity must be positive" }
        require(available >= quantity) {
            "Insufficient available stock. Required: $quantity, Available: $available"
        }

        available -= quantity
        reserved += quantity

        updateStatus()
        registerEvent(
            StockEvent.StockReservedEvent(
                stockId = UUID.fromString(id),
                quantity = quantity,
                orderId = orderId
            )
        )
    }

    /**
     * Release reservation (예약 해제 - reserved → available)
     */
    fun release(quantity: Int) {
        require(quantity > 0) { "Release quantity must be positive" }
        require(reserved >= quantity) {
            "Insufficient reserved stock. Required: $quantity, Reserved: $reserved"
        }

        reserved -= quantity
        available += quantity

        updateStatus()
        registerEvent(
            StockEvent.StockReleasedEvent(
                stockId = UUID.fromString(id),
                quantity = quantity
            )
        )
    }

    /**
     * Ship stock (출고 - reserved → 감소)
     */
    fun ship(quantity: Int, orderId: String? = null) {
        require(quantity > 0) { "Ship quantity must be positive" }
        require(reserved >= quantity) {
            "Insufficient reserved stock. Required: $quantity, Reserved: $reserved"
        }

        reserved -= quantity
        total -= quantity

        updateStatus()
        registerEvent(
            StockEvent.StockShippedEvent(
                stockId = UUID.fromString(id),
                quantity = quantity,
                orderId = orderId
            )
        )
    }

    /**
     * Adjust stock manually (재고 조정)
     */
    fun adjust(quantity: Int, reason: String) {
        require(reason.isNotBlank()) { "Adjustment reason is required" }

        val beforeTotal = total
        total += quantity
        available += quantity

        // Ensure non-negative values
        require(total >= 0) { "Total stock cannot be negative after adjustment" }
        require(available >= 0) { "Available stock cannot be negative after adjustment" }

        updateStatus()
        registerEvent(
            StockEvent.StockAdjustedEvent(
                stockId = UUID.fromString(id),
                quantity = quantity,
                reason = reason
            )
        )
    }

    /**
     * Allocate stock to a specific channel
     */
    fun allocateToChannel(channelId: String, quantity: Int) {
        require(quantity > 0) { "Allocation quantity must be positive" }
        require(available >= quantity) {
            "Insufficient available stock for allocation. Required: $quantity, Available: $available"
        }

        val currentAllocation = channelAllocations.getOrDefault(channelId, 0)
        channelAllocations[channelId] = currentAllocation + quantity
        available -= quantity
    }

    /**
     * Deallocate stock from a specific channel
     */
    fun deallocateFromChannel(channelId: String, quantity: Int) {
        require(quantity > 0) { "Deallocation quantity must be positive" }
        val currentAllocation = channelAllocations.getOrDefault(channelId, 0)
        require(currentAllocation >= quantity) {
            "Insufficient allocated stock. Required: $quantity, Allocated: $currentAllocation"
        }

        channelAllocations[channelId] = currentAllocation - quantity
        if (channelAllocations[channelId] == 0) {
            channelAllocations.remove(channelId)
        }
        available += quantity
    }

    /**
     * Update stock status based on current levels
     */
    private fun updateStatus() {
        status = when {
            total <= 0 -> StockStatus.OUT_OF_STOCK
            available < safetyStock -> {
                // Emit low stock alert
                registerEvent(
                    StockEvent.LowStockAlertEvent(
                        stockId = UUID.fromString(id),
                        productId = productId,
                        available = available,
                        safetyStock = safetyStock
                    )
                )
                StockStatus.LOW
            }
            total > safetyStock * 3 -> StockStatus.OVERSTOCK
            else -> StockStatus.NORMAL
        }
    }

    fun isOutOfStock(): Boolean = status == StockStatus.OUT_OF_STOCK

    fun isLowStock(): Boolean = status == StockStatus.LOW

    fun hasAvailableStock(): Boolean = available > 0

    private fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun clearEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Stock) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
