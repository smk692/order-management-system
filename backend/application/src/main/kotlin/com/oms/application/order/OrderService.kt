package com.oms.application.order

import com.oms.catalog.repository.ProductRepository
import com.oms.core.domain.Address
import com.oms.core.domain.Money
import com.oms.core.exception.BusinessRuleException
import com.oms.core.exception.EntityNotFoundException
import com.oms.order.domain.*
import com.oms.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Application service for Order operations
 */
@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) {

    /**
     * Create a new order
     */
    fun createOrder(command: CreateOrderCommand): OrderResult {
        val order = Order.create(
            companyId = command.companyId,
            channelId = command.channelId,
            customer = Customer(
                name = command.customerName,
                phone = command.customerPhone,
                email = command.customerEmail
            ),
            shippingAddress = command.shippingAddress,
            fulfillmentMethod = command.fulfillmentMethod,
            externalOrderId = command.externalOrderId
        )

        // Add items
        command.items.forEach { itemCmd ->
            // Optionally validate product exists
            val product = productRepository.findById(itemCmd.productId)
            val productName = product?.name?.ko ?: itemCmd.productName
            val sku = product?.sku ?: itemCmd.sku

            order.addItem(
                productId = itemCmd.productId,
                productName = productName,
                sku = sku,
                quantity = itemCmd.quantity,
                unitPrice = itemCmd.unitPrice
            )
        }

        val savedOrder = orderRepository.save(order)
        return toOrderResult(savedOrder)
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    fun getOrder(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        return toOrderResult(order)
    }

    /**
     * Get orders by company
     */
    @Transactional(readOnly = true)
    fun getOrdersByCompany(companyId: String): List<OrderResult> {
        return orderRepository.findByCompanyId(companyId)
            .map { toOrderResult(it) }
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    fun getOrdersByStatus(companyId: String, status: OrderStatus): List<OrderResult> {
        return orderRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toOrderResult(it) }
    }

    /**
     * Get orders by date range
     */
    @Transactional(readOnly = true)
    fun getOrdersByDateRange(
        companyId: String,
        startDate: Instant,
        endDate: Instant
    ): List<OrderResult> {
        return orderRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate)
            .map { toOrderResult(it) }
    }

    /**
     * Mark order as paid
     */
    fun markOrderPaid(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.markAsPaid()
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Start preparing order
     */
    fun startPreparingOrder(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.startPreparing()
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Mark order ready to ship
     */
    fun markOrderReadyToShip(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.markReadyToShip()
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Ship order
     */
    fun shipOrder(orderId: String, command: ShipOrderCommand): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.ship(command.carrier, command.trackingNumber)
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Mark order as in delivery
     */
    fun markOrderInDelivery(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.markInDelivery()
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Mark order as delivered
     */
    fun markOrderDelivered(orderId: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.markDelivered()
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Cancel order
     */
    fun cancelOrder(orderId: String, reason: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.cancel(reason)
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Assign order to warehouse
     */
    fun assignToWarehouse(orderId: String, warehouseId: String, routingLogic: String): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        order.assignToWarehouse(warehouseId, routingLogic)
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    /**
     * Add tracking event to shipping
     */
    fun addTrackingEvent(
        orderId: String,
        status: ShippingStatus,
        location: String,
        description: String
    ): OrderResult {
        val order = orderRepository.findById(orderId)
            ?: throw EntityNotFoundException("Order", orderId)

        val shipping = order.shipping
            ?: throw BusinessRuleException("Order has no shipping information")

        shipping.addTrackingEvent(status, location, description)
        val updatedOrder = orderRepository.save(order)

        return toOrderResult(updatedOrder)
    }

    private fun toOrderResult(order: Order): OrderResult {
        return OrderResult(
            id = order.id,
            companyId = order.companyId,
            channelId = order.channelId,
            externalOrderId = order.externalOrderId,
            status = order.status,
            orderDate = order.orderDate,
            customer = CustomerResult(
                name = order.customer.name,
                phone = order.customer.phone,
                email = order.customer.email
            ),
            shippingAddress = order.shippingAddress,
            fulfillmentMethod = order.fulfillmentMethod,
            assignedWarehouseId = order.assignedWarehouseId,
            totalAmount = order.totalAmount,
            items = order.items.map { item ->
                OrderItemResult(
                    id = item.id,
                    productId = item.productId,
                    productName = item.productName,
                    sku = item.sku,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice
                )
            },
            shipping = order.shipping?.let { shipping ->
                ShippingResult(
                    carrier = shipping.carrier,
                    trackingNumber = shipping.trackingNumber,
                    status = shipping.status,
                    shippedAt = shipping.shippedAt,
                    deliveredAt = shipping.deliveredAt
                )
            }
        )
    }
}

/**
 * Command to create an order
 */
data class CreateOrderCommand(
    val companyId: String,
    val channelId: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String? = null,
    val shippingAddress: Address,
    val fulfillmentMethod: FulfillmentMethod = FulfillmentMethod.WMS,
    val externalOrderId: String? = null,
    val items: List<CreateOrderItemCommand>
)

/**
 * Command to create an order item
 */
data class CreateOrderItemCommand(
    val productId: String,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Money
)

/**
 * Command to ship an order
 */
data class ShipOrderCommand(
    val carrier: Carrier,
    val trackingNumber: String
)

/**
 * Result DTO for Order
 */
data class OrderResult(
    val id: String,
    val companyId: String,
    val channelId: String,
    val externalOrderId: String?,
    val status: OrderStatus,
    val orderDate: Instant,
    val customer: CustomerResult,
    val shippingAddress: Address,
    val fulfillmentMethod: FulfillmentMethod,
    val assignedWarehouseId: String?,
    val totalAmount: Money,
    val items: List<OrderItemResult>,
    val shipping: ShippingResult?
)

/**
 * Result DTO for Customer
 */
data class CustomerResult(
    val name: String,
    val phone: String,
    val email: String?
)

/**
 * Result DTO for OrderItem
 */
data class OrderItemResult(
    val id: Long,
    val productId: String,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money
)

/**
 * Result DTO for Shipping
 */
data class ShippingResult(
    val carrier: Carrier,
    val trackingNumber: String,
    val status: ShippingStatus,
    val shippedAt: Instant?,
    val deliveredAt: Instant?
)
