package com.oms.strategy.repository

import com.oms.strategy.domain.StrategyDeployment
import com.oms.strategy.domain.vo.DeploymentStatus
import java.util.UUID

interface StrategyDeploymentRepository {
    fun save(deployment: StrategyDeployment): StrategyDeployment
    fun findById(id: UUID): StrategyDeployment?
    fun findByIdOrThrow(id: UUID): StrategyDeployment
    fun findByCompanyId(companyId: String): List<StrategyDeployment>
    fun findByStrategyId(strategyId: UUID): List<StrategyDeployment>
    fun findByCompanyIdAndCountry(companyId: String, country: String): List<StrategyDeployment>
    fun findByCompanyIdAndStatus(companyId: String, status: DeploymentStatus): List<StrategyDeployment>
    fun delete(deployment: StrategyDeployment)
    fun existsById(id: UUID): Boolean
}
