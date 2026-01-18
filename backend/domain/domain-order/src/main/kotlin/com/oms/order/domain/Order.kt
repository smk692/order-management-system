package com.oms.order.domain

import com.oms.core.domain.Address
import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.domain.Money
import com.oms.core.event.DomainEvent
import com.oms.order.domain.event.OrderCreatedEvent
import com.oms.order.domain.event.OrderStatusChangedEvent
import com.oms.order.domain.event.OrderCancelledEvent
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Order aggregate root
 * Represents a customer order in the OMS
 */
@Entity
@Table(
    name = "orders",  // 'order' is reserved keyword in SQL
    indexes = [
        Index(name = "idx_order_company", columnList = "company_id"),
        Index(name = "idx_order_channel", columnList = "channel_id"),
        Index(name = "idx_order_status", columnList = "status"),
        Index(name = "idx_order_date", columnList = "order_date"),
        Index(name = "idx_order_external_id", columnList = "external_order_id")
    ]
)
class Order private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "channel_id", nullable = false, length = 36)
    val channelId: String,

    @Column(name = "external_order_id", length = 100)
    val externalOrderId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: OrderStatus = OrderStatus.NEW,

    @Column(name = "order_date", nullable = false)
    val orderDate: Instant,

    // Customer info (embedded value object)
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "name", column = Column(name = "customer_name", nullable = false, length = 100)),
        AttributeOverride(name = "phone", column = Column(name = "customer_phone", nullable = false, length = 20)),
        AttributeOverride(name = "email", column = Column(name = "customer_email", length = 100))
    )
    val customer: Customer,

    // Shipping address (embedded value object)
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "recipient", column = Column(name = "ship_recipient", nullable = false, length = 100)),
        AttributeOverride(name = "phone", column = Column(name = "ship_phone", nullable = false, length = 20)),
        AttributeOverride(name = "zipCode", column = Column(name = "ship_zip_code", length = 10)),
        AttributeOverride(name = "address1", column = Column(name = "ship_address1", nullable = false, length = 200)),
        AttributeOverride(name = "address2", column = Column(name = "ship_address2", length = 200)),
        AttributeOverride(name = "city", column = Column(name = "ship_city", length = 100)),
        AttributeOverride(name = "state", column = Column(name = "ship_state", length = 100)),
        AttributeOverride(name = "country", column = Column(name = "ship_country", nullable = false, length = 50))
    )
    val shippingAddress: Address,

    // Fulfillment
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_method", nullable = false, length = 20)
    val fulfillmentMethod: FulfillmentMethod = FulfillmentMethod.WMS,

    @Column(name = "assigned_warehouse_id", length = 36)
    var assignedWarehouseId: String? = null,

    @Column(name = "routing_logic", length = 200)
    var routingLogic: String? = null,

    // Total amount
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount", nullable = false, precision = 19, scale = 2)),
        AttributeOverride(name = "currency", column = Column(name = "total_currency", nullable = false, length = 3))
    )
    var totalAmount: Money

) : CompanyAwareEntity() {

    // Order items
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _items: MutableList<OrderItem> = mutableListOf()
    val items: List<OrderItem> get() = _items.toList()

    // Shipping info
    @OneToOne(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var shipping: Shipping? = null
        private set

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        fun create(
            companyId: String,
            channelId: String,
            customer: Customer,
            shippingAddress: Address,
            fulfillmentMethod: FulfillmentMethod = FulfillmentMethod.WMS,
            externalOrderId: String? = null
        ): Order {
            val now = Instant.now()
            val orderId = generateOrderId()

            val order = Order(
                id = orderId,
                channelId = channelId,
                externalOrderId = externalOrderId,
                status = OrderStatus.NEW,
                orderDate = now,
                customer = customer,
                shippingAddress = shippingAddress,
                fulfillmentMethod = fulfillmentMethod,
                totalAmount = Money.zero()
            )
            order.assignToCompany(companyId)

            order.registerEvent(
                OrderCreatedEvent(
                    orderId = orderId,
                    companyId = companyId,
                    channelId = channelId,
                    customerName = customer.name,
                    externalOrderId = externalOrderId
                )
            )

            return order
        }

        private fun generateOrderId(): String {
            val dateStr = LocalDate.now().format(dateFormatter)
            val uniquePart = UUID.randomUUID().toString().substring(0, 8).uppercase()
            return "ORD-$dateStr-$uniquePart"
        }
    }

    /**
     * Add an item to the order
     */
    fun addItem(
        productId: String,
        productName: String,
        sku: String,
        quantity: Int,
        unitPrice: Money
    ): OrderItem {
        require(status == OrderStatus.NEW || status == OrderStatus.PAYMENT_PENDING) {
            "Cannot add items to order in status: $status"
        }
        require(quantity > 0) { "Quantity must be positive" }

        val totalPrice = unitPrice.multiply(quantity)
        val item = OrderItem.create(this, productId, productName, sku, quantity, unitPrice, totalPrice)
        _items.add(item)

        recalculateTotal()
        return item
    }

    /**
     * Remove an item from the order
     */
    fun removeItem(itemId: Long) {
        require(status == OrderStatus.NEW || status == OrderStatus.PAYMENT_PENDING) {
            "Cannot remove items from order in status: $status"
        }

        val item = _items.find { it.id == itemId }
            ?: throw IllegalArgumentException("Item not found: $itemId")

        _items.remove(item)
        recalculateTotal()
    }

    /**
     * Recalculate total amount from items
     */
    fun recalculateTotal() {
        if (_items.isEmpty()) {
            totalAmount = Money.zero(totalAmount.currency)
            return
        }

        val sum = _items.fold(java.math.BigDecimal.ZERO) { acc, item ->
            acc.add(item.totalPrice.amount)
        }
        totalAmount = Money(amount = sum, currency = totalAmount.currency)
    }

    /**
     * Check if status transition is allowed
     */
    fun canTransitionTo(newStatus: OrderStatus): Boolean {
        return OrderStatusMachine.canTransition(status, newStatus)
    }

    /**
     * Transition to new status
     */
    fun transitionTo(newStatus: OrderStatus) {
        require(canTransitionTo(newStatus)) {
            "Cannot transition from $status to $newStatus"
        }
        val previousStatus = status
        status = newStatus

        registerEvent(
            OrderStatusChangedEvent(
                orderId = id,
                companyId = companyId,
                previousStatus = previousStatus,
                newStatus = newStatus
            )
        )
    }

    /**
     * Assign order to a warehouse
     */
    fun assignToWarehouse(warehouseId: String, logic: String) {
        require(fulfillmentMethod == FulfillmentMethod.WMS) {
            "Can only assign warehouse for WMS fulfillment"
        }
        this.assignedWarehouseId = warehouseId
        this.routingLogic = logic
    }

    /**
     * Mark order as paid
     */
    fun markAsPaid() {
        transitionTo(OrderStatus.PAID)
    }

    /**
     * Start preparing the order
     */
    fun startPreparing() {
        transitionTo(OrderStatus.PREPARING)
    }

    /**
     * Mark order as ready to ship
     */
    fun markReadyToShip() {
        transitionTo(OrderStatus.READY_TO_SHIP)
    }

    /**
     * Create shipping and mark as shipped
     */
    fun ship(carrier: Carrier, trackingNumber: String) {
        require(status == OrderStatus.READY_TO_SHIP) {
            "Order must be ready to ship"
        }
        require(trackingNumber.isNotBlank()) { "Tracking number is required" }

        this.shipping = Shipping.create(this, carrier, trackingNumber)
        transitionTo(OrderStatus.SHIPPED)
    }

    /**
     * Mark as in delivery
     */
    fun markInDelivery() {
        transitionTo(OrderStatus.IN_DELIVERY)
    }

    /**
     * Mark as delivered
     */
    fun markDelivered() {
        transitionTo(OrderStatus.DELIVERED)
        shipping?.markDelivered()
    }

    /**
     * Cancel the order
     */
    fun cancel(reason: String) {
        require(canTransitionTo(OrderStatus.CANCELLED)) {
            "Cannot cancel order in status: $status"
        }

        val previousStatus = status
        status = OrderStatus.CANCELLED

        registerEvent(
            OrderCancelledEvent(
                orderId = id,
                companyId = companyId,
                previousStatus = previousStatus,
                reason = reason
            )
        )
    }

    /**
     * Request exchange
     */
    fun requestExchange() {
        transitionTo(OrderStatus.EXCHANGE_REQUESTED)
    }

    /**
     * Request return
     */
    fun requestReturn() {
        transitionTo(OrderStatus.RETURN_REQUESTED)
    }

    fun isCompleted(): Boolean = status == OrderStatus.DELIVERED

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    fun canBeModified(): Boolean = status in listOf(OrderStatus.NEW, OrderStatus.PAYMENT_PENDING)

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
        if (other !is Order) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Order status enum
 */
enum class OrderStatus {
    NEW,
    PAYMENT_PENDING,
    PAID,
    PREPARING,
    READY_TO_SHIP,
    SHIPPED,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED,
    EXCHANGE_REQUESTED,
    RETURN_REQUESTED
}

/**
 * Fulfillment method enum
 */
enum class FulfillmentMethod {
    WMS,    // Warehouse Management System
    DIRECT  // Direct fulfillment from vendor
}

/**
 * Order status state machine
 */
object OrderStatusMachine {
    private val transitions = mapOf(
        OrderStatus.NEW to setOf(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
        OrderStatus.PAYMENT_PENDING to setOf(OrderStatus.PAID, OrderStatus.CANCELLED),
        OrderStatus.PAID to setOf(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING to setOf(OrderStatus.READY_TO_SHIP, OrderStatus.CANCELLED),
        OrderStatus.READY_TO_SHIP to setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED to setOf(OrderStatus.IN_DELIVERY),
        OrderStatus.IN_DELIVERY to setOf(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED to setOf(OrderStatus.EXCHANGE_REQUESTED, OrderStatus.RETURN_REQUESTED)
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean {
        return transitions[from]?.contains(to) ?: false
    }

    fun getAvailableTransitions(from: OrderStatus): Set<OrderStatus> {
        return transitions[from] ?: emptySet()
    }
}
