package com.oms.inventory.domain.event

import com.oms.core.event.DomainEvent
import java.util.UUID

sealed class StockEvent : DomainEvent() {

    data class StockCreatedEvent(
        val stockId: UUID,
        val productId: String,
        val warehouseId: String
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class StockReceivedEvent(
        val stockId: UUID,
        val quantity: Int,
        val newTotal: Int
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class StockReservedEvent(
        val stockId: UUID,
        val quantity: Int,
        val orderId: String?
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class StockReleasedEvent(
        val stockId: UUID,
        val quantity: Int
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class StockShippedEvent(
        val stockId: UUID,
        val quantity: Int,
        val orderId: String?
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class StockAdjustedEvent(
        val stockId: UUID,
        val quantity: Int,
        val reason: String
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }

    data class LowStockAlertEvent(
        val stockId: UUID,
        val productId: String,
        val available: Int,
        val safetyStock: Int
    ) : StockEvent() {
        override val aggregateId: String = stockId.toString()
        override val aggregateType: String = "Stock"
    }
}
