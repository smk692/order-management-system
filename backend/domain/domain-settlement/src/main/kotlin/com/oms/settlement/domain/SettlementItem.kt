package com.oms.settlement.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "settlement_items")
class SettlementItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "settlement_id", nullable = false, columnDefinition = "BINARY(16)")
    var settlementId: UUID? = null,

    @Column(name = "order_id", nullable = false, length = 50)
    val orderId: String,

    @Column(name = "order_amount", nullable = false, precision = 15, scale = 2)
    val orderAmount: BigDecimal,

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    val commissionRate: BigDecimal,

    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    val commissionAmount: BigDecimal,

    @Column(name = "settlement_amount", nullable = false, precision = 15, scale = 2)
    val settlementAmount: BigDecimal
) {
    init {
        require(orderId.isNotBlank()) {
            "Order ID cannot be blank"
        }
        require(orderAmount >= BigDecimal.ZERO) {
            "Order amount must be non-negative"
        }
        require(commissionRate >= BigDecimal.ZERO && commissionRate <= BigDecimal.ONE) {
            "Commission rate must be between 0 and 1"
        }
        require(commissionAmount >= BigDecimal.ZERO) {
            "Commission amount must be non-negative"
        }
        require(settlementAmount >= BigDecimal.ZERO) {
            "Settlement amount must be non-negative"
        }
    }

    companion object {
        fun create(
            orderId: String,
            orderAmount: BigDecimal,
            commissionRate: BigDecimal
        ): SettlementItem {
            val commissionAmount = orderAmount.multiply(commissionRate)
            val settlementAmount = orderAmount.subtract(commissionAmount)

            return SettlementItem(
                orderId = orderId,
                orderAmount = orderAmount,
                commissionRate = commissionRate,
                commissionAmount = commissionAmount,
                settlementAmount = settlementAmount
            )
        }
    }
}
