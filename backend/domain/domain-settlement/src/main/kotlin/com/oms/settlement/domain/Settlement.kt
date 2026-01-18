package com.oms.settlement.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.settlement.domain.vo.SettlementPeriod
import com.oms.settlement.domain.vo.SettlementStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "settlements")
class Settlement(
    @Id
    @Column(nullable = false, columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "channel_id", nullable = false, length = 50)
    val channelId: String,

    @Embedded
    val period: SettlementPeriod,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: SettlementStatus = SettlementStatus.DRAFT,

    @Column(name = "total_sales", nullable = false, precision = 15, scale = 2)
    var totalSales: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_commission", nullable = false, precision = 15, scale = 2)
    var totalCommission: BigDecimal = BigDecimal.ZERO,

    @Column(name = "net_settlement", nullable = false, precision = 15, scale = 2)
    var netSettlement: BigDecimal = BigDecimal.ZERO,

    @Column(name = "confirmed_at")
    var confirmedAt: LocalDateTime? = null,

    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null
) : CompanyAwareEntity() {

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", referencedColumnName = "id")
    private val _items: MutableList<SettlementItem> = mutableListOf()

    val items: List<SettlementItem>
        get() = _items.toList()

    init {
        require(channelId.isNotBlank()) {
            "Channel ID cannot be blank"
        }
    }

    fun addItem(item: SettlementItem) {
        require(status == SettlementStatus.DRAFT) {
            "Can only add items to DRAFT settlements"
        }
        item.settlementId = this.id
        _items.add(item)
    }

    fun calculate() {
        require(status == SettlementStatus.DRAFT) {
            "Can only calculate DRAFT settlements"
        }

        totalSales = _items.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.orderAmount)
        }

        totalCommission = _items.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.commissionAmount)
        }

        netSettlement = totalSales.subtract(totalCommission)
    }

    fun confirm() {
        require(status == SettlementStatus.DRAFT) {
            "Can only confirm DRAFT settlements"
        }
        require(_items.isNotEmpty()) {
            "Cannot confirm settlement with no items"
        }

        status = SettlementStatus.CONFIRMED
        confirmedAt = LocalDateTime.now()
    }

    fun markAsPaid() {
        require(status == SettlementStatus.CONFIRMED) {
            "Can only mark CONFIRMED settlements as paid"
        }

        status = SettlementStatus.PAID
        paidAt = LocalDateTime.now()
    }

    fun getItemCount(): Int = _items.size
}
