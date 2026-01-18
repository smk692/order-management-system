package com.oms.infra.mysql.automation

import com.oms.automation.domain.AutomationRule
import com.oms.automation.domain.vo.TriggerType
import com.oms.automation.repository.AutomationRuleRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for AutomationRule
 */
interface AutomationRuleJpaRepositoryInterface : JpaRepository<AutomationRule, String> {
    fun findByCompanyId(companyId: String): List<AutomationRule>
    fun findByCompanyIdAndEnabled(companyId: String, enabled: Boolean): List<AutomationRule>
    fun findByCompanyIdAndTrigger_Type(companyId: String, triggerType: TriggerType): List<AutomationRule>
}

/**
 * Implementation of AutomationRuleRepository using Spring Data JPA
 */
@Repository
class JpaAutomationRuleRepository(
    private val jpaRepository: AutomationRuleJpaRepositoryInterface
) : AutomationRuleRepository {

    override fun save(rule: AutomationRule): AutomationRule {
        return jpaRepository.save(rule)
    }

    override fun findById(id: UUID): AutomationRule? {
        return jpaRepository.findById(id.toString()).orElse(null)
    }

    override fun findByCompanyId(companyId: UUID): List<AutomationRule> {
        return jpaRepository.findByCompanyId(companyId.toString())
    }

    override fun findByCompanyIdAndEnabled(companyId: UUID, enabled: Boolean): List<AutomationRule> {
        return jpaRepository.findByCompanyIdAndEnabled(companyId.toString(), enabled)
    }

    override fun findByCompanyIdAndTriggerType(companyId: UUID, triggerType: TriggerType): List<AutomationRule> {
        return jpaRepository.findByCompanyIdAndTrigger_Type(companyId.toString(), triggerType)
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id.toString())
    }
}
