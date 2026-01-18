package com.oms.claim.repository

import com.oms.claim.domain.Claim
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import java.util.*

interface ClaimRepository {
    fun save(claim: Claim): Claim
    fun findById(id: String): Claim?
    fun findByClaimNumber(claimNumber: String): Claim?
    fun findByCompanyId(companyId: UUID): List<Claim>
    fun findByOrderId(orderId: String): List<Claim>
    fun findByCompanyIdAndStatus(companyId: UUID, status: ClaimStatus): List<Claim>
    fun findByCompanyIdAndType(companyId: UUID, type: ClaimType): List<Claim>
}
