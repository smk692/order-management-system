package com.oms.api.controller

import com.oms.application.settlement.SettlementService
import com.oms.application.settlement.dto.AddSettlementItemCommand
import com.oms.application.settlement.dto.CreateSettlementCommand
import com.oms.application.settlement.dto.SettlementResult
import com.oms.settlement.domain.vo.SettlementStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/settlements")
class SettlementController(
    private val settlementService: SettlementService
) {

    @PostMapping
    fun createSettlement(@RequestBody command: CreateSettlementCommand): ResponseEntity<SettlementResult> {
        val result = settlementService.createSettlement(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/{id}")
    fun getSettlement(@PathVariable id: String): ResponseEntity<SettlementResult> {
        val result = settlementService.getSettlement(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}")
    fun getSettlementsByCompany(@PathVariable companyId: String): ResponseEntity<List<SettlementResult>> {
        val result = settlementService.getSettlementsByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    @GetMapping("/channel/{channelId}")
    fun getSettlementsByChannel(@PathVariable channelId: String): ResponseEntity<List<SettlementResult>> {
        val result = settlementService.getSettlementsByChannel(channelId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}/period")
    fun getSettlementsByCompanyAndPeriod(
        @PathVariable companyId: String,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<List<SettlementResult>> {
        val result = settlementService.getSettlementsByCompanyAndPeriod(
            UUID.fromString(companyId),
            year,
            month
        )
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}/status/{status}")
    fun getSettlementsByCompanyAndStatus(
        @PathVariable companyId: String,
        @PathVariable status: SettlementStatus
    ): ResponseEntity<List<SettlementResult>> {
        val result = settlementService.getSettlementsByCompanyAndStatus(
            UUID.fromString(companyId),
            status
        )
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/items")
    fun addSettlementItem(
        @PathVariable id: String,
        @RequestBody command: AddSettlementItemCommand
    ): ResponseEntity<SettlementResult> {
        val result = settlementService.addSettlementItem(UUID.fromString(id), command)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/calculate")
    fun calculateSettlement(@PathVariable id: String): ResponseEntity<SettlementResult> {
        val result = settlementService.calculateSettlement(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/confirm")
    fun confirmSettlement(@PathVariable id: String): ResponseEntity<SettlementResult> {
        val result = settlementService.confirmSettlement(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/paid")
    fun markSettlementAsPaid(@PathVariable id: String): ResponseEntity<SettlementResult> {
        val result = settlementService.markSettlementAsPaid(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }
}
