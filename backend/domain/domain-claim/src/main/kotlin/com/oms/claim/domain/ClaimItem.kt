package com.oms.claim.domain

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "claim_items")
class ClaimItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "claim_id", nullable = false)
    var claimId: String,

    @Column(name = "product_id", nullable = false)
    val productId: String,

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", nullable = false)
    val unitPrice: BigDecimal,

    @Column(name = "reason")
    val reason: String? = null
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
        require(unitPrice >= BigDecimal.ZERO) { "Unit price must be non-negative" }
    }

    fun getTotalPrice(): BigDecimal = unitPrice.multiply(BigDecimal(quantity))
}
