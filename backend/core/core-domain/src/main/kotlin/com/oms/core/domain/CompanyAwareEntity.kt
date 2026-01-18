package com.oms.core.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass

/**
 * Base class for entities that belong to a company (multi-tenancy support)
 * Works with Hibernate Filter for automatic company isolation
 */
@MappedSuperclass
abstract class CompanyAwareEntity : AuditableEntity() {

    @Column(name = "company_id", nullable = false, updatable = false, length = 36)
    lateinit var companyId: String
        protected set

    fun assignToCompany(companyId: String) {
        require(companyId.isNotBlank()) { "Company ID cannot be blank" }
        this.companyId = companyId
    }

    fun belongsTo(companyId: String): Boolean {
        return this.companyId == companyId
    }
}
