package com.oms.inventory.domain.vo

import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
@JvmInline
value class StockId(val value: UUID) : Serializable {
    companion object {
        fun generate() = StockId(UUID.randomUUID())
        fun from(value: String) = StockId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
