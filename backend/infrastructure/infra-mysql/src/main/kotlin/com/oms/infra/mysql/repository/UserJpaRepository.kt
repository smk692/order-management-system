package com.oms.infra.mysql.repository

import com.oms.identity.domain.User
import com.oms.identity.domain.UserRole
import com.oms.identity.domain.UserStatus
import com.oms.identity.repository.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for User
 */
interface UserJpaRepositoryInterface : JpaRepository<User, String> {

    fun findByCompanyId(companyId: String): List<User>

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND u.email.value = :email")
    fun findByCompanyIdAndEmail(@Param("companyId") companyId: String, @Param("email") email: String): User?

    fun findByCompanyIdAndStatus(companyId: String, status: UserStatus): List<User>

    fun findByCompanyIdAndRole(companyId: String, role: UserRole): List<User>

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND u.role = 'OWNER'")
    fun findOwnerByCompanyId(@Param("companyId") companyId: String): User?

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.companyId = :companyId AND u.email.value = :email")
    fun existsByCompanyIdAndEmail(@Param("companyId") companyId: String, @Param("email") email: String): Boolean

    fun countByCompanyId(companyId: String): Long
}

/**
 * Implementation of UserRepository using Spring Data JPA
 */
@Repository
class UserJpaRepository(
    private val jpaRepository: UserJpaRepositoryInterface
) : UserRepository {

    override fun save(user: User): User {
        return jpaRepository.save(user)
    }

    override fun findById(id: String): User? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByCompanyId(companyId: String): List<User> {
        return jpaRepository.findByCompanyId(companyId)
    }

    override fun findByCompanyIdAndEmail(companyId: String, email: String): User? {
        return jpaRepository.findByCompanyIdAndEmail(companyId, email)
    }

    override fun findByCompanyIdAndStatus(companyId: String, status: UserStatus): List<User> {
        return jpaRepository.findByCompanyIdAndStatus(companyId, status)
    }

    override fun findByCompanyIdAndRole(companyId: String, role: UserRole): List<User> {
        return jpaRepository.findByCompanyIdAndRole(companyId, role)
    }

    override fun findOwnerByCompanyId(companyId: String): User? {
        return jpaRepository.findOwnerByCompanyId(companyId)
    }

    override fun existsByCompanyIdAndEmail(companyId: String, email: String): Boolean {
        return jpaRepository.existsByCompanyIdAndEmail(companyId, email)
    }

    override fun countByCompanyId(companyId: String): Long {
        return jpaRepository.countByCompanyId(companyId)
    }

    override fun delete(user: User) {
        jpaRepository.delete(user)
    }
}
