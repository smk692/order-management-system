package com.oms.strategy.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.strategy.domain.vo.ReadinessItem
import com.oms.strategy.domain.vo.ReadinessStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "global_readiness")
class GlobalReadiness(
    country: String,
    initialChecklist: List<ReadinessItem> = emptyList()
) : CompanyAwareEntity() {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    var id: UUID = UUID.randomUUID()
        protected set

    @Column(name = "country", nullable = false, length = 3)
    var country: String = country
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ReadinessStatus = ReadinessStatus.NOT_READY
        protected set

    @ElementCollection
    @CollectionTable(
        name = "global_readiness_checklist",
        joinColumns = [JoinColumn(name = "readiness_id")]
    )
    val checklist: MutableList<ReadinessItem> = initialChecklist.toMutableList()

    @Column(name = "score", nullable = false)
    var score: Int = 0
        protected set

    @Column(name = "launched_at")
    var launchedAt: LocalDateTime? = null
        protected set

    init {
        calculateScore()
    }

    fun updateChecklist(item: ReadinessItem) {
        val index = checklist.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            checklist[index] = item
        } else {
            checklist.add(item)
        }
        calculateScore()
        updateStatus()
    }

    fun calculateScore() {
        if (checklist.isEmpty()) {
            score = 0
            return
        }
        val completedCount = checklist.count { it.completed }
        score = (completedCount * 100) / checklist.size
    }

    private fun updateStatus() {
        status = when {
            score >= 100 -> ReadinessStatus.READY
            score >= 50 -> ReadinessStatus.PREPARING
            else -> ReadinessStatus.NOT_READY
        }
    }

    fun launch() {
        require(status == ReadinessStatus.READY) {
            "Cannot launch country that is not ready (current status: $status)"
        }
        this.status = ReadinessStatus.LAUNCHED
        this.launchedAt = LocalDateTime.now()
    }

    fun addChecklistItem(item: ReadinessItem) {
        if (checklist.none { it.id == item.id }) {
            checklist.add(item)
            calculateScore()
            updateStatus()
        }
    }

    fun removeChecklistItem(itemId: String) {
        checklist.removeIf { it.id == itemId }
        calculateScore()
        updateStatus()
    }
}
