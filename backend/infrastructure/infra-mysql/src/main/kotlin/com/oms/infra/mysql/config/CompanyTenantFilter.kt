package com.oms.infra.mysql.config

import jakarta.persistence.EntityManager
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.hibernate.Session
import org.springframework.stereotype.Component

/**
 * Aspect to enable company-based tenant filtering
 * This automatically filters queries by company_id for multi-tenancy
 */
@Aspect
@Component
class CompanyTenantFilterAspect(
    private val entityManager: EntityManager,
    private val tenantContext: TenantContext
) {

    @Before("execution(* com.oms.infra.mysql.repository.*.*(..))")
    fun enableTenantFilter() {
        tenantContext.getCurrentCompanyId()?.let { companyId ->
            val session = entityManager.unwrap(Session::class.java)
            val filter = session.enableFilter("companyFilter")
            filter.setParameter("companyId", companyId)
        }
    }
}

/**
 * Holds the current tenant (company) context
 * Should be set per request in a web filter or interceptor
 */
@Component
class TenantContext {

    private val currentCompanyId = ThreadLocal<String?>()

    fun setCurrentCompanyId(companyId: String?) {
        currentCompanyId.set(companyId)
    }

    fun getCurrentCompanyId(): String? {
        return currentCompanyId.get()
    }

    fun clear() {
        currentCompanyId.remove()
    }
}
