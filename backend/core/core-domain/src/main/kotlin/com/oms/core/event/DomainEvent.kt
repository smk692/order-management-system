package com.oms.core.event

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events
 */
abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now()
) {
    abstract val aggregateId: String
    abstract val aggregateType: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainEvent) return false
        return eventId == other.eventId
    }

    override fun hashCode(): Int = eventId.hashCode()
}

/**
 * Base class for aggregate roots that can publish domain events
 */
abstract class AggregateRoot {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    protected fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun clearEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()
}
