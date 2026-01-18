package com.oms.api.controller.strategy

import com.oms.application.strategy.GlobalReadinessService
import com.oms.strategy.domain.GlobalReadiness
import com.oms.strategy.domain.vo.ReadinessCategory
import com.oms.strategy.domain.vo.ReadinessItem
import com.oms.strategy.domain.vo.ReadinessStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/readiness")
class GlobalReadinessController(
    private val readinessService: GlobalReadinessService
) {

    @PostMapping
    fun createReadiness(@RequestBody request: CreateReadinessRequest): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.createReadiness(
            companyId = request.companyId,
            country = request.country,
            initialChecklist = request.initialChecklist
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(readiness)
    }

    @GetMapping("/{id}")
    fun getReadiness(@PathVariable id: UUID): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.getReadiness(id)
        return ResponseEntity.ok(readiness)
    }

    @GetMapping("/company/{companyId}")
    fun getReadinessByCompany(@PathVariable companyId: String): ResponseEntity<List<GlobalReadiness>> {
        val readiness = readinessService.getReadinessByCompany(companyId)
        return ResponseEntity.ok(readiness)
    }

    @GetMapping("/company/{companyId}/country/{country}")
    fun getReadinessByCountry(
        @PathVariable companyId: String,
        @PathVariable country: String
    ): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.getReadinessByCountry(companyId, country)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(readiness)
    }

    @GetMapping("/company/{companyId}/status/{status}")
    fun getReadinessByStatus(
        @PathVariable companyId: String,
        @PathVariable status: ReadinessStatus
    ): ResponseEntity<List<GlobalReadiness>> {
        val readiness = readinessService.getReadinessByStatus(companyId, status)
        return ResponseEntity.ok(readiness)
    }

    @PutMapping("/{id}/checklist")
    fun updateChecklistItem(
        @PathVariable id: UUID,
        @RequestBody request: UpdateChecklistItemRequest
    ): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.updateChecklistItem(
            readinessId = id,
            itemId = request.itemId,
            category = request.category,
            description = request.description,
            completed = request.completed
        )
        return ResponseEntity.ok(readiness)
    }

    @PostMapping("/{id}/checklist")
    fun addChecklistItem(
        @PathVariable id: UUID,
        @RequestBody request: AddChecklistItemRequest
    ): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.addChecklistItem(
            readinessId = id,
            itemId = request.itemId,
            category = request.category,
            description = request.description
        )
        return ResponseEntity.ok(readiness)
    }

    @DeleteMapping("/{id}/checklist/{itemId}")
    fun removeChecklistItem(
        @PathVariable id: UUID,
        @PathVariable itemId: String
    ): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.removeChecklistItem(id, itemId)
        return ResponseEntity.ok(readiness)
    }

    @PostMapping("/{id}/launch")
    fun launchCountry(@PathVariable id: UUID): ResponseEntity<GlobalReadiness> {
        val readiness = readinessService.launchCountry(id)
        return ResponseEntity.ok(readiness)
    }
}

data class CreateReadinessRequest(
    val companyId: String,
    val country: String,
    val initialChecklist: List<ReadinessItem> = emptyList()
)

data class UpdateChecklistItemRequest(
    val itemId: String,
    val category: ReadinessCategory,
    val description: String,
    val completed: Boolean
)

data class AddChecklistItemRequest(
    val itemId: String,
    val category: ReadinessCategory,
    val description: String
)
