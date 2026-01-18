package com.oms.application.claim.dto

import com.oms.claim.domain.vo.ClaimPriority
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import java.math.BigDecimal

data class CreateClaimCommand(
    val companyId: String,
    val orderId: String,
    val type: ClaimType,
    val reason: String,
    val priority: ClaimPriority = ClaimPriority.NORMAL,
    val memo: String? = null,
    val items: List<CreateClaimItemCommand>
)

data class CreateClaimItemCommand(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val reason: String? = null
)

data class CompleteClaimCommand(
    val refundAmount: BigDecimal
)

data class RejectClaimCommand(
    val reason: String
)

data class ClaimResult(
    val id: String,
    val companyId: String,
    val claimNumber: String,
    val orderId: String,
    val type: ClaimType,
    val status: ClaimStatus,
    val reason: String,
    val memo: String?,
    val priority: ClaimPriority,
    val refundAmount: BigDecimal,
    val refundedAt: String?,
    val processedAt: String?,
    val items: List<ClaimItemResult>,
    val createdAt: String,
    val updatedAt: String
)

data class ClaimItemResult(
    val id: Long?,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal,
    val reason: String?
)
