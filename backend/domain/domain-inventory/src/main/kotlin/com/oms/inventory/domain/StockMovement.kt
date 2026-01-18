package com.oms.inventory.domain

import com.oms.inventory.domain.vo.MovementType
import com.oms.core.domain.CompanyAwareEntity
import jakarta.persistence.*
import java.util.UUID

/**
 * Stock Movement entity
 * Represents a stock transaction (audit trail)
 */
@Entity
@Table(
    name = "stock_movements",
    indexes = [
        Index(name = "idx_movement_company", columnList = "company_id"),
        Index(name = "idx_movement_stock", columnList = "stock_id"),
        Index(name = "idx_movement_reference", columnList = "reference_id"),
        Index(name = "idx_movement_type", columnList = "type")
    ]
)
class StockMovement private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "stock_id", nullable = false, length = 36)
    val stockId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: MovementType,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "before_total", nullable = false)
    val beforeTotal: Int,

    @Column(name = "after_total", nullable = false)
    val afterTotal: Int,

    @Column(name = "reference_id", length = 100)
    val referenceId: String? = null,

    @Column(name = "reason", columnDefinition = "TEXT")
    val reason: String? = null

) : CompanyAwareEntity() {

    companion object {
        fun record(
            companyId: String,
            stockId: UUID,
            type: MovementType,
            quantity: Int,
            beforeTotal: Int,
            afterTotal: Int,
            referenceId: String? = null,
            reason: String? = null
        ): StockMovement {
            val movement = StockMovement(
                id = UUID.randomUUID().toString(),
                stockId = stockId,
                type = type,
                quantity = quantity,
                beforeTotal = beforeTotal,
                afterTotal = afterTotal,
                referenceId = referenceId,
                reason = reason
            )
            movement.assignToCompany(companyId)
            return movement
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StockMovement) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
