package com.oms.claim.domain.vo

import jakarta.persistence.Embeddable

@Embeddable
@JvmInline
value class ClaimId(val value: String) {
    init {
        require(value.matches(Regex("CLM-\\d{8}-\\d{3}"))) {
            "ClaimId must be in format CLM-yyyyMMdd-nnn"
        }
    }
}
