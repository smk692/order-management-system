package com.oms.settlement.repository

import com.oms.settlement.domain.Settlement
import com.oms.settlement.domain.vo.SettlementStatus
import java.util.*

interface SettlementRepository {

    fun save(settlement: Settlement): Settlement

    fun findById(id: UUID): Settlement?

    fun findByCompanyId(companyId: UUID): List<Settlement>

    fun findByChannelId(channelId: String): List<Settlement>

    fun findByCompanyIdAndPeriod(
        companyId: UUID,
        year: Int,
        month: Int
    ): List<Settlement>

    fun findByCompanyIdAndStatus(
        companyId: UUID,
        status: SettlementStatus
    ): List<Settlement>

    fun delete(settlement: Settlement)

    fun existsById(id: UUID): Boolean
}
