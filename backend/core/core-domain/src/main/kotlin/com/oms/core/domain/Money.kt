package com.oms.core.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Money Value Object
 * Represents monetary values with currency
 */
@Embeddable
data class Money(
    val amount: BigDecimal,
    @Enumerated(EnumType.STRING)
    val currency: Currency = Currency.KRW
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }

    companion object {
        val ZERO_KRW = Money(BigDecimal.ZERO, Currency.KRW)
        val ZERO_USD = Money(BigDecimal.ZERO, Currency.USD)

        fun of(amount: Number, currency: Currency = Currency.KRW): Money {
            return Money(BigDecimal.valueOf(amount.toDouble()), currency)
        }

        fun zero(currency: Currency = Currency.KRW): Money {
            return Money(BigDecimal.ZERO, currency)
        }
    }

    fun add(other: Money): Money {
        require(currency == other.currency) { "Cannot add money with different currencies" }
        return Money(amount.add(other.amount), currency)
    }

    fun subtract(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract money with different currencies" }
        return Money(amount.subtract(other.amount), currency)
    }

    fun multiply(multiplier: Int): Money {
        return Money(amount.multiply(BigDecimal.valueOf(multiplier.toLong())), currency)
    }

    fun multiply(multiplier: BigDecimal): Money {
        return Money(amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP), currency)
    }

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare money with different currencies" }
        return amount > other.amount
    }

    fun isLessThan(other: Money): Boolean {
        require(currency == other.currency) { "Cannot compare money with different currencies" }
        return amount < other.amount
    }

    fun toDouble(): Double = amount.toDouble()

    override fun toString(): String = "$currency ${amount.setScale(2, RoundingMode.HALF_UP)}"
}

enum class Currency {
    KRW, USD, JPY, CNY, EUR
}
