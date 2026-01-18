package com.oms.application.inventory.dto

import com.oms.inventory.domain.vo.StockStatus
import java.util.UUID

data class CreateStockCommand(
    val companyId: String,
    val productId: String,
    val warehouseId: String,
    val initialQuantity: Int = 0,
    val safetyStock: Int = 0
)

data class ReceiveStockCommand(
    val stockId: UUID,
    val quantity: Int
)

data class ReserveStockCommand(
    val stockId: UUID,
    val quantity: Int,
    val orderId: String? = null
)

data class ReleaseStockCommand(
    val stockId: UUID,
    val quantity: Int
)

data class ShipStockCommand(
    val stockId: UUID,
    val quantity: Int,
    val orderId: String? = null
)

data class AdjustStockCommand(
    val stockId: UUID,
    val quantity: Int,
    val reason: String
)

data class AllocateToChannelCommand(
    val stockId: UUID,
    val channelId: String,
    val quantity: Int
)

data class DeallocateFromChannelCommand(
    val stockId: UUID,
    val channelId: String,
    val quantity: Int
)

data class StockResult(
    val id: String,
    val companyId: String,
    val productId: String,
    val warehouseId: String,
    val total: Int,
    val available: Int,
    val reserved: Int,
    val safetyStock: Int,
    val status: StockStatus,
    val channelAllocations: Map<String, Int>
)
