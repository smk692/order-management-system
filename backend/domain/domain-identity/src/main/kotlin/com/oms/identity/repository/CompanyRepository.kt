package com.oms.identity.repository

import com.oms.identity.domain.Company
import com.oms.identity.domain.CompanyStatus

/**
 * Repository interface for Company aggregate
 * Implementation will be in infrastructure module
 */
interface CompanyRepository {

    fun save(company: Company): Company

    fun findById(id: String): Company?

    fun findByBusinessNumber(businessNumber: String): Company?

    fun findAllByStatus(status: CompanyStatus): List<Company>

    fun existsByBusinessNumber(businessNumber: String): Boolean

    fun delete(company: Company)
}
