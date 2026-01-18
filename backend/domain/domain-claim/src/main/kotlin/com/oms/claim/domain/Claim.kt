package com.oms.claim.domain

import com.oms.claim.domain.vo.ClaimPriority
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import com.oms.core.domain.CompanyAwareEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "claims")
class Claim(
    @Id
    @Column(nullable = false, length = 20)
    val id: String,

    @Column(name = "claim_number", nullable = false, unique = true, length = 20)
    val claimNumber: String,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: ClaimType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ClaimStatus = ClaimStatus.PENDING,

    @Column(nullable = false, columnDefinition = "TEXT")
    val reason: String,

    @Column(columnDefinition = "TEXT")
    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var priority: ClaimPriority = ClaimPriority.NORMAL,

    @Column(name = "refund_amount", precision = 15, scale = 2)
    var refundAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null,

    @Column(name = "processed_at")
    var processedAt: LocalDateTime? = null
) : CompanyAwareEntity() {

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", referencedColumnName = "id")
    private val _items: MutableList<ClaimItem> = mutableListOf()

    val items: List<ClaimItem>
        get() = _items.toList()

    init {
        require(claimNumber.matches(Regex("CLM-\\d{8}-\\d{3}"))) {
            "Claim number must be in format CLM-yyyyMMdd-nnn"
        }
        require(reason.isNotBlank()) { "Reason cannot be blank" }
    }

    fun addItem(item: ClaimItem) {
        item.claimId = this.id
        _items.add(item)
    }

    fun startProcessing() {
        require(status == ClaimStatus.PENDING) {
            "Can only start processing a PENDING claim"
        }
        status = ClaimStatus.PROCESSING
        processedAt = LocalDateTime.now()
    }

    fun complete(refundAmount: BigDecimal) {
        require(status == ClaimStatus.PROCESSING) {
            "Can only complete a PROCESSING claim"
        }
        require(refundAmount >= BigDecimal.ZERO) {
            "Refund amount must be non-negative"
        }
        status = ClaimStatus.COMPLETED
        this.refundAmount = refundAmount
        this.refundedAt = LocalDateTime.now()
    }

    fun reject(rejectionReason: String) {
        require(status == ClaimStatus.PENDING || status == ClaimStatus.PROCESSING) {
            "Can only reject PENDING or PROCESSING claims"
        }
        require(rejectionReason.isNotBlank()) {
            "Rejection reason cannot be blank"
        }
        status = ClaimStatus.REJECTED
        this.memo = rejectionReason
    }

    fun getTotalItemsAmount(): BigDecimal {
        return _items.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.getTotalPrice())
        }
    }
}
