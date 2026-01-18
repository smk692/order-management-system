package com.oms.strategy.domain.vo

import jakarta.persistence.Embeddable
import java.math.BigDecimal
import java.time.LocalDateTime

@Embeddable
data class SimulationResult(
    val efficiencyScore: Int,
    val costSaving: BigDecimal,
    val avgLeadTime: Int,        // days
    val recommendation: String,
    val calculatedAt: LocalDateTime
) {
    init {
        require(efficiencyScore in 0..100) { "Efficiency score must be between 0 and 100" }
        require(avgLeadTime >= 0) { "Average lead time must be non-negative" }
    }
}
