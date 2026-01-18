package com.oms.api.controller

import com.oms.application.automation.AutomationRuleService
import com.oms.application.automation.dto.AutomationRuleResult
import com.oms.application.automation.dto.CreateRuleCommand
import com.oms.application.automation.dto.UpdateRuleCommand
import com.oms.automation.domain.vo.TriggerType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST API controller for Automation Rule management
 */
@RestController
@RequestMapping("/api/v1/automation/rules")
class AutomationRuleController(
    private val automationRuleService: AutomationRuleService
) {

    /**
     * Create a new automation rule
     */
    @PostMapping
    fun createRule(@RequestBody command: CreateRuleCommand): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.createRule(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/{id}")
    fun getRule(@PathVariable id: String): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.getRule(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    /**
     * Get all rules for a company
     */
    @GetMapping("/company/{companyId}")
    fun getRulesByCompany(@PathVariable companyId: String): ResponseEntity<List<AutomationRuleResult>> {
        val result = automationRuleService.getRulesByCompany(UUID.fromString(companyId))
        return ResponseEntity.ok(result)
    }

    /**
     * Get active rules by trigger type for a company
     */
    @GetMapping("/company/{companyId}/trigger/{triggerType}")
    fun getRulesByTrigger(
        @PathVariable companyId: String,
        @PathVariable triggerType: TriggerType
    ): ResponseEntity<List<AutomationRuleResult>> {
        val result = automationRuleService.getActiveRulesByTrigger(UUID.fromString(companyId), triggerType)
        return ResponseEntity.ok(result)
    }

    /**
     * Update rule
     */
    @PutMapping("/{id}")
    fun updateRule(
        @PathVariable id: String,
        @RequestBody command: UpdateRuleCommand
    ): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.updateRule(UUID.fromString(id), command)
        return ResponseEntity.ok(result)
    }

    /**
     * Enable a rule
     */
    @PostMapping("/{id}/enable")
    fun enableRule(@PathVariable id: String): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.enableRule(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    /**
     * Disable a rule
     */
    @PostMapping("/{id}/disable")
    fun disableRule(@PathVariable id: String): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.disableRule(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }

    /**
     * Delete a rule
     */
    @DeleteMapping("/{id}")
    fun deleteRule(@PathVariable id: String): ResponseEntity<Void> {
        automationRuleService.deleteRule(UUID.fromString(id))
        return ResponseEntity.noContent().build()
    }

    /**
     * Record rule execution (typically called by internal automation engine)
     */
    @PostMapping("/{id}/execute")
    fun recordExecution(@PathVariable id: String): ResponseEntity<AutomationRuleResult> {
        val result = automationRuleService.recordRuleExecution(UUID.fromString(id))
        return ResponseEntity.ok(result)
    }
}
