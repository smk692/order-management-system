package com.oms.order.domain

import jakarta.persistence.*
import java.time.Instant

/**
 * Shipping child entity
 * Contains shipping information for an order
 */
@Entity
@Table(
    name = "shipping",
    indexes = [
        Index(name = "idx_shipping_order", columnList = "order_id"),
        Index(name = "idx_shipping_tracking", columnList = "tracking_number"),
        Index(name = "idx_shipping_status", columnList = "status")
    ]
)
class Shipping private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Order,

    @Enumerated(EnumType.STRING)
    @Column(name = "carrier", nullable = false, length = 20)
    val carrier: Carrier,

    @Column(name = "tracking_number", nullable = false, length = 100)
    val trackingNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    var status: ShippingStatus = ShippingStatus.PICKED_UP,

    @Column(name = "shipped_at")
    var shippedAt: Instant? = null,

    @Column(name = "delivered_at")
    var deliveredAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {

    // Tracking history
    @OneToMany(mappedBy = "shipping", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _trackingHistory: MutableList<TrackingEvent> = mutableListOf()
    val trackingHistory: List<TrackingEvent> get() = _trackingHistory.toList()

    companion object {
        fun create(order: Order, carrier: Carrier, trackingNumber: String): Shipping {
            require(trackingNumber.isNotBlank()) { "Tracking number cannot be blank" }

            return Shipping(
                order = order,
                carrier = carrier,
                trackingNumber = trackingNumber,
                status = ShippingStatus.PICKED_UP,
                shippedAt = Instant.now()
            )
        }
    }

    /**
     * Add a tracking event
     */
    fun addTrackingEvent(status: ShippingStatus, location: String, description: String) {
        val event = TrackingEvent.create(this, status, location, description)
        _trackingHistory.add(event)
        this.status = status

        if (status == ShippingStatus.DELIVERED) {
            this.deliveredAt = event.timestamp
        }
    }

    /**
     * Mark as in transit
     */
    fun markInTransit(location: String) {
        addTrackingEvent(ShippingStatus.IN_TRANSIT, location, "Package is in transit")
    }

    /**
     * Mark as out for delivery
     */
    fun markOutForDelivery(location: String) {
        addTrackingEvent(ShippingStatus.OUT_FOR_DELIVERY, location, "Package is out for delivery")
    }

    /**
     * Mark as delivered
     */
    fun markDelivered() {
        addTrackingEvent(ShippingStatus.DELIVERED, "Destination", "Package delivered")
    }

    /**
     * Mark as exception
     */
    fun markException(reason: String) {
        addTrackingEvent(ShippingStatus.EXCEPTION, "Unknown", reason)
    }

    fun isDelivered(): Boolean = status == ShippingStatus.DELIVERED

    fun hasException(): Boolean = status == ShippingStatus.EXCEPTION

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shipping) return false
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}

/**
 * Carrier enum
 */
enum class Carrier {
    CJ,      // CJ Logistics
    HANJIN,  // Hanjin
    LOGEN,   // Logen
    POST,    // Korea Post
    FEDEX,   // FedEx
    DHL,     // DHL
    UPS      // UPS
}

/**
 * Shipping status enum
 */
enum class ShippingStatus {
    PICKED_UP,        // Picked up from sender
    IN_TRANSIT,       // In transit
    OUT_FOR_DELIVERY, // Out for delivery
    DELIVERED,        // Delivered
    EXCEPTION         // Exception occurred
}
