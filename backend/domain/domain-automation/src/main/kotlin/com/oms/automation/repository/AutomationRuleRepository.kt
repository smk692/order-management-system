package com.oms.automation.repository

import com.oms.automation.domain.AutomationRule
import com.oms.automation.domain.vo.TriggerType
import java.util.UUID

interface AutomationRuleRepository {
    fun save(rule: AutomationRule): AutomationRule
    fun findById(id: UUID): AutomationRule?
    fun findByCompanyId(companyId: UUID): List<AutomationRule>
    fun findByCompanyIdAndEnabled(companyId: UUID, enabled: Boolean): List<AutomationRule>
    fun findByCompanyIdAndTriggerType(companyId: UUID, triggerType: TriggerType): List<AutomationRule>
    fun deleteById(id: UUID)
}
