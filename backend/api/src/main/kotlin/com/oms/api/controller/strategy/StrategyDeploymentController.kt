package com.oms.api.controller.strategy

import com.oms.application.strategy.StrategyDeploymentService
import com.oms.strategy.domain.StrategyDeployment
import com.oms.strategy.domain.vo.DeploymentStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/deployments")
class StrategyDeploymentController(
    private val deploymentService: StrategyDeploymentService
) {

    @PostMapping
    fun deployStrategy(@RequestBody request: DeployStrategyRequest): ResponseEntity<StrategyDeployment> {
        val deployment = deploymentService.deployStrategy(
            companyId = request.companyId,
            strategyId = request.strategyId,
            country = request.country,
            notes = request.notes
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(deployment)
    }

    @GetMapping("/{id}")
    fun getDeployment(@PathVariable id: UUID): ResponseEntity<StrategyDeployment> {
        val deployment = deploymentService.getDeployment(id)
        return ResponseEntity.ok(deployment)
    }

    @GetMapping("/company/{companyId}")
    fun getDeploymentsByCompany(@PathVariable companyId: String): ResponseEntity<List<StrategyDeployment>> {
        val deployments = deploymentService.getDeploymentsByCompany(companyId)
        return ResponseEntity.ok(deployments)
    }

    @GetMapping("/strategy/{strategyId}")
    fun getDeploymentsByStrategy(@PathVariable strategyId: UUID): ResponseEntity<List<StrategyDeployment>> {
        val deployments = deploymentService.getDeploymentsByStrategy(strategyId)
        return ResponseEntity.ok(deployments)
    }

    @GetMapping("/company/{companyId}/country/{country}")
    fun getDeploymentsByCountry(
        @PathVariable companyId: String,
        @PathVariable country: String
    ): ResponseEntity<List<StrategyDeployment>> {
        val deployments = deploymentService.getDeploymentsByCountry(companyId, country)
        return ResponseEntity.ok(deployments)
    }

    @GetMapping("/company/{companyId}/status/{status}")
    fun getDeploymentsByStatus(
        @PathVariable companyId: String,
        @PathVariable status: DeploymentStatus
    ): ResponseEntity<List<StrategyDeployment>> {
        val deployments = deploymentService.getDeploymentsByStatus(companyId, status)
        return ResponseEntity.ok(deployments)
    }

    @PostMapping("/{id}/rollback")
    fun rollbackDeployment(
        @PathVariable id: UUID,
        @RequestBody request: RollbackRequest?
    ): ResponseEntity<StrategyDeployment> {
        val deployment = deploymentService.rollbackDeployment(id, request?.reason)
        return ResponseEntity.ok(deployment)
    }

    @PostMapping("/{id}/fail")
    fun markDeploymentAsFailed(
        @PathVariable id: UUID,
        @RequestBody request: FailureRequest
    ): ResponseEntity<StrategyDeployment> {
        val deployment = deploymentService.markDeploymentAsFailed(id, request.reason)
        return ResponseEntity.ok(deployment)
    }

    @PutMapping("/{id}/notes")
    fun updateDeploymentNotes(
        @PathVariable id: UUID,
        @RequestBody request: NotesRequest
    ): ResponseEntity<StrategyDeployment> {
        val deployment = deploymentService.updateDeploymentNotes(id, request.notes)
        return ResponseEntity.ok(deployment)
    }
}

data class DeployStrategyRequest(
    val companyId: String,
    val strategyId: UUID,
    val country: String,
    val notes: String? = null
)

data class RollbackRequest(
    val reason: String? = null
)

data class FailureRequest(
    val reason: String
)

data class NotesRequest(
    val notes: String
)
