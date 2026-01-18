package com.oms.channel.domain

import com.oms.channel.domain.vo.MappingRole
import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.event.DomainEvent
import jakarta.persistence.*
import java.util.UUID

/**
 * ChannelWarehouseMapping aggregate root
 * Represents the association between a channel and a warehouse
 */
@Entity
@Table(
    name = "channel_warehouse_mappings",
    indexes = [
        Index(name = "idx_mapping_company", columnList = "company_id"),
        Index(name = "idx_mapping_channel", columnList = "channel_id"),
        Index(name = "idx_mapping_warehouse", columnList = "warehouse_id"),
        Index(name = "idx_mapping_role", columnList = "role")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_channel_warehouse",
            columnNames = ["channel_id", "warehouse_id"]
        )
    ]
)
class ChannelWarehouseMapping private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "channel_id", nullable = false, length = 36)
    val channelId: String,

    @Column(name = "warehouse_id", nullable = false, length = 36)
    val warehouseId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: MappingRole,

    @Column(name = "priority", nullable = false)
    var priority: Int

) : CompanyAwareEntity() {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(
            companyId: String,
            channelId: String,
            warehouseId: String,
            role: MappingRole,
            priority: Int = 0
        ): ChannelWarehouseMapping {
            require(priority >= 0) { "Priority must be non-negative" }

            val mappingId = UUID.randomUUID().toString()

            val mapping = ChannelWarehouseMapping(
                id = mappingId,
                channelId = channelId,
                warehouseId = warehouseId,
                role = role,
                priority = priority
            )
            mapping.assignToCompany(companyId)

            return mapping
        }
    }

    /**
     * Update the role of this mapping
     */
    fun updateRole(newRole: MappingRole) {
        this.role = newRole
    }

    /**
     * Update the priority of this mapping
     */
    fun updatePriority(newPriority: Int) {
        require(newPriority >= 0) { "Priority must be non-negative" }
        this.priority = newPriority
    }

    fun isPrimary(): Boolean = role == MappingRole.PRIMARY

    fun isRegional(): Boolean = role == MappingRole.REGIONAL

    fun isBackup(): Boolean = role == MappingRole.BACKUP

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
        if (other !is ChannelWarehouseMapping) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
