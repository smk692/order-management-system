package com.oms.inventory.domain.vo

import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
@JvmInline
value class MovementId(val value: UUID) : Serializable {
    companion object {
        fun generate() = MovementId(UUID.randomUUID())
        fun from(value: String) = MovementId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
