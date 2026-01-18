package com.oms.strategy.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class ReadinessItem(
    val id: String,
    @Enumerated(EnumType.STRING)
    val category: ReadinessCategory,
    val description: String,
    val completed: Boolean
)
