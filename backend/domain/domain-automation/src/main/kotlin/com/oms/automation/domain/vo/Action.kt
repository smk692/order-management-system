package com.oms.automation.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class Action(
    @Enumerated(EnumType.STRING)
    val type: ActionType,
    val config: String
)
