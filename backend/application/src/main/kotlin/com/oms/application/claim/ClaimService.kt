package com.oms.application.claim

import com.oms.application.claim.dto.*
import com.oms.claim.domain.Claim
import com.oms.claim.domain.ClaimItem
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import com.oms.claim.repository.ClaimRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class ClaimService(
    private val claimRepository: ClaimRepository
) {

    fun createClaim(command: CreateClaimCommand): ClaimResult {
        val companyId = UUID.fromString(command.companyId)
        val claimId = UUID.randomUUID().toString()
        val claimNumber = generateClaimNumber()

        val claim = Claim(
            id = claimId,
            claimNumber = claimNumber,
            orderId = command.orderId,
            type = command.type,
            reason = command.reason,
            memo = command.memo,
            priority = command.priority
        )

        claim.assignToCompany(command.companyId)

        command.items.forEach { itemCommand ->
            val item = ClaimItem(
                claimId = claimId,
                productId = itemCommand.productId,
                productName = itemCommand.productName,
                quantity = itemCommand.quantity,
                unitPrice = itemCommand.unitPrice,
                reason = itemCommand.reason
            )
            claim.addItem(item)
        }

        val savedClaim = claimRepository.save(claim)
        return toClaimResult(savedClaim)
    }

    @Transactional(readOnly = true)
    fun getClaim(id: String): ClaimResult {
        val claim = claimRepository.findById(id)
            ?: throw IllegalArgumentException("Claim not found: $id")
        return toClaimResult(claim)
    }

    @Transactional(readOnly = true)
    fun getClaimByNumber(claimNumber: String): ClaimResult {
        val claim = claimRepository.findByClaimNumber(claimNumber)
            ?: throw IllegalArgumentException("Claim not found: $claimNumber")
        return toClaimResult(claim)
    }

    @Transactional(readOnly = true)
    fun getClaimsByCompany(companyId: UUID): List<ClaimResult> {
        return claimRepository.findByCompanyId(companyId)
            .map { toClaimResult(it) }
    }

    @Transactional(readOnly = true)
    fun getClaimsByOrder(orderId: String): List<ClaimResult> {
        return claimRepository.findByOrderId(orderId)
            .map { toClaimResult(it) }
    }

    @Transactional(readOnly = true)
    fun getClaimsByCompanyAndStatus(companyId: UUID, status: ClaimStatus): List<ClaimResult> {
        return claimRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toClaimResult(it) }
    }

    @Transactional(readOnly = true)
    fun getClaimsByCompanyAndType(companyId: UUID, type: ClaimType): List<ClaimResult> {
        return claimRepository.findByCompanyIdAndType(companyId, type)
            .map { toClaimResult(it) }
    }

    fun startProcessing(id: String): ClaimResult {
        val claim = claimRepository.findById(id)
            ?: throw IllegalArgumentException("Claim not found: $id")

        claim.startProcessing()
        val savedClaim = claimRepository.save(claim)
        return toClaimResult(savedClaim)
    }

    fun completeClaim(id: String, command: CompleteClaimCommand): ClaimResult {
        val claim = claimRepository.findById(id)
            ?: throw IllegalArgumentException("Claim not found: $id")

        claim.complete(command.refundAmount)
        val savedClaim = claimRepository.save(claim)
        return toClaimResult(savedClaim)
    }

    fun rejectClaim(id: String, command: RejectClaimCommand): ClaimResult {
        val claim = claimRepository.findById(id)
            ?: throw IllegalArgumentException("Claim not found: $id")

        claim.reject(command.reason)
        val savedClaim = claimRepository.save(claim)
        return toClaimResult(savedClaim)
    }

    private fun generateClaimNumber(): String {
        val today = LocalDate.now()
        val dateString = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        val existingClaims = claimRepository.findByCompanyId(UUID.randomUUID())
        val sequence = existingClaims
            .filter { it.claimNumber.startsWith("CLM-$dateString") }
            .size + 1

        return "CLM-$dateString-${sequence.toString().padStart(3, '0')}"
    }

    private fun toClaimResult(claim: Claim): ClaimResult {
        return ClaimResult(
            id = claim.id,
            companyId = claim.companyId,
            claimNumber = claim.claimNumber,
            orderId = claim.orderId,
            type = claim.type,
            status = claim.status,
            reason = claim.reason,
            memo = claim.memo,
            priority = claim.priority,
            refundAmount = claim.refundAmount,
            refundedAt = claim.refundedAt?.toString(),
            processedAt = claim.processedAt?.toString(),
            items = claim.items.map { toClaimItemResult(it) },
            createdAt = claim.createdAt.toString(),
            updatedAt = claim.updatedAt.toString()
        )
    }

    private fun toClaimItemResult(item: ClaimItem): ClaimItemResult {
        return ClaimItemResult(
            id = item.id,
            productId = item.productId,
            productName = item.productName,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            totalPrice = item.getTotalPrice(),
            reason = item.reason
        )
    }
}
