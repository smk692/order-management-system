package com.oms.inventory.repository

import com.oms.inventory.domain.StockMovement
import java.util.UUID

interface StockMovementRepository {
    fun save(movement: StockMovement): StockMovement
    fun findByStockId(stockId: UUID): List<StockMovement>
    fun findByReferenceId(referenceId: String): List<StockMovement>
}
