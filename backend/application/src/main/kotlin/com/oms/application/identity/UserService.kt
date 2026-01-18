package com.oms.application.identity

import com.oms.core.exception.BusinessRuleException
import com.oms.core.exception.EntityNotFoundException
import com.oms.core.exception.ErrorCode
import com.oms.identity.domain.User
import com.oms.identity.domain.UserRole
import com.oms.identity.domain.UserStatus
import com.oms.identity.repository.CompanyRepository
import com.oms.identity.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application service for User operations
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val companyRepository: CompanyRepository
) {

    /**
     * Invite a new user to a company
     */
    fun inviteUser(command: InviteUserCommand): UserResult {
        // Validate company exists and is active
        val company = companyRepository.findById(command.companyId)
            ?: throw EntityNotFoundException("Company", command.companyId)

        if (!company.isActive()) {
            throw BusinessRuleException("Cannot invite users to inactive company")
        }

        // Check if email already exists in company
        if (userRepository.existsByCompanyIdAndEmail(command.companyId, command.email)) {
            throw BusinessRuleException(
                "Email already exists in company: ${command.email}",
                ErrorCode.DUPLICATE_EMAIL
            )
        }

        // Cannot create another owner
        if (command.role == UserRole.OWNER) {
            throw BusinessRuleException("Cannot invite user as owner")
        }

        val user = User.create(
            companyId = command.companyId,
            email = command.email,
            name = command.name,
            role = command.role
        )
        val savedUser = userRepository.save(user)

        return toUserResult(savedUser)
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    fun getUser(userId: String): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        return toUserResult(user)
    }

    /**
     * Get users by company ID
     */
    @Transactional(readOnly = true)
    fun getUsersByCompany(companyId: String): List<UserResult> {
        return userRepository.findByCompanyId(companyId)
            .map { toUserResult(it) }
    }

    /**
     * Activate a user (after accepting invitation)
     */
    fun activateUser(userId: String): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        user.activate()
        val updatedUser = userRepository.save(user)

        return toUserResult(updatedUser)
    }

    /**
     * Deactivate a user
     */
    fun deactivateUser(userId: String): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        user.deactivate()
        val updatedUser = userRepository.save(user)

        return toUserResult(updatedUser)
    }

    /**
     * Reactivate a user
     */
    fun reactivateUser(userId: String): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        user.reactivate()
        val updatedUser = userRepository.save(user)

        return toUserResult(updatedUser)
    }

    /**
     * Change user role
     */
    fun changeUserRole(userId: String, newRole: UserRole): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        user.changeRole(newRole)
        val updatedUser = userRepository.save(user)

        return toUserResult(updatedUser)
    }

    /**
     * Update user profile
     */
    fun updateUserProfile(userId: String, command: UpdateUserCommand): UserResult {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        command.name?.let { user.updateName(it) }
        command.email?.let { newEmail ->
            // Check for duplicate email
            val existingUser = userRepository.findByCompanyIdAndEmail(user.companyId, newEmail)
            if (existingUser != null && existingUser.id != userId) {
                throw BusinessRuleException(
                    "Email already exists in company: $newEmail",
                    ErrorCode.DUPLICATE_EMAIL
                )
            }
            user.updateEmail(newEmail)
        }

        val updatedUser = userRepository.save(user)
        return toUserResult(updatedUser)
    }

    private fun toUserResult(user: User): UserResult {
        return UserResult(
            id = user.id,
            companyId = user.companyId,
            email = user.email.value,
            name = user.name,
            role = user.role,
            status = user.status
        )
    }
}

/**
 * Command to invite a user
 */
data class InviteUserCommand(
    val companyId: String,
    val email: String,
    val name: String,
    val role: UserRole = UserRole.VIEWER
)

/**
 * Command to update user profile
 */
data class UpdateUserCommand(
    val name: String? = null,
    val email: String? = null
)

/**
 * Result DTO for User
 */
data class UserResult(
    val id: String,
    val companyId: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val status: UserStatus
)
