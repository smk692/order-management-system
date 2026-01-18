package com.oms.infra.mysql.settlement

import com.oms.settlement.domain.Settlement
import com.oms.settlement.domain.vo.SettlementStatus
import com.oms.settlement.repository.SettlementRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

interface SettlementJpaRepositoryInterface : JpaRepository<Settlement, UUID> {
    fun findByCompanyId(companyId: String): List<Settlement>

    fun findByChannelId(channelId: String): List<Settlement>

    @Query("""
        SELECT s FROM Settlement s
        WHERE s.companyId = :companyId
        AND s.period.year = :year
        AND s.period.month = :month
    """)
    fun findByCompanyIdAndPeriod(
        @Param("companyId") companyId: String,
        @Param("year") year: Int,
        @Param("month") month: Int
    ): List<Settlement>

    fun findByCompanyIdAndStatus(
        companyId: String,
        status: SettlementStatus
    ): List<Settlement>
}

@Repository
class JpaSettlementRepository(
    private val jpaRepository: SettlementJpaRepositoryInterface
) : SettlementRepository {

    override fun save(settlement: Settlement): Settlement {
        return jpaRepository.save(settlement)
    }

    override fun findById(id: UUID): Settlement? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: UUID): List<Settlement> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByChannelId(channelId: String): List<Settlement> {
        return jpaRepository.findByChannelId(channelId)
    }

    override fun findByCompanyIdAndPeriod(
        companyId: UUID,
        year: Int,
        month: Int
    ): List<Settlement> {
        return jpaRepository.findByCompanyIdAndPeriod(
            companyId.toString(),
            year,
            month
        )
    }

    override fun findByCompanyIdAndStatus(
        companyId: UUID,
        status: SettlementStatus
    ): List<Settlement> {
        return jpaRepository.findByCompanyIdAndStatus(
            companyId.toString(),
            status
        )
    }

    override fun delete(settlement: Settlement) {
        jpaRepository.delete(settlement)
    }

    override fun existsById(id: UUID): Boolean {
        return jpaRepository.existsById(id)
    }
}
