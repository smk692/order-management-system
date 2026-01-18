package com.oms.automation.domain.event

import com.oms.automation.domain.vo.TriggerType
import com.oms.core.event.DomainEvent
import java.time.LocalDateTime
import java.util.UUID

sealed class AutomationEvent : DomainEvent() {
    override val aggregateType: String = "AutomationRule"

    data class RuleCreatedEvent(
        override val aggregateId: String,
        val name: String,
        val triggerType: TriggerType
    ) : AutomationEvent()

    data class RuleEnabledEvent(
        override val aggregateId: String
    ) : AutomationEvent()

    data class RuleDisabledEvent(
        override val aggregateId: String
    ) : AutomationEvent()

    data class RuleExecutedEvent(
        override val aggregateId: String,
        val executionCount: Long,
        val executedAt: LocalDateTime
    ) : AutomationEvent()
}
