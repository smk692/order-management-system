package com.oms.api.controller

import com.oms.application.claim.ClaimService
import com.oms.application.claim.dto.ClaimResult
import com.oms.application.claim.dto.CompleteClaimCommand
import com.oms.application.claim.dto.CreateClaimCommand
import com.oms.application.claim.dto.RejectClaimCommand
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/claims")
class ClaimController(
    private val claimService: ClaimService
) {

    @PostMapping
    fun createClaim(@RequestBody command: CreateClaimCommand): ResponseEntity<ClaimResult> {
        val result = claimService.createClaim(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @GetMapping("/{id}")
    fun getClaim(@PathVariable id: String): ResponseEntity<ClaimResult> {
        val result = claimService.getClaim(id)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/number/{claimNumber}")
    fun getClaimByNumber(@PathVariable claimNumber: String): ResponseEntity<ClaimResult> {
        val result = claimService.getClaimByNumber(claimNumber)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}")
    fun getClaimsByCompany(@PathVariable companyId: String): ResponseEntity<List<ClaimResult>> {
        val result = claimService.getClaimsByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    @GetMapping("/order/{orderId}")
    fun getClaimsByOrder(@PathVariable orderId: String): ResponseEntity<List<ClaimResult>> {
        val result = claimService.getClaimsByOrder(orderId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}/status/{status}")
    fun getClaimsByCompanyAndStatus(
        @PathVariable companyId: String,
        @PathVariable status: ClaimStatus
    ): ResponseEntity<List<ClaimResult>> {
        val result = claimService.getClaimsByCompanyAndStatus(UUID.fromString(companyId), status)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/company/{companyId}/type/{type}")
    fun getClaimsByCompanyAndType(
        @PathVariable companyId: String,
        @PathVariable type: ClaimType
    ): ResponseEntity<List<ClaimResult>> {
        val result = claimService.getClaimsByCompanyAndType(UUID.fromString(companyId), type)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/process")
    fun startProcessing(@PathVariable id: String): ResponseEntity<ClaimResult> {
        val result = claimService.startProcessing(id)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/complete")
    fun completeClaim(
        @PathVariable id: String,
        @RequestBody command: CompleteClaimCommand
    ): ResponseEntity<ClaimResult> {
        val result = claimService.completeClaim(id, command)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/reject")
    fun rejectClaim(
        @PathVariable id: String,
        @RequestBody command: RejectClaimCommand
    ): ResponseEntity<ClaimResult> {
        val result = claimService.rejectClaim(id, command)
        return ResponseEntity.ok(result)
    }
}
