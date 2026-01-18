package com.oms.channel.domain

import com.oms.channel.domain.vo.ChannelCredentials
import com.oms.channel.domain.vo.ChannelStatus
import com.oms.channel.domain.vo.ChannelType
import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.event.DomainEvent
import jakarta.persistence.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Channel aggregate root
 * Represents a sales channel in the OMS
 */
@Entity
@Table(
    name = "channels",
    indexes = [
        Index(name = "idx_channel_company", columnList = "company_id"),
        Index(name = "idx_channel_type", columnList = "type"),
        Index(name = "idx_channel_status", columnList = "status")
    ]
)
class Channel private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    val type: ChannelType,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ChannelStatus = ChannelStatus.DISCONNECTED,

    @Embedded
    var credentials: ChannelCredentials,

    @Column(name = "api_endpoint", length = 500)
    var apiEndpoint: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

) : CompanyAwareEntity() {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        fun create(
            companyId: String,
            name: String,
            type: ChannelType,
            credentials: ChannelCredentials,
            apiEndpoint: String? = null,
            description: String? = null
        ): Channel {
            val channelId = generateChannelId()

            val channel = Channel(
                id = channelId,
                name = name,
                type = type,
                status = ChannelStatus.DISCONNECTED,
                credentials = credentials,
                apiEndpoint = apiEndpoint,
                description = description
            )
            channel.assignToCompany(companyId)

            return channel
        }

        private fun generateChannelId(): String {
            val dateStr = LocalDate.now().format(dateFormatter)
            val uniquePart = UUID.randomUUID().toString().substring(0, 8).uppercase()
            return "CH-$dateStr-$uniquePart"
        }
    }

    /**
     * Connect the channel
     */
    fun connect() {
        require(status != ChannelStatus.CONNECTED) {
            "Channel is already connected"
        }
        status = ChannelStatus.CONNECTED
    }

    /**
     * Disconnect the channel
     */
    fun disconnect() {
        require(status == ChannelStatus.CONNECTED) {
            "Channel is not connected"
        }
        status = ChannelStatus.DISCONNECTED
    }

    /**
     * Update channel credentials
     */
    fun updateCredentials(newCredentials: ChannelCredentials) {
        this.credentials = newCredentials
        // May need to reconnect after credential update
        if (status == ChannelStatus.CONNECTED) {
            status = ChannelStatus.DISCONNECTED
        }
    }

    /**
     * Update channel information
     */
    fun update(
        name: String? = null,
        apiEndpoint: String? = null,
        description: String? = null
    ) {
        name?.let { this.name = it }
        apiEndpoint?.let { this.apiEndpoint = it }
        description?.let { this.description = it }
    }

    /**
     * Mark channel as error
     */
    fun markError() {
        status = ChannelStatus.ERROR
    }

    fun isConnected(): Boolean = status == ChannelStatus.CONNECTED

    fun isDisconnected(): Boolean = status == ChannelStatus.DISCONNECTED

    fun hasError(): Boolean = status == ChannelStatus.ERROR

    private fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun clearEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Channel) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
