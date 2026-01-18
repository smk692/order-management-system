package com.oms.application.automation.dto

import com.oms.automation.domain.vo.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class TriggerDto(
    val type: TriggerType,
    val config: String? = null
)

data class ConditionDto(
    val field: String,
    val operator: ConditionOperator,
    val value: String
)

data class ActionDto(
    val type: ActionType,
    val config: String
)

data class CreateRuleCommand(
    val companyId: UUID,
    val name: String,
    val description: String? = null,
    val trigger: TriggerDto,
    val conditions: List<ConditionDto> = emptyList(),
    val actions: List<ActionDto> = emptyList(),
    val priority: Int = 0
)

data class UpdateRuleCommand(
    val name: String,
    val description: String? = null,
    val conditions: List<ConditionDto>,
    val actions: List<ActionDto>,
    val priority: Int
)

data class AutomationRuleResult(
    val id: UUID,
    val companyId: UUID,
    val name: String,
    val description: String?,
    val trigger: TriggerDto,
    val conditions: List<ConditionDto>,
    val actions: List<ActionDto>,
    val enabled: Boolean,
    val priority: Int,
    val executionCount: Long,
    val lastExecutedAt: LocalDateTime?,
    val createdAt: Instant,
    val updatedAt: Instant
)

// Extension functions for mapping
fun TriggerDto.toDomain(): Trigger {
    return Trigger(type = this.type, config = this.config)
}

fun ConditionDto.toDomain(): Condition {
    return Condition(field = this.field, operator = this.operator, value = this.value)
}

fun ActionDto.toDomain(): Action {
    return Action(type = this.type, config = this.config)
}

fun Trigger.toDto(): TriggerDto {
    return TriggerDto(type = this.type, config = this.config)
}

fun Condition.toDto(): ConditionDto {
    return ConditionDto(field = this.field, operator = this.operator, value = this.value)
}

fun Action.toDto(): ActionDto {
    return ActionDto(type = this.type, config = this.config)
}
