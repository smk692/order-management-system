package com.oms.channel.repository

import com.oms.channel.domain.Warehouse
import java.util.UUID

/**
 * Repository interface for Warehouse aggregate
 */
interface WarehouseRepository {
    /**
     * Save a warehouse
     */
    fun save(warehouse: Warehouse): Warehouse

    /**
     * Find a warehouse by its ID
     */
    fun findById(id: String): Warehouse?

    /**
     * Find all warehouses for a company
     */
    fun findByCompanyId(companyId: UUID): List<Warehouse>

    /**
     * Find warehouses by company ID and region
     */
    fun findByCompanyIdAndRegion(companyId: UUID, region: String): List<Warehouse>
}
