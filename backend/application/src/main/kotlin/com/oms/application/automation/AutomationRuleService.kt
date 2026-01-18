package com.oms.application.automation

import com.oms.application.automation.dto.*
import com.oms.automation.domain.AutomationRule
import com.oms.automation.domain.vo.TriggerType
import com.oms.automation.repository.AutomationRuleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Application service for Automation Rule management
 */
@Service
@Transactional
class AutomationRuleService(
    private val automationRuleRepository: AutomationRuleRepository
) {

    /**
     * Create a new automation rule
     */
    fun createRule(command: CreateRuleCommand): AutomationRuleResult {
        val rule = AutomationRule(
            name = command.name,
            description = command.description,
            trigger = command.trigger.toDomain(),
            priority = command.priority
        )

        rule.assignToCompany(command.companyId.toString())

        command.conditions.forEach { conditionDto ->
            rule.addCondition(conditionDto.toDomain())
        }

        command.actions.forEach { actionDto ->
            rule.addAction(actionDto.toDomain())
        }

        val savedRule = automationRuleRepository.save(rule)
        return toRuleResult(savedRule)
    }

    /**
     * Get rule by ID
     */
    @Transactional(readOnly = true)
    fun getRule(id: UUID): AutomationRuleResult {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")
        return toRuleResult(rule)
    }

    /**
     * Get all rules for a company
     */
    @Transactional(readOnly = true)
    fun getRulesByCompany(companyId: UUID): List<AutomationRuleResult> {
        return automationRuleRepository.findByCompanyId(companyId)
            .sortedByDescending { it.priority }
            .map { toRuleResult(it) }
    }

    /**
     * Get active rules by trigger type for a company
     */
    @Transactional(readOnly = true)
    fun getActiveRulesByTrigger(companyId: UUID, triggerType: TriggerType): List<AutomationRuleResult> {
        return automationRuleRepository.findByCompanyIdAndTriggerType(companyId, triggerType)
            .filter { it.enabled }
            .sortedByDescending { it.priority }
            .map { toRuleResult(it) }
    }

    /**
     * Enable a rule
     */
    fun enableRule(id: UUID): AutomationRuleResult {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")

        rule.enable()
        val savedRule = automationRuleRepository.save(rule)
        return toRuleResult(savedRule)
    }

    /**
     * Disable a rule
     */
    fun disableRule(id: UUID): AutomationRuleResult {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")

        rule.disable()
        val savedRule = automationRuleRepository.save(rule)
        return toRuleResult(savedRule)
    }

    /**
     * Update rule
     */
    fun updateRule(id: UUID, command: UpdateRuleCommand): AutomationRuleResult {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")

        rule.name = command.name
        rule.description = command.description
        rule.priority = command.priority

        // Clear and update conditions
        rule.conditions.clear()
        command.conditions.forEach { conditionDto ->
            rule.addCondition(conditionDto.toDomain())
        }

        // Clear and update actions
        rule.actions.clear()
        command.actions.forEach { actionDto ->
            rule.addAction(actionDto.toDomain())
        }

        val savedRule = automationRuleRepository.save(rule)
        return toRuleResult(savedRule)
    }

    /**
     * Delete a rule
     */
    fun deleteRule(id: UUID) {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")

        automationRuleRepository.deleteById(id)
    }

    /**
     * Record rule execution
     */
    fun recordRuleExecution(id: UUID): AutomationRuleResult {
        val rule = automationRuleRepository.findById(id)
            ?: throw IllegalArgumentException("Automation rule not found: $id")

        rule.recordExecution()
        val savedRule = automationRuleRepository.save(rule)
        return toRuleResult(savedRule)
    }

    private fun toRuleResult(rule: AutomationRule): AutomationRuleResult {
        return AutomationRuleResult(
            id = UUID.fromString(rule.id),
            companyId = UUID.fromString(rule.companyId),
            name = rule.name,
            description = rule.description,
            trigger = rule.trigger.toDto(),
            conditions = rule.conditions.map { it.toDto() },
            actions = rule.actions.map { it.toDto() },
            enabled = rule.enabled,
            priority = rule.priority,
            executionCount = rule.executionCount,
            lastExecutedAt = rule.lastExecutedAt,
            createdAt = rule.createdAt,
            updatedAt = rule.updatedAt
        )
    }
}
