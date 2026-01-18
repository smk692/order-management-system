package com.oms.settlement.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.*

@Embeddable
@JvmInline
value class SettlementId(
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    val value: UUID = UUID.randomUUID()
) : Serializable {
    override fun toString(): String = value.toString()
}
