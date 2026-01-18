package com.oms.infra.mongo.repository

import com.oms.core.event.DomainEvent
import com.oms.infra.mongo.document.DomainEventDocument
import com.oms.infra.mongo.document.EventMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Spring Data MongoDB repository interface
 */
interface DomainEventMongoRepositoryInterface : MongoRepository<DomainEventDocument, String> {

    fun findByAggregateTypeAndAggregateId(aggregateType: String, aggregateId: String): List<DomainEventDocument>

    fun findByCompanyIdOrderByOccurredAtDesc(companyId: String): List<DomainEventDocument>
}

/**
 * Domain event repository for storing and retrieving domain events from MongoDB
 */
@Repository
class DomainEventMongoRepository(
    private val mongoTemplate: MongoTemplate,
    private val objectMapper: ObjectMapper
) {

    /**
     * Store a domain event
     */
    fun store(event: DomainEvent, companyId: String? = null, metadata: EventMetadata = EventMetadata()) {
        val document = DomainEventDocument(
            id = event.eventId,
            eventType = event::class.simpleName ?: "Unknown",
            aggregateId = event.aggregateId,
            aggregateType = event.aggregateType,
            companyId = companyId,
            occurredAt = event.occurredAt,
            payload = objectMapper.convertValue(event, Map::class.java) as Map<String, Any?>,
            metadata = metadata
        )
        mongoTemplate.save(document)
    }

    /**
     * Store multiple domain events
     */
    fun storeAll(events: List<DomainEvent>, companyId: String? = null, metadata: EventMetadata = EventMetadata()) {
        events.forEach { store(it, companyId, metadata) }
    }

    /**
     * Find events by aggregate
     */
    fun findByAggregate(aggregateType: String, aggregateId: String): List<DomainEventDocument> {
        val query = Query(
            Criteria.where("aggregateType").`is`(aggregateType)
                .and("aggregateId").`is`(aggregateId)
        ).with(Sort.by(Sort.Direction.ASC, "occurredAt"))

        return mongoTemplate.find(query, DomainEventDocument::class.java)
    }

    /**
     * Find events by company
     */
    fun findByCompanyId(companyId: String, limit: Int = 100): List<DomainEventDocument> {
        val query = Query(Criteria.where("companyId").`is`(companyId))
            .with(Sort.by(Sort.Direction.DESC, "occurredAt"))
            .limit(limit)

        return mongoTemplate.find(query, DomainEventDocument::class.java)
    }

    /**
     * Find events by type
     */
    fun findByEventType(eventType: String, limit: Int = 100): List<DomainEventDocument> {
        val query = Query(Criteria.where("eventType").`is`(eventType))
            .with(Sort.by(Sort.Direction.DESC, "occurredAt"))
            .limit(limit)

        return mongoTemplate.find(query, DomainEventDocument::class.java)
    }

    /**
     * Find events in time range
     */
    fun findByTimeRange(
        companyId: String?,
        startTime: Instant,
        endTime: Instant,
        limit: Int = 1000
    ): List<DomainEventDocument> {
        var criteria = Criteria.where("occurredAt").gte(startTime).lt(endTime)
        companyId?.let {
            criteria = criteria.and("companyId").`is`(it)
        }

        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.ASC, "occurredAt"))
            .limit(limit)

        return mongoTemplate.find(query, DomainEventDocument::class.java)
    }

    /**
     * Count events by aggregate
     */
    fun countByAggregate(aggregateType: String, aggregateId: String): Long {
        val query = Query(
            Criteria.where("aggregateType").`is`(aggregateType)
                .and("aggregateId").`is`(aggregateId)
        )
        return mongoTemplate.count(query, DomainEventDocument::class.java)
    }
}
