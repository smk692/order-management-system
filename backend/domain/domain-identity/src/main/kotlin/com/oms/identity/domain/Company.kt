package com.oms.identity.domain

import com.oms.core.domain.AuditableEntity
import com.oms.core.event.AggregateRoot
import com.oms.core.event.DomainEvent
import com.oms.identity.domain.event.CompanyCreatedEvent
import com.oms.identity.domain.event.CompanyStatusChangedEvent
import jakarta.persistence.*
import java.util.UUID

/**
 * Company aggregate root
 * Represents a tenant in the multi-tenant OMS system
 */
@Entity
@Table(
    name = "company",
    indexes = [
        Index(name = "idx_company_business_no", columnList = "business_number", unique = true),
        Index(name = "idx_company_status", columnList = "status")
    ]
)
class Company private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    val businessNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: CompanyStatus = CompanyStatus.ACTIVE
) : AuditableEntity() {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(
            name: String,
            businessNumber: String
        ): Company {
            require(name.isNotBlank()) { "Company name cannot be blank" }
            require(businessNumber.isNotBlank()) { "Business number cannot be blank" }

            val company = Company(
                id = UUID.randomUUID().toString(),
                name = name,
                businessNumber = businessNumber,
                status = CompanyStatus.ACTIVE
            )

            company.registerEvent(
                CompanyCreatedEvent(
                    companyId = company.id,
                    name = company.name,
                    businessNumber = company.businessNumber
                )
            )

            return company
        }
    }

    /**
     * Suspend the company - prevents user login
     */
    fun suspend() {
        require(status == CompanyStatus.ACTIVE) {
            "Only active companies can be suspended"
        }
        val previousStatus = status
        status = CompanyStatus.SUSPENDED
        registerEvent(
            CompanyStatusChangedEvent(
                companyId = id,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    /**
     * Reactivate a suspended company
     */
    fun reactivate() {
        require(status == CompanyStatus.SUSPENDED) {
            "Only suspended companies can be reactivated"
        }
        val previousStatus = status
        status = CompanyStatus.ACTIVE
        registerEvent(
            CompanyStatusChangedEvent(
                companyId = id,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    /**
     * Soft delete the company
     */
    fun delete() {
        require(status != CompanyStatus.DELETED) {
            "Company is already deleted"
        }
        val previousStatus = status
        status = CompanyStatus.DELETED
        registerEvent(
            CompanyStatusChangedEvent(
                companyId = id,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    fun updateName(newName: String) {
        require(newName.isNotBlank()) { "Company name cannot be blank" }
        this.name = newName
    }

    fun isActive(): Boolean = status == CompanyStatus.ACTIVE

    fun canUsersLogin(): Boolean = status == CompanyStatus.ACTIVE

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
        if (other !is Company) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Company status enum
 */
enum class CompanyStatus {
    ACTIVE,
    SUSPENDED,
    DELETED
}
