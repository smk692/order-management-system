package com.oms.channel.domain.vo

import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
@JvmInline
value class MappingId(val value: UUID) {
    companion object {
        fun generate(): MappingId = MappingId(UUID.randomUUID())
    }

    override fun toString(): String = value.toString()
}
