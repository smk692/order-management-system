package com.oms.inventory.repository

import com.oms.inventory.domain.Stock
import com.oms.inventory.domain.vo.StockStatus
import java.util.UUID

interface StockRepository {
    fun save(stock: Stock): Stock
    fun findById(id: UUID): Stock?
    fun findByProductIdAndWarehouseId(productId: String, warehouseId: String): Stock?
    fun findByCompanyId(companyId: UUID): List<Stock>
    fun findByCompanyIdAndStatus(companyId: UUID, status: StockStatus): List<Stock>
    fun findLowStockByCompanyId(companyId: UUID): List<Stock>
    fun delete(stock: Stock)
}
