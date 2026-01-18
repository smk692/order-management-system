package com.oms.strategy.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.strategy.domain.vo.SimulationResult
import com.oms.strategy.domain.vo.SimulationWeights
import com.oms.strategy.domain.vo.StrategyStatus
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "operations_strategies")
class OperationsStrategy(
    name: String,
    description: String? = null,
    weights: SimulationWeights
) : CompanyAwareEntity() {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    var id: UUID = UUID.randomUUID()
        protected set

    @Column(name = "name", nullable = false, length = 100)
    var name: String = name
        protected set

    @Column(name = "description", length = 500)
    var description: String? = description
        protected set

    @ElementCollection
    @CollectionTable(
        name = "operations_strategy_countries",
        joinColumns = [JoinColumn(name = "strategy_id")]
    )
    @Column(name = "country")
    val targetCountries: MutableList<String> = mutableListOf()

    @Embedded
    var weights: SimulationWeights = weights
        protected set

    @Embedded
    var result: SimulationResult? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: StrategyStatus = StrategyStatus.DRAFT
        protected set

    fun simulate(simulationResult: SimulationResult) {
        require(status == StrategyStatus.DRAFT || status == StrategyStatus.SIMULATED) {
            "Can only simulate strategies in DRAFT or SIMULATED status"
        }
        this.result = simulationResult
        this.status = StrategyStatus.SIMULATED
    }

    fun activate() {
        require(status == StrategyStatus.SIMULATED) {
            "Can only activate simulated strategies"
        }
        requireNotNull(result) { "Cannot activate strategy without simulation result" }
        this.status = StrategyStatus.ACTIVE
    }

    fun archive() {
        require(status != StrategyStatus.ARCHIVED) {
            "Strategy is already archived"
        }
        this.status = StrategyStatus.ARCHIVED
    }

    fun addTargetCountry(country: String) {
        require(country.isNotBlank()) { "Country cannot be blank" }
        if (!targetCountries.contains(country)) {
            targetCountries.add(country)
        }
    }

    fun removeTargetCountry(country: String) {
        targetCountries.remove(country)
    }
}
