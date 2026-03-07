package com.oms.core.event

interface DomainEventPublisher {
    fun publish(event: DomainEvent)

    fun publishAll(events: List<DomainEvent>)
}
