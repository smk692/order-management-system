package com.oms.application.settlement

import com.oms.application.settlement.dto.*
import com.oms.settlement.domain.Settlement
import com.oms.settlement.domain.SettlementItem
import com.oms.settlement.domain.vo.SettlementPeriod
import com.oms.settlement.domain.vo.SettlementStatus
import com.oms.settlement.repository.SettlementRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class SettlementService(
    private val settlementRepository: SettlementRepository
) {

    fun createSettlement(command: CreateSettlementCommand): SettlementResult {
        val companyId = UUID.fromString(command.companyId)
        val period = SettlementPeriod(command.year, command.month)

        val settlement = Settlement(
            channelId = command.channelId,
            period = period
        )

        settlement.assignToCompany(command.companyId)

        val savedSettlement = settlementRepository.save(settlement)
        return toSettlementResult(savedSettlement)
    }

    @Transactional(readOnly = true)
    fun getSettlement(id: UUID): SettlementResult {
        val settlement = settlementRepository.findById(id)
            ?: throw IllegalArgumentException("Settlement not found: $id")
        return toSettlementResult(settlement)
    }

    @Transactional(readOnly = true)
    fun getSettlementsByCompany(companyId: UUID): List<SettlementResult> {
        return settlementRepository.findByCompanyId(companyId)
            .map { toSettlementResult(it) }
    }

    @Transactional(readOnly = true)
    fun getSettlementsByChannel(channelId: String): List<SettlementResult> {
        return settlementRepository.findByChannelId(channelId)
            .map { toSettlementResult(it) }
    }

    @Transactional(readOnly = true)
    fun getSettlementsByCompanyAndPeriod(
        companyId: UUID,
        year: Int,
        month: Int
    ): List<SettlementResult> {
        return settlementRepository.findByCompanyIdAndPeriod(companyId, year, month)
            .map { toSettlementResult(it) }
    }

    @Transactional(readOnly = true)
    fun getSettlementsByCompanyAndStatus(
        companyId: UUID,
        status: SettlementStatus
    ): List<SettlementResult> {
        return settlementRepository.findByCompanyIdAndStatus(companyId, status)
            .map { toSettlementResult(it) }
    }

    fun addSettlementItem(id: UUID, command: AddSettlementItemCommand): SettlementResult {
        val settlement = settlementRepository.findById(id)
            ?: throw IllegalArgumentException("Settlement not found: $id")

        val item = SettlementItem.create(
            orderId = command.orderId,
            orderAmount = command.orderAmount,
            commissionRate = command.commissionRate
        )

        settlement.addItem(item)
        val savedSettlement = settlementRepository.save(settlement)
        return toSettlementResult(savedSettlement)
    }

    fun calculateSettlement(id: UUID): SettlementResult {
        val settlement = settlementRepository.findById(id)
            ?: throw IllegalArgumentException("Settlement not found: $id")

        settlement.calculate()
        val savedSettlement = settlementRepository.save(settlement)
        return toSettlementResult(savedSettlement)
    }

    fun confirmSettlement(id: UUID): SettlementResult {
        val settlement = settlementRepository.findById(id)
            ?: throw IllegalArgumentException("Settlement not found: $id")

        settlement.confirm()
        val savedSettlement = settlementRepository.save(settlement)
        return toSettlementResult(savedSettlement)
    }

    fun markSettlementAsPaid(id: UUID): SettlementResult {
        val settlement = settlementRepository.findById(id)
            ?: throw IllegalArgumentException("Settlement not found: $id")

        settlement.markAsPaid()
        val savedSettlement = settlementRepository.save(settlement)
        return toSettlementResult(savedSettlement)
    }

    private fun toSettlementResult(settlement: Settlement): SettlementResult {
        return SettlementResult(
            id = settlement.id.toString(),
            companyId = settlement.companyId,
            channelId = settlement.channelId,
            period = settlement.period,
            status = settlement.status,
            totalSales = settlement.totalSales,
            totalCommission = settlement.totalCommission,
            netSettlement = settlement.netSettlement,
            confirmedAt = settlement.confirmedAt?.toString(),
            paidAt = settlement.paidAt?.toString(),
            items = settlement.items.map { toSettlementItemResult(it) },
            createdAt = settlement.createdAt.toString(),
            updatedAt = settlement.updatedAt.toString()
        )
    }

    private fun toSettlementItemResult(item: SettlementItem): SettlementItemResult {
        return SettlementItemResult(
            id = item.id,
            orderId = item.orderId,
            orderAmount = item.orderAmount,
            commissionRate = item.commissionRate,
            commissionAmount = item.commissionAmount,
            settlementAmount = item.settlementAmount
        )
    }
}
