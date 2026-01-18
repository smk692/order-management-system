package com.oms.strategy.repository

import com.oms.strategy.domain.GlobalReadiness
import com.oms.strategy.domain.vo.ReadinessStatus
import java.util.UUID

interface GlobalReadinessRepository {
    fun save(readiness: GlobalReadiness): GlobalReadiness
    fun findById(id: UUID): GlobalReadiness?
    fun findByIdOrThrow(id: UUID): GlobalReadiness
    fun findByCompanyId(companyId: String): List<GlobalReadiness>
    fun findByCompanyIdAndCountry(companyId: String, country: String): GlobalReadiness?
    fun findByCompanyIdAndStatus(companyId: String, status: ReadinessStatus): List<GlobalReadiness>
    fun delete(readiness: GlobalReadiness)
    fun existsById(id: UUID): Boolean
}
