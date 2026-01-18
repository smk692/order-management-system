package com.oms.infra.mysql.repository

import com.oms.identity.domain.Company
import com.oms.identity.domain.CompanyStatus
import com.oms.identity.repository.CompanyRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for Company
 */
interface CompanyJpaRepositoryInterface : JpaRepository<Company, String> {

    fun findByBusinessNumber(businessNumber: String): Company?

    fun findAllByStatus(status: CompanyStatus): List<Company>

    fun existsByBusinessNumber(businessNumber: String): Boolean
}

/**
 * Implementation of CompanyRepository using Spring Data JPA
 */
@Repository
class CompanyJpaRepository(
    private val jpaRepository: CompanyJpaRepositoryInterface
) : CompanyRepository {

    override fun save(company: Company): Company {
        return jpaRepository.save(company)
    }

    override fun findById(id: String): Company? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByBusinessNumber(businessNumber: String): Company? {
        return jpaRepository.findByBusinessNumber(businessNumber)
    }

    override fun findAllByStatus(status: CompanyStatus): List<Company> {
        return jpaRepository.findAllByStatus(status)
    }

    override fun existsByBusinessNumber(businessNumber: String): Boolean {
        return jpaRepository.existsByBusinessNumber(businessNumber)
    }

    override fun delete(company: Company) {
        jpaRepository.delete(company)
    }
}
