package com.oms.application.strategy

import com.oms.strategy.domain.StrategyDeployment
import com.oms.strategy.domain.vo.DeploymentStatus
import com.oms.strategy.repository.OperationsStrategyRepository
import com.oms.strategy.repository.StrategyDeploymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StrategyDeploymentService(
    private val deploymentRepository: StrategyDeploymentRepository,
    private val strategyRepository: OperationsStrategyRepository
) {

    fun deployStrategy(
        companyId: String,
        strategyId: UUID,
        country: String,
        notes: String? = null
    ): StrategyDeployment {
        // Verify strategy exists and belongs to company
        val strategy = strategyRepository.findByIdOrThrow(strategyId)
        require(strategy.belongsTo(companyId)) {
            "Strategy $strategyId does not belong to company $companyId"
        }

        val deployment = StrategyDeployment(
            strategyId = strategyId,
            country = country,
            notes = notes
        ).apply {
            assignToCompany(companyId)
        }

        return deploymentRepository.save(deployment)
    }

    @Transactional(readOnly = true)
    fun getDeployment(deploymentId: UUID): StrategyDeployment {
        return deploymentRepository.findByIdOrThrow(deploymentId)
    }

    @Transactional(readOnly = true)
    fun getDeploymentsByCompany(companyId: String): List<StrategyDeployment> {
        return deploymentRepository.findByCompanyId(companyId)
    }

    @Transactional(readOnly = true)
    fun getDeploymentsByStrategy(strategyId: UUID): List<StrategyDeployment> {
        return deploymentRepository.findByStrategyId(strategyId)
    }

    @Transactional(readOnly = true)
    fun getDeploymentsByCountry(companyId: String, country: String): List<StrategyDeployment> {
        return deploymentRepository.findByCompanyIdAndCountry(companyId, country)
    }

    @Transactional(readOnly = true)
    fun getDeploymentsByStatus(companyId: String, status: DeploymentStatus): List<StrategyDeployment> {
        return deploymentRepository.findByCompanyIdAndStatus(companyId, status)
    }

    fun rollbackDeployment(deploymentId: UUID, reason: String? = null): StrategyDeployment {
        val deployment = deploymentRepository.findByIdOrThrow(deploymentId)
        deployment.rollback(reason)
        return deploymentRepository.save(deployment)
    }

    fun markDeploymentAsFailed(deploymentId: UUID, reason: String): StrategyDeployment {
        val deployment = deploymentRepository.findByIdOrThrow(deploymentId)
        deployment.markAsFailed(reason)
        return deploymentRepository.save(deployment)
    }

    fun updateDeploymentNotes(deploymentId: UUID, notes: String): StrategyDeployment {
        val deployment = deploymentRepository.findByIdOrThrow(deploymentId)
        deployment.updateNotes(notes)
        return deploymentRepository.save(deployment)
    }
}
