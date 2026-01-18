package com.oms.strategy.domain.vo

import jakarta.persistence.Embeddable

@Embeddable
data class SimulationWeights(
    val costReduction: Int,      // 0-100
    val leadTime: Int,           // 0-100
    val stockBalance: Int,       // 0-100
    val carbonEmission: Int      // 0-100
) {
    init {
        require(costReduction in 0..100) { "Cost reduction weight must be between 0 and 100" }
        require(leadTime in 0..100) { "Lead time weight must be between 0 and 100" }
        require(stockBalance in 0..100) { "Stock balance weight must be between 0 and 100" }
        require(carbonEmission in 0..100) { "Carbon emission weight must be between 0 and 100" }
    }
}
