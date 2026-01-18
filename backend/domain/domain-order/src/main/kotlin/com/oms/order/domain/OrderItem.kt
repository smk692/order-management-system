package com.oms.order.domain

import com.oms.core.domain.Money
import jakarta.persistence.*
import java.time.Instant

/**
 * OrderItem child entity
 * Represents a line item in an order with product snapshot
 */
@Entity
@Table(
    name = "order_item",
    indexes = [
        Index(name = "idx_order_item_order", columnList = "order_id"),
        Index(name = "idx_order_item_product", columnList = "product_id")
    ]
)
class OrderItem private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "product_id", nullable = false, length = 36)
    val productId: String,

    @Column(name = "product_name", nullable = false, length = 200)
    val productName: String,  // Snapshot at order time

    @Column(name = "sku", nullable = false, length = 50)
    val sku: String,

    @Column(name = "quantity", nullable = false)
    var quantity: Int,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "unit_price", nullable = false, precision = 19, scale = 2)),
        AttributeOverride(name = "currency", column = Column(name = "unit_price_currency", nullable = false, length = 3))
    )
    val unitPrice: Money,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_price", nullable = false, precision = 19, scale = 2)),
        AttributeOverride(name = "currency", column = Column(name = "total_price_currency", nullable = false, length = 3))
    )
    var totalPrice: Money,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {

    companion object {
        fun create(
            order: Order,
            productId: String,
            productName: String,
            sku: String,
            quantity: Int,
            unitPrice: Money,
            totalPrice: Money
        ): OrderItem {
            require(productId.isNotBlank()) { "Product ID cannot be blank" }
            require(productName.isNotBlank()) { "Product name cannot be blank" }
            require(sku.isNotBlank()) { "SKU cannot be blank" }
            require(quantity > 0) { "Quantity must be positive" }

            return OrderItem(
                order = order,
                productId = productId,
                productName = productName,
                sku = sku,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice
            )
        }
    }

    /**
     * Update quantity and recalculate total
     */
    fun updateQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "Quantity must be positive" }
        this.quantity = newQuantity
        this.totalPrice = unitPrice.multiply(newQuantity)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        if (id == 0L && other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}
