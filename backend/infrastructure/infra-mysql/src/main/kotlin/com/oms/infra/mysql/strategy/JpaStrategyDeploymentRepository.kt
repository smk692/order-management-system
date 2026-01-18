package com.oms.infra.mysql.strategy

import com.oms.strategy.domain.StrategyDeployment
import com.oms.strategy.domain.vo.DeploymentStatus
import com.oms.strategy.repository.StrategyDeploymentRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

interface StrategyDeploymentJpaRepositoryInterface : JpaRepository<StrategyDeployment, UUID> {
    fun findByCompanyId(companyId: String): List<StrategyDeployment>
    fun findByStrategyId(strategyId: UUID): List<StrategyDeployment>
    fun findByCompanyIdAndCountry(companyId: String, country: String): List<StrategyDeployment>
    fun findByCompanyIdAndStatus(companyId: String, status: DeploymentStatus): List<StrategyDeployment>
}

@Repository
class JpaStrategyDeploymentRepository(
    private val jpaRepository: StrategyDeploymentJpaRepositoryInterface
) : StrategyDeploymentRepository {

    override fun save(deployment: StrategyDeployment): StrategyDeployment {
        return jpaRepository.save(deployment)
    }

    override fun findById(id: UUID): StrategyDeployment? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByIdOrThrow(id: UUID): StrategyDeployment {
        return jpaRepository.findById(id).orElseThrow {
            IllegalArgumentException("StrategyDeployment not found with id: $id")
        }
    }

    override fun findByCompanyId(companyId: String): List<StrategyDeployment> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByStrategyId(strategyId: UUID): List<StrategyDeployment> {
        return jpaRepository.findByStrategyId(strategyId)
    }

    override fun findByCompanyIdAndCountry(companyId: String, country: String): List<StrategyDeployment> {
        return jpaRepository.findByCompanyIdAndCountry(companyId, country)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: DeploymentStatus): List<StrategyDeployment> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun delete(deployment: StrategyDeployment) {
        jpaRepository.delete(deployment)
    }

    override fun existsById(id: UUID): Boolean {
        return jpaRepository.existsById(id)
    }
}
