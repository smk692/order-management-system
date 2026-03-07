package com.oms.core

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearAllMocks
import io.mockk.unmockkAll

/**
 * Abstract base class for unit tests.
 *
 * This class provides:
 * - Kotest DescribeSpec setup for BDD-style tests
 * - Mockk lifecycle management (automatic cleanup)
 * - Common test utilities
 *
 * Usage:
 * ```kotlin
 * class MoneyTest : AbstractUnitTest({
 *     describe("Money value object") {
 *         it("should create money with valid amount") {
 *             val money = Money.of(100, Currency.KRW)
 *             money.amount shouldBe BigDecimal("100")
 *         }
 *     }
 * })
 * ```
 *
 * Best Practices:
 * - Pure unit tests: No Spring context, no database
 * - Fast execution: Tests should run in milliseconds
 * - Isolated: Each test is independent
 * - Use Mockk for mocking dependencies
 */
abstract class AbstractUnitTest(body: DescribeSpec.() -> Unit) : DescribeSpec(body) {
    init {
        // Clean up all mocks after each test to prevent test pollution
        afterEach {
            clearAllMocks()
        }

        // Clean up all mock definitions after spec completes
        afterSpec {
            unmockkAll()
        }
    }
}
