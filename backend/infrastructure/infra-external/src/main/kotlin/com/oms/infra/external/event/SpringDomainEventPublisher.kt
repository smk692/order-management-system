package com.oms.infra.external.event

import com.oms.core.event.DomainEvent
import com.oms.core.event.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(
    private val publisher: ApplicationEventPublisher
) : DomainEventPublisher {

    override fun publish(event: DomainEvent) {
        publisher.publishEvent(event)
    }

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
