package com.oms.automation.domain

import com.oms.automation.domain.vo.*
import com.oms.core.domain.CompanyAwareEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "automation_rules")
class AutomationRule(
    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Embedded
    var trigger: Trigger,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var priority: Int = 0,

    @Column(nullable = false)
    var executionCount: Long = 0,

    @Column
    var lastExecutedAt: LocalDateTime? = null,

    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString()
) : CompanyAwareEntity() {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "automation_rule_conditions",
        joinColumns = [JoinColumn(name = "rule_id")]
    )
    val conditions: MutableList<Condition> = mutableListOf()

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "automation_rule_actions",
        joinColumns = [JoinColumn(name = "rule_id")]
    )
    val actions: MutableList<Action> = mutableListOf()

    fun enable() {
        enabled = true
    }

    fun disable() {
        enabled = false
    }

    fun recordExecution() {
        executionCount++
        lastExecutedAt = LocalDateTime.now()
    }

    fun addCondition(condition: Condition) {
        conditions.add(condition)
    }

    fun addAction(action: Action) {
        actions.add(action)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AutomationRule) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
