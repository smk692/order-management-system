package com.oms.strategy.domain.vo

import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
@JvmInline
value class ReadinessId(val value: UUID = UUID.randomUUID()) {
    override fun toString(): String = value.toString()
}
