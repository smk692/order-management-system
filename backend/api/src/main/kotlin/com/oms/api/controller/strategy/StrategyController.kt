package com.oms.api.controller.strategy

import com.oms.application.strategy.OperationsStrategyService
import com.oms.strategy.domain.OperationsStrategy
import com.oms.strategy.domain.vo.SimulationWeights
import com.oms.strategy.domain.vo.StrategyStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/strategies")
class StrategyController(
    private val strategyService: OperationsStrategyService
) {

    @PostMapping
    fun createStrategy(@RequestBody request: CreateStrategyRequest): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.createStrategy(
            companyId = request.companyId,
            name = request.name,
            description = request.description,
            targetCountries = request.targetCountries,
            weights = request.weights
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(strategy)
    }

    @GetMapping("/{id}")
    fun getStrategy(@PathVariable id: UUID): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.getStrategy(id)
        return ResponseEntity.ok(strategy)
    }

    @GetMapping("/company/{companyId}")
    fun getStrategiesByCompany(@PathVariable companyId: String): ResponseEntity<List<OperationsStrategy>> {
        val strategies = strategyService.getStrategiesByCompany(companyId)
        return ResponseEntity.ok(strategies)
    }

    @GetMapping("/company/{companyId}/status/{status}")
    fun getStrategiesByStatus(
        @PathVariable companyId: String,
        @PathVariable status: StrategyStatus
    ): ResponseEntity<List<OperationsStrategy>> {
        val strategies = strategyService.getStrategiesByStatus(companyId, status)
        return ResponseEntity.ok(strategies)
    }

    @GetMapping("/company/{companyId}/active")
    fun getActiveStrategies(@PathVariable companyId: String): ResponseEntity<List<OperationsStrategy>> {
        val strategies = strategyService.getActiveStrategies(companyId)
        return ResponseEntity.ok(strategies)
    }

    @PostMapping("/{id}/simulate")
    fun simulateStrategy(
        @PathVariable id: UUID,
        @RequestBody request: SimulateStrategyRequest
    ): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.simulateStrategy(
            strategyId = id,
            efficiencyScore = request.efficiencyScore,
            costSaving = request.costSaving,
            avgLeadTime = request.avgLeadTime,
            recommendation = request.recommendation
        )
        return ResponseEntity.ok(strategy)
    }

    @PostMapping("/{id}/activate")
    fun activateStrategy(@PathVariable id: UUID): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.activateStrategy(id)
        return ResponseEntity.ok(strategy)
    }

    @PostMapping("/{id}/archive")
    fun archiveStrategy(@PathVariable id: UUID): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.archiveStrategy(id)
        return ResponseEntity.ok(strategy)
    }

    @PostMapping("/{id}/countries")
    fun addTargetCountry(
        @PathVariable id: UUID,
        @RequestBody request: CountryRequest
    ): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.addTargetCountry(id, request.country)
        return ResponseEntity.ok(strategy)
    }

    @DeleteMapping("/{id}/countries/{country}")
    fun removeTargetCountry(
        @PathVariable id: UUID,
        @PathVariable country: String
    ): ResponseEntity<OperationsStrategy> {
        val strategy = strategyService.removeTargetCountry(id, country)
        return ResponseEntity.ok(strategy)
    }
}

data class CreateStrategyRequest(
    val companyId: String,
    val name: String,
    val description: String?,
    val targetCountries: List<String>,
    val weights: SimulationWeights
)

data class SimulateStrategyRequest(
    val efficiencyScore: Int,
    val costSaving: BigDecimal,
    val avgLeadTime: Int,
    val recommendation: String
)

data class CountryRequest(
    val country: String
)
