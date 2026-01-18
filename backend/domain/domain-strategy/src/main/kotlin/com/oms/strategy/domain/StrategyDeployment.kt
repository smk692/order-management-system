package com.oms.strategy.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.strategy.domain.vo.DeploymentStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "strategy_deployments")
class StrategyDeployment(
    strategyId: UUID,
    country: String,
    notes: String? = null
) : CompanyAwareEntity() {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    var id: UUID = UUID.randomUUID()
        protected set

    @Column(name = "strategy_id", nullable = false, length = 36)
    var strategyId: UUID = strategyId
        protected set

    @Column(name = "country", nullable = false, length = 3)
    var country: String = country
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: DeploymentStatus = DeploymentStatus.DEPLOYED
        protected set

    @Column(name = "deployed_at", nullable = false)
    var deployedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Column(name = "rollback_at")
    var rollbackAt: LocalDateTime? = null
        protected set

    @Column(name = "notes", length = 1000)
    var notes: String? = notes
        protected set

    fun rollback(reason: String? = null) {
        require(status == DeploymentStatus.DEPLOYED) {
            "Can only rollback deployed strategies"
        }
        this.status = DeploymentStatus.ROLLED_BACK
        this.rollbackAt = LocalDateTime.now()
        if (reason != null) {
            this.notes = if (notes != null) {
                "$notes\nRollback reason: $reason"
            } else {
                "Rollback reason: $reason"
            }
        }
    }

    fun markAsFailed(reason: String) {
        this.status = DeploymentStatus.FAILED
        this.notes = if (notes != null) {
            "$notes\nFailure reason: $reason"
        } else {
            "Failure reason: $reason"
        }
    }

    fun updateNotes(additionalNotes: String) {
        this.notes = if (notes != null) {
            "$notes\n$additionalNotes"
        } else {
            additionalNotes
        }
    }
}
