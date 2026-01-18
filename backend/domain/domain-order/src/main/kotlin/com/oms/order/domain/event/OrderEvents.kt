package com.oms.order.domain.event

import com.oms.core.event.DomainEvent
import com.oms.order.domain.OrderStatus

/**
 * Event fired when a new order is created
 */
class OrderCreatedEvent(
    val orderId: String,
    val companyId: String,
    val channelId: String,
    val customerName: String,
    val externalOrderId: String?
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}

/**
 * Event fired when order status changes
 */
class OrderStatusChangedEvent(
    val orderId: String,
    val companyId: String,
    val previousStatus: OrderStatus,
    val newStatus: OrderStatus
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}

/**
 * Event fired when an order is cancelled
 */
class OrderCancelledEvent(
    val orderId: String,
    val companyId: String,
    val previousStatus: OrderStatus,
    val reason: String
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}

/**
 * Event fired when order is assigned to a warehouse
 */
class OrderAssignedToWarehouseEvent(
    val orderId: String,
    val companyId: String,
    val warehouseId: String,
    val routingLogic: String
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}

/**
 * Event fired when an order is shipped
 */
class OrderShippedEvent(
    val orderId: String,
    val companyId: String,
    val carrier: String,
    val trackingNumber: String
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}

/**
 * Event fired when an order is delivered
 */
class OrderDeliveredEvent(
    val orderId: String,
    val companyId: String
) : DomainEvent() {
    override val aggregateId: String = orderId
    override val aggregateType: String = "Order"
}
