package com.oms.infra.mysql.strategy

import com.oms.strategy.domain.GlobalReadiness
import com.oms.strategy.domain.vo.ReadinessStatus
import com.oms.strategy.repository.GlobalReadinessRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

interface GlobalReadinessJpaRepositoryInterface : JpaRepository<GlobalReadiness, UUID> {
    fun findByCompanyId(companyId: String): List<GlobalReadiness>
    fun findByCompanyIdAndCountry(companyId: String, country: String): GlobalReadiness?
    fun findByCompanyIdAndStatus(companyId: String, status: ReadinessStatus): List<GlobalReadiness>
}

@Repository
class JpaGlobalReadinessRepository(
    private val jpaRepository: GlobalReadinessJpaRepositoryInterface
) : GlobalReadinessRepository {

    override fun save(readiness: GlobalReadiness): GlobalReadiness {
        return jpaRepository.save(readiness)
    }

    override fun findById(id: UUID): GlobalReadiness? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByIdOrThrow(id: UUID): GlobalReadiness {
        return jpaRepository.findById(id).orElseThrow {
            IllegalArgumentException("GlobalReadiness not found with id: $id")
        }
    }

    override fun findByCompanyId(companyId: String): List<GlobalReadiness> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByCompanyIdAndCountry(companyId: String, country: String): GlobalReadiness? {
        return jpaRepository.findByCompanyIdAndCountry(companyId, country)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: ReadinessStatus): List<GlobalReadiness> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun delete(readiness: GlobalReadiness) {
        jpaRepository.delete(readiness)
    }

    override fun existsById(id: UUID): Boolean {
        return jpaRepository.existsById(id)
    }
}
