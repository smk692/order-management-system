package com.oms.automation.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class Trigger(
    @Enumerated(EnumType.STRING)
    val type: TriggerType,
    val config: String? = null
)
