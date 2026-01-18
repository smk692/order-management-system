package com.oms.application.strategy

import com.oms.strategy.domain.OperationsStrategy
import com.oms.strategy.domain.vo.SimulationResult
import com.oms.strategy.domain.vo.SimulationWeights
import com.oms.strategy.domain.vo.StrategyStatus
import com.oms.strategy.repository.OperationsStrategyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class OperationsStrategyService(
    private val strategyRepository: OperationsStrategyRepository
) {

    fun createStrategy(
        companyId: String,
        name: String,
        description: String?,
        targetCountries: List<String>,
        weights: SimulationWeights
    ): OperationsStrategy {
        val strategy = OperationsStrategy(
            name = name,
            description = description,
            weights = weights
        ).apply {
            assignToCompany(companyId)
            targetCountries.forEach { addTargetCountry(it) }
        }

        return strategyRepository.save(strategy)
    }

    @Transactional(readOnly = true)
    fun getStrategy(strategyId: UUID): OperationsStrategy {
        return strategyRepository.findByIdOrThrow(strategyId)
    }

    @Transactional(readOnly = true)
    fun getStrategiesByCompany(companyId: String): List<OperationsStrategy> {
        return strategyRepository.findByCompanyId(companyId)
    }

    @Transactional(readOnly = true)
    fun getStrategiesByStatus(companyId: String, status: StrategyStatus): List<OperationsStrategy> {
        return strategyRepository.findByCompanyIdAndStatus(companyId, status)
    }

    @Transactional(readOnly = true)
    fun getActiveStrategies(companyId: String): List<OperationsStrategy> {
        return strategyRepository.findActiveStrategiesByCompanyId(companyId)
    }

    fun simulateStrategy(
        strategyId: UUID,
        efficiencyScore: Int,
        costSaving: BigDecimal,
        avgLeadTime: Int,
        recommendation: String
    ): OperationsStrategy {
        val strategy = strategyRepository.findByIdOrThrow(strategyId)

        val result = SimulationResult(
            efficiencyScore = efficiencyScore,
            costSaving = costSaving,
            avgLeadTime = avgLeadTime,
            recommendation = recommendation,
            calculatedAt = LocalDateTime.now()
        )

        strategy.simulate(result)
        return strategyRepository.save(strategy)
    }

    fun activateStrategy(strategyId: UUID): OperationsStrategy {
        val strategy = strategyRepository.findByIdOrThrow(strategyId)
        strategy.activate()
        return strategyRepository.save(strategy)
    }

    fun archiveStrategy(strategyId: UUID): OperationsStrategy {
        val strategy = strategyRepository.findByIdOrThrow(strategyId)
        strategy.archive()
        return strategyRepository.save(strategy)
    }

    fun addTargetCountry(strategyId: UUID, country: String): OperationsStrategy {
        val strategy = strategyRepository.findByIdOrThrow(strategyId)
        strategy.addTargetCountry(country)
        return strategyRepository.save(strategy)
    }

    fun removeTargetCountry(strategyId: UUID, country: String): OperationsStrategy {
        val strategy = strategyRepository.findByIdOrThrow(strategyId)
        strategy.removeTargetCountry(country)
        return strategyRepository.save(strategy)
    }
}
