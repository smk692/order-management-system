package com.oms.automation.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class Condition(
    val field: String,
    @Enumerated(EnumType.STRING)
    val operator: ConditionOperator,
    val value: String
)
