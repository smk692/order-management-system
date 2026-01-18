package com.oms.application.channel

import com.oms.application.channel.dto.CreateWarehouseCommand
import com.oms.application.channel.dto.UpdateWarehouseCommand
import com.oms.application.channel.dto.WarehouseResult
import com.oms.channel.domain.Warehouse
import com.oms.channel.domain.vo.WarehouseAddress
import com.oms.channel.repository.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for Warehouse management
 */
@Service
@Transactional
class WarehouseService(
    private val warehouseRepository: WarehouseRepository
) {

    /**
     * Create a new warehouse
     */
    fun createWarehouse(command: CreateWarehouseCommand): WarehouseResult {
        val address = WarehouseAddress(
            zipCode = command.zipCode,
            address1 = command.address1,
            address2 = command.address2,
            city = command.city,
            country = command.country
        )

        val warehouse = Warehouse.create(
            companyId = command.companyId,
            code = command.code,
            name = command.name,
            type = command.type,
            address = address,
            region = command.region,
            capacity = command.capacity
        )

        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    /**
     * Get warehouse by ID
     */
    @Transactional(readOnly = true)
    fun getWarehouse(id: String): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")
        return toWarehouseResult(warehouse)
    }

    /**
     * Get all warehouses for a company
     */
    @Transactional(readOnly = true)
    fun getWarehousesByCompany(companyId: UUID): List<WarehouseResult> {
        return warehouseRepository.findByCompanyId(companyId)
            .map { toWarehouseResult(it) }
    }

    /**
     * Get warehouses by company and region
     */
    @Transactional(readOnly = true)
    fun getWarehousesByCompanyAndRegion(companyId: UUID, region: String): List<WarehouseResult> {
        return warehouseRepository.findByCompanyIdAndRegion(companyId, region)
            .map { toWarehouseResult(it) }
    }

    /**
     * Update warehouse
     */
    fun updateWarehouse(id: String, command: UpdateWarehouseCommand): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")

        // Update address if any address field is provided
        val newAddress = if (command.zipCode != null || command.address1 != null ||
            command.city != null || command.country != null) {
            WarehouseAddress(
                zipCode = command.zipCode ?: warehouse.address.zipCode,
                address1 = command.address1 ?: warehouse.address.address1,
                address2 = command.address2 ?: warehouse.address.address2,
                city = command.city ?: warehouse.address.city,
                country = command.country ?: warehouse.address.country
            )
        } else null

        warehouse.update(
            name = command.name,
            address = newAddress,
            region = command.region,
            capacity = command.capacity
        )

        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    /**
     * Activate a warehouse
     */
    fun activateWarehouse(id: String): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")

        warehouse.activate()
        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    /**
     * Deactivate a warehouse
     */
    fun deactivateWarehouse(id: String): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")

        warehouse.deactivate()
        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    /**
     * Set warehouse to maintenance mode
     */
    fun setWarehouseMaintenance(id: String): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")

        warehouse.setMaintenance()
        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    /**
     * Update warehouse stock level
     */
    fun updateStock(id: String, newStock: Int): WarehouseResult {
        val warehouse = warehouseRepository.findById(id)
            ?: throw IllegalArgumentException("Warehouse not found: $id")

        warehouse.updateStock(newStock)
        val savedWarehouse = warehouseRepository.save(warehouse)
        return toWarehouseResult(savedWarehouse)
    }

    private fun toWarehouseResult(warehouse: Warehouse): WarehouseResult {
        return WarehouseResult(
            id = warehouse.id,
            companyId = warehouse.companyId,
            code = warehouse.code,
            name = warehouse.name,
            type = warehouse.type,
            status = warehouse.status,
            zipCode = warehouse.address.zipCode,
            address1 = warehouse.address.address1,
            address2 = warehouse.address.address2,
            city = warehouse.address.city,
            country = warehouse.address.country,
            region = warehouse.region,
            capacity = warehouse.capacity,
            currentStock = warehouse.currentStock,
            availableCapacity = warehouse.getAvailableCapacity(),
            utilizationPercentage = warehouse.getUtilizationPercentage(),
            createdAt = warehouse.createdAt.toString(),
            updatedAt = warehouse.updatedAt.toString()
        )
    }
}
