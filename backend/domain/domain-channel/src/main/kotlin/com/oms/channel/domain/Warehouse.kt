package com.oms.channel.domain

import com.oms.channel.domain.vo.WarehouseAddress
import com.oms.channel.domain.vo.WarehouseStatus
import com.oms.channel.domain.vo.WarehouseType
import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.event.DomainEvent
import jakarta.persistence.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Warehouse aggregate root
 * Represents a warehouse or fulfillment center in the OMS
 */
@Entity
@Table(
    name = "warehouses",
    indexes = [
        Index(name = "idx_warehouse_company", columnList = "company_id"),
        Index(name = "idx_warehouse_code", columnList = "code"),
        Index(name = "idx_warehouse_type", columnList = "type"),
        Index(name = "idx_warehouse_status", columnList = "status"),
        Index(name = "idx_warehouse_region", columnList = "region")
    ]
)
class Warehouse private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "code", nullable = false, length = 50, unique = true)
    val code: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: WarehouseType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: WarehouseStatus = WarehouseStatus.ACTIVE,

    @Embedded
    var address: WarehouseAddress,

    @Column(name = "region", length = 100)
    var region: String,

    @Column(name = "capacity", nullable = false)
    var capacity: Int,

    @Column(name = "current_stock", nullable = false)
    var currentStock: Int = 0

) : CompanyAwareEntity() {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        fun create(
            companyId: String,
            code: String,
            name: String,
            type: WarehouseType,
            address: WarehouseAddress,
            region: String,
            capacity: Int
        ): Warehouse {
            require(capacity > 0) { "Capacity must be positive" }
            val warehouseId = generateWarehouseId()

            val warehouse = Warehouse(
                id = warehouseId,
                code = code,
                name = name,
                type = type,
                status = WarehouseStatus.ACTIVE,
                address = address,
                region = region,
                capacity = capacity,
                currentStock = 0
            )
            warehouse.assignToCompany(companyId)

            return warehouse
        }

        private fun generateWarehouseId(): String {
            val dateStr = LocalDate.now().format(dateFormatter)
            val uniquePart = UUID.randomUUID().toString().substring(0, 8).uppercase()
            return "WH-$dateStr-$uniquePart"
        }
    }

    /**
     * Activate the warehouse
     */
    fun activate() {
        require(status != WarehouseStatus.ACTIVE) {
            "Warehouse is already active"
        }
        status = WarehouseStatus.ACTIVE
    }

    /**
     * Deactivate the warehouse
     */
    fun deactivate() {
        require(status == WarehouseStatus.ACTIVE) {
            "Warehouse is not active"
        }
        status = WarehouseStatus.INACTIVE
    }

    /**
     * Set warehouse to maintenance mode
     */
    fun setMaintenance() {
        require(status == WarehouseStatus.ACTIVE) {
            "Can only set maintenance mode for active warehouse"
        }
        status = WarehouseStatus.MAINTENANCE
    }

    /**
     * Update warehouse information
     */
    fun update(
        name: String? = null,
        address: WarehouseAddress? = null,
        region: String? = null,
        capacity: Int? = null
    ) {
        name?.let { this.name = it }
        address?.let { this.address = it }
        region?.let { this.region = it }
        capacity?.let {
            require(it > 0) { "Capacity must be positive" }
            this.capacity = it
        }
    }

    /**
     * Update current stock level
     */
    fun updateStock(newStock: Int) {
        require(newStock >= 0) { "Stock cannot be negative" }
        require(newStock <= capacity) { "Stock cannot exceed capacity" }
        this.currentStock = newStock
    }

    /**
     * Check if warehouse has available capacity
     */
    fun hasAvailableCapacity(requiredCapacity: Int = 1): Boolean {
        return (currentStock + requiredCapacity) <= capacity
    }

    /**
     * Get available capacity
     */
    fun getAvailableCapacity(): Int {
        return capacity - currentStock
    }

    /**
     * Get capacity utilization percentage
     */
    fun getUtilizationPercentage(): Double {
        if (capacity == 0) return 0.0
        return (currentStock.toDouble() / capacity.toDouble()) * 100.0
    }

    fun isActive(): Boolean = status == WarehouseStatus.ACTIVE

    fun isInMaintenance(): Boolean = status == WarehouseStatus.MAINTENANCE

    fun isInactive(): Boolean = status == WarehouseStatus.INACTIVE

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
        if (other !is Warehouse) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
