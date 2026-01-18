package com.oms.identity.repository

import com.oms.identity.domain.User
import com.oms.identity.domain.UserRole
import com.oms.identity.domain.UserStatus

/**
 * Repository interface for User entity
 * Implementation will be in infrastructure module
 */
interface UserRepository {

    fun save(user: User): User

    fun findById(id: String): User?

    fun findByCompanyId(companyId: String): List<User>

    fun findByCompanyIdAndEmail(companyId: String, email: String): User?

    fun findByCompanyIdAndStatus(companyId: String, status: UserStatus): List<User>

    fun findByCompanyIdAndRole(companyId: String, role: UserRole): List<User>

    fun findOwnerByCompanyId(companyId: String): User?

    fun existsByCompanyIdAndEmail(companyId: String, email: String): Boolean

    fun countByCompanyId(companyId: String): Long

    fun delete(user: User)
}
