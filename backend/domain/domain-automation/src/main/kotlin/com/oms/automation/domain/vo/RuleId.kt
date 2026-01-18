package com.oms.automation.domain.vo

import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
@JvmInline
value class RuleId(val value: UUID) {
    companion object {
        fun generate(): RuleId = RuleId(UUID.randomUUID())
    }
}
