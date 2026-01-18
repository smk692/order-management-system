package com.oms.order.domain

import jakarta.persistence.*
import java.time.Instant

/**
 * TrackingEvent entity
 * Represents a shipping tracking event
 */
@Entity
@Table(
    name = "tracking_event",
    indexes = [
        Index(name = "idx_tracking_shipping", columnList = "shipping_id"),
        Index(name = "idx_tracking_timestamp", columnList = "timestamp")
    ]
)
class TrackingEvent private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_id", nullable = false)
    val shipping: Shipping,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    val status: ShippingStatus,

    @Column(name = "location", nullable = false, length = 200)
    val location: String,

    @Column(name = "description", nullable = false, length = 500)
    val description: String,

    @Column(name = "timestamp", nullable = false)
    val timestamp: Instant = Instant.now()
) {

    companion object {
        fun create(
            shipping: Shipping,
            status: ShippingStatus,
            location: String,
            description: String
        ): TrackingEvent {
            require(location.isNotBlank()) { "Location cannot be blank" }
            require(description.isNotBlank()) { "Description cannot be blank" }

            return TrackingEvent(
                shipping = shipping,
                status = status,
                location = location,
                description = description
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackingEvent) return false
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}
