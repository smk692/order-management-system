package com.oms.api

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Smoke test to verify test infrastructure is working.
 *
 * This test verifies:
 * - Kotest framework is correctly configured
 * - JUnit 5 integration works
 * - Basic assertions work
 * - Test discovery is functional
 *
 * If this test passes, the test infrastructure is operational.
 */
class TestInfrastructureSmokeTest : DescribeSpec({

    describe("Test infrastructure smoke test") {

        it("should run basic assertions") {
            val result = 2 + 2

            result shouldBe 4
        }

        it("should support BDD-style test structure") {
            val value = "test"

            value shouldNotBe null
            value shouldBe "test"
        }

        it("should execute multiple test cases") {
            val list = listOf(1, 2, 3)

            list.size shouldBe 3
            list.first() shouldBe 1
            list.last() shouldBe 3
        }
    }

    describe("Kotest features") {

        it("should support nested describe blocks") {
            true shouldBe true
        }

        it("should support descriptive test names with spaces") {
            "Hello, World!".length shouldBe 13
        }
    }
})
