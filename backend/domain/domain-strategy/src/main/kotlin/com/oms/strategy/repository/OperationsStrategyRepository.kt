package com.oms.strategy.repository

import com.oms.strategy.domain.OperationsStrategy
import com.oms.strategy.domain.vo.StrategyStatus
import java.util.UUID

interface OperationsStrategyRepository {
    fun save(strategy: OperationsStrategy): OperationsStrategy
    fun findById(id: UUID): OperationsStrategy?
    fun findByIdOrThrow(id: UUID): OperationsStrategy
    fun findByCompanyId(companyId: String): List<OperationsStrategy>
    fun findByCompanyIdAndStatus(companyId: String, status: StrategyStatus): List<OperationsStrategy>
    fun findActiveStrategiesByCompanyId(companyId: String): List<OperationsStrategy>
    fun delete(strategy: OperationsStrategy)
    fun existsById(id: UUID): Boolean
}
