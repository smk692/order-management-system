package com.oms.strategy.domain.event

import com.oms.core.event.DomainEvent
import java.util.UUID

sealed class StrategyEvent : DomainEvent() {

    data class StrategyCreatedEvent(
        override val aggregateId: String,
        val strategyId: UUID,
        val name: String
    ) : StrategyEvent() {
        override val aggregateType: String = "OperationsStrategy"
    }

    data class StrategySimulatedEvent(
        override val aggregateId: String,
        val strategyId: UUID,
        val efficiencyScore: Int
    ) : StrategyEvent() {
        override val aggregateType: String = "OperationsStrategy"
    }

    data class StrategyActivatedEvent(
        override val aggregateId: String,
        val strategyId: UUID
    ) : StrategyEvent() {
        override val aggregateType: String = "OperationsStrategy"
    }

    data class ReadinessUpdatedEvent(
        override val aggregateId: String,
        val readinessId: UUID,
        val country: String,
        val score: Int
    ) : StrategyEvent() {
        override val aggregateType: String = "GlobalReadiness"
    }

    data class CountryLaunchedEvent(
        override val aggregateId: String,
        val readinessId: UUID,
        val country: String
    ) : StrategyEvent() {
        override val aggregateType: String = "GlobalReadiness"
    }

    data class StrategyDeployedEvent(
        override val aggregateId: String,
        val deploymentId: UUID,
        val strategyId: UUID,
        val country: String
    ) : StrategyEvent() {
        override val aggregateType: String = "StrategyDeployment"
    }
}
