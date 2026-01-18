package com.oms.infra.mongo.document

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

/**
 * MongoDB document for storing domain events
 */
@Document(collection = "domain_events")
@CompoundIndex(name = "aggregate_idx", def = "{'aggregateType': 1, 'aggregateId': 1}")
@CompoundIndex(name = "company_idx", def = "{'companyId': 1, 'occurredAt': -1}")
data class DomainEventDocument(
    @Id
    val id: String,

    @Indexed
    val eventType: String,

    val aggregateId: String,

    val aggregateType: String,

    @Indexed
    val companyId: String?,

    val occurredAt: Instant,

    val payload: Map<String, Any?>,

    val metadata: EventMetadata = EventMetadata()
)

/**
 * Event metadata
 */
data class EventMetadata(
    val version: Int = 1,
    val correlationId: String? = null,
    val causationId: String? = null,
    val userId: String? = null
)
