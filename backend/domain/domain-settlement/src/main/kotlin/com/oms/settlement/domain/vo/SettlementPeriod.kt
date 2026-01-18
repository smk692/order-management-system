package com.oms.settlement.domain.vo

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class SettlementPeriod(
    @Column(name = "period_year", nullable = false)
    val year: Int,

    @Column(name = "period_month", nullable = false)
    val month: Int
) : Serializable {

    init {
        require(year in 2000..2100) {
            "Year must be between 2000 and 2100"
        }
        require(month in 1..12) {
            "Month must be between 1 and 12"
        }
    }

    override fun toString(): String = String.format("%04d-%02d", year, month)
}
