package com.oms.identity.domain

import com.oms.core.domain.CompanyAwareEntity
import com.oms.core.event.DomainEvent
import com.oms.identity.domain.event.UserCreatedEvent
import com.oms.identity.domain.event.UserRoleChangedEvent
import com.oms.identity.domain.event.UserStatusChangedEvent
import jakarta.persistence.*
import java.util.UUID

/**
 * User entity
 * Represents a user belonging to a company
 */
@Entity
@Table(
    name = "user",
    indexes = [
        Index(name = "idx_user_company", columnList = "company_id"),
        Index(name = "idx_user_email", columnList = "email"),
        Index(name = "idx_user_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_company_email", columnNames = ["company_id", "email"])
    ]
)
class User private constructor(
    @Id
    @Column(name = "id", length = 36)
    val id: String,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "email", nullable = false, length = 255))
    var email: Email,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: UserRole,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: UserStatus = UserStatus.INVITED
) : CompanyAwareEntity() {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(
            companyId: String,
            email: String,
            name: String,
            role: UserRole
        ): User {
            require(name.isNotBlank()) { "User name cannot be blank" }

            val user = User(
                id = UUID.randomUUID().toString(),
                email = Email(email),
                name = name,
                role = role,
                status = UserStatus.INVITED
            )
            user.assignToCompany(companyId)

            user.registerEvent(
                UserCreatedEvent(
                    userId = user.id,
                    companyId = companyId,
                    email = email,
                    name = name,
                    role = role
                )
            )

            return user
        }

        /**
         * Create the company owner - special case
         */
        fun createOwner(
            companyId: String,
            email: String,
            name: String
        ): User {
            val user = User(
                id = UUID.randomUUID().toString(),
                email = Email(email),
                name = name,
                role = UserRole.OWNER,
                status = UserStatus.ACTIVE
            )
            user.assignToCompany(companyId)

            user.registerEvent(
                UserCreatedEvent(
                    userId = user.id,
                    companyId = companyId,
                    email = email,
                    name = name,
                    role = UserRole.OWNER
                )
            )

            return user
        }
    }

    /**
     * Activate the user (after accepting invitation)
     */
    fun activate() {
        require(status == UserStatus.INVITED) {
            "Only invited users can be activated"
        }
        val previousStatus = status
        status = UserStatus.ACTIVE
        registerEvent(
            UserStatusChangedEvent(
                userId = id,
                companyId = companyId,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    /**
     * Deactivate the user
     */
    fun deactivate() {
        require(status == UserStatus.ACTIVE) {
            "Only active users can be deactivated"
        }
        require(role != UserRole.OWNER) {
            "Owner cannot be deactivated"
        }
        val previousStatus = status
        status = UserStatus.INACTIVE
        registerEvent(
            UserStatusChangedEvent(
                userId = id,
                companyId = companyId,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    /**
     * Reactivate an inactive user
     */
    fun reactivate() {
        require(status == UserStatus.INACTIVE) {
            "Only inactive users can be reactivated"
        }
        val previousStatus = status
        status = UserStatus.ACTIVE
        registerEvent(
            UserStatusChangedEvent(
                userId = id,
                companyId = companyId,
                previousStatus = previousStatus,
                newStatus = status
            )
        )
    }

    /**
     * Change user role
     * Owner role transfer is not allowed through this method
     */
    fun changeRole(newRole: UserRole) {
        require(role != UserRole.OWNER) {
            "Owner role cannot be changed"
        }
        require(newRole != UserRole.OWNER) {
            "Cannot assign owner role through this method"
        }
        val previousRole = role
        role = newRole
        registerEvent(
            UserRoleChangedEvent(
                userId = id,
                companyId = companyId,
                previousRole = previousRole,
                newRole = newRole
            )
        )
    }

    fun updateEmail(newEmail: String) {
        this.email = Email(newEmail)
    }

    fun updateName(newName: String) {
        require(newName.isNotBlank()) { "User name cannot be blank" }
        this.name = newName
    }

    fun isActive(): Boolean = status == UserStatus.ACTIVE

    fun canLogin(): Boolean = status == UserStatus.ACTIVE

    fun isOwner(): Boolean = role == UserRole.OWNER

    fun canManageUsers(): Boolean = role == UserRole.OWNER || role == UserRole.EDITOR

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
        if (other !is User) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Email value object with validation
 */
@Embeddable
data class Email(
    @Column(name = "email")
    val value: String
) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.contains("@")) { "Invalid email format" }
        require(value.length <= 255) { "Email is too long" }
    }

    override fun toString(): String = value
}

/**
 * User role enum
 */
enum class UserRole {
    OWNER,   // Company owner - full access, 1 per company
    EDITOR,  // Can create/edit data
    VIEWER   // Read-only access
}

/**
 * User status enum
 */
enum class UserStatus {
    ACTIVE,   // User is active and can login
    INACTIVE, // User is deactivated
    INVITED   // User has been invited but hasn't accepted yet
}
