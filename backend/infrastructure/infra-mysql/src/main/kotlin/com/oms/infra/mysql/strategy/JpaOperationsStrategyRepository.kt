package com.oms.infra.mysql.strategy

import com.oms.strategy.domain.OperationsStrategy
import com.oms.strategy.domain.vo.StrategyStatus
import com.oms.strategy.repository.OperationsStrategyRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

interface OperationsStrategyJpaRepositoryInterface : JpaRepository<OperationsStrategy, UUID> {
    fun findByCompanyId(companyId: String): List<OperationsStrategy>
    fun findByCompanyIdAndStatus(companyId: String, status: StrategyStatus): List<OperationsStrategy>
}

@Repository
class JpaOperationsStrategyRepository(
    private val jpaRepository: OperationsStrategyJpaRepositoryInterface
) : OperationsStrategyRepository {

    override fun save(strategy: OperationsStrategy): OperationsStrategy {
        return jpaRepository.save(strategy)
    }

    override fun findById(id: UUID): OperationsStrategy? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByIdOrThrow(id: UUID): OperationsStrategy {
        return jpaRepository.findById(id).orElseThrow {
            IllegalArgumentException("OperationsStrategy not found with id: $id")
        }
    }

    override fun findByCompanyId(companyId: String): List<OperationsStrategy> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: StrategyStatus): List<OperationsStrategy> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun findActiveStrategiesByCompanyId(companyId: String): List<OperationsStrategy> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, StrategyStatus.ACTIVE)
    }

    override fun delete(strategy: OperationsStrategy) {
        jpaRepository.delete(strategy)
    }

    override fun existsById(id: UUID): Boolean {
        return jpaRepository.existsById(id)
    }
}
