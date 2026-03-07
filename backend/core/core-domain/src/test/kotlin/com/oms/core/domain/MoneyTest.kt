package com.oms.core.domain

import com.oms.core.AbstractUnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

/**
 * Unit tests for Money value object.
 *
 * Tests verify:
 * - Valid money creation
 * - Arithmetic operations (add, subtract, multiply)
 * - Currency validation
 * - Comparison operations
 * - Boundary conditions
 */
class MoneyTest : AbstractUnitTest({

    describe("Money value object creation") {

        it("should create money with valid amount and currency") {
            val money = Money(BigDecimal("100.00"), Currency.KRW)

            money.amount shouldBe BigDecimal("100.00")
            money.currency shouldBe Currency.KRW
        }

        it("should create money using factory method") {
            val money = Money.of(100, Currency.USD)

            money.amount shouldBe BigDecimal("100.0")
            money.currency shouldBe Currency.USD
        }

        it("should create zero money") {
            val money = Money.zero(Currency.KRW)

            money.amount shouldBe BigDecimal.ZERO
            money.currency shouldBe Currency.KRW
        }

        it("should provide zero constants") {
            Money.ZERO_KRW.amount shouldBe BigDecimal.ZERO
            Money.ZERO_KRW.currency shouldBe Currency.KRW

            Money.ZERO_USD.amount shouldBe BigDecimal.ZERO
            Money.ZERO_USD.currency shouldBe Currency.USD
        }

        it("should reject negative amounts") {
            shouldThrow<IllegalArgumentException> {
                Money(BigDecimal("-10"), Currency.KRW)
            }
        }
    }

    describe("Money arithmetic operations") {

        it("should add two money values of same currency") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("50.00"), Currency.USD)

            val result = m1.add(m2)

            result.amount shouldBe BigDecimal("150.00")
            result.currency shouldBe Currency.USD
        }

        it("should throw when adding different currencies") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("100.00"), Currency.KRW)

            shouldThrow<IllegalArgumentException> {
                m1.add(m2)
            }
        }

        it("should subtract two money values of same currency") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("30.00"), Currency.USD)

            val result = m1.subtract(m2)

            result.amount shouldBe BigDecimal("70.00")
            result.currency shouldBe Currency.USD
        }

        it("should throw when subtracting different currencies") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("50.00"), Currency.KRW)

            shouldThrow<IllegalArgumentException> {
                m1.subtract(m2)
            }
        }

        it("should multiply by integer quantity") {
            val money = Money(BigDecimal("25.00"), Currency.USD)

            val result = money.multiply(4)

            result.amount shouldBe BigDecimal("100.00")
            result.currency shouldBe Currency.USD
        }

        it("should multiply by decimal multiplier") {
            val money = Money(BigDecimal("100.00"), Currency.USD)

            val result = money.multiply(BigDecimal("1.5"))

            result.amount shouldBe BigDecimal("150.00")
            result.currency shouldBe Currency.USD
        }

        it("should round to 2 decimal places when multiplying") {
            val money = Money(BigDecimal("10.00"), Currency.USD)

            val result = money.multiply(BigDecimal("0.333"))

            result.amount shouldBe BigDecimal("3.33")
        }
    }

    describe("Money comparison operations") {

        it("should compare greater than") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("50.00"), Currency.USD)

            m1.isGreaterThan(m2) shouldBe true
            m2.isGreaterThan(m1) shouldBe false
        }

        it("should compare less than") {
            val m1 = Money(BigDecimal("50.00"), Currency.USD)
            val m2 = Money(BigDecimal("100.00"), Currency.USD)

            m1.isLessThan(m2) shouldBe true
            m2.isLessThan(m1) shouldBe false
        }

        it("should throw when comparing different currencies") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("100.00"), Currency.KRW)

            shouldThrow<IllegalArgumentException> {
                m1.isGreaterThan(m2)
            }

            shouldThrow<IllegalArgumentException> {
                m1.isLessThan(m2)
            }
        }
    }

    describe("Money value equality") {

        it("should be equal when amount and currency match") {
            val m1 = Money(BigDecimal("100.00"), Currency.KRW)
            val m2 = Money(BigDecimal("100.00"), Currency.KRW)

            m1 shouldBe m2
        }

        it("should not be equal when amounts differ") {
            val m1 = Money(BigDecimal("100.00"), Currency.KRW)
            val m2 = Money(BigDecimal("200.00"), Currency.KRW)

            m1 shouldBe m1 // same instance
            (m1 == m2) shouldBe false
        }

        it("should not be equal when currencies differ") {
            val m1 = Money(BigDecimal("100.00"), Currency.KRW)
            val m2 = Money(BigDecimal("100.00"), Currency.USD)

            (m1 == m2) shouldBe false
        }
    }

    describe("Money conversion and formatting") {

        it("should convert to double") {
            val money = Money(BigDecimal("123.45"), Currency.USD)

            money.toDouble() shouldBe 123.45
        }

        it("should format to string with currency") {
            val money = Money(BigDecimal("100"), Currency.KRW)

            money.toString() shouldBe "KRW 100.00"
        }

        it("should format with proper rounding") {
            val money = Money(BigDecimal("99.999"), Currency.USD)

            money.toString() shouldBe "USD 100.00"
        }
    }

    describe("Money boundary conditions") {

        it("should handle zero amounts") {
            val zero = Money(BigDecimal.ZERO, Currency.KRW)

            zero.amount shouldBe BigDecimal.ZERO
            zero.isLessThan(Money.of(1, Currency.KRW)) shouldBe true
        }

        it("should handle very large amounts") {
            val large = Money(BigDecimal("999999999.99"), Currency.USD)

            large.amount shouldBe BigDecimal("999999999.99")
        }

        it("should handle all supported currencies") {
            Currency.values().forEach { currency ->
                val money = Money.of(100, currency)
                money.currency shouldBe currency
            }
        }
    }
})
