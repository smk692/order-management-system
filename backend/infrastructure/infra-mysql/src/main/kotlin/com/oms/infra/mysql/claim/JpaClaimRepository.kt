package com.oms.infra.mysql.claim

import com.oms.claim.domain.Claim
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import com.oms.claim.repository.ClaimRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

interface ClaimJpaRepositoryInterface : JpaRepository<Claim, String> {
    fun findByClaimNumber(claimNumber: String): Claim?
    fun findByCompanyId(companyId: String): List<Claim>
    fun findByOrderId(orderId: String): List<Claim>
    fun findByCompanyIdAndStatus(companyId: String, status: ClaimStatus): List<Claim>
    fun findByCompanyIdAndType(companyId: String, type: ClaimType): List<Claim>
}

@Repository
class JpaClaimRepository(
    private val jpaRepository: ClaimJpaRepositoryInterface
) : ClaimRepository {

    override fun save(claim: Claim): Claim {
        return jpaRepository.save(claim)
    }

    override fun findById(id: String): Claim? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByClaimNumber(claimNumber: String): Claim? {
        return jpaRepository.findByClaimNumber(claimNumber)
    }

    override fun findByCompanyId(companyId: UUID): List<Claim> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByOrderId(orderId: String): List<Claim> {
        return jpaRepository.findByOrderId(orderId)
    }

    override fun findByCompanyIdAndStatus(companyId: UUID, status: ClaimStatus): List<Claim> {
        return jpaRepository.findByCompanyIdAndStatus(companyId.toString(), status)
    }

    override fun findByCompanyIdAndType(companyId: UUID, type: ClaimType): List<Claim> {
        return jpaRepository.findByCompanyIdAndType(companyId.toString(), type)
    }
}
