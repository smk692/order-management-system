package com.oms.core.domain

import com.oms.core.AbstractUnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * Unit tests for Address value object.
 *
 * Tests verify:
 * - Valid address creation
 * - Required field validation
 * - Address formatting
 * - Value object equality
 * - Factory methods
 */
class AddressTest : AbstractUnitTest({

    describe("Address value object creation") {

        it("should create valid address with all fields") {
            val address =
                Address(
                    recipient = "홍길동",
                    phone = "010-1234-5678",
                    zipCode = "06234",
                    address1 = "서울특별시 강남구 테헤란로 123",
                    address2 = "ABC빌딩 4층",
                    city = "서울",
                    state = "서울특별시",
                    country = "KR",
                )

            address.recipient shouldBe "홍길동"
            address.phone shouldBe "010-1234-5678"
            address.zipCode shouldBe "06234"
            address.address1 shouldBe "서울특별시 강남구 테헤란로 123"
            address.address2 shouldBe "ABC빌딩 4층"
            address.city shouldBe "서울"
            address.state shouldBe "서울특별시"
            address.country shouldBe "KR"
        }

        it("should create address with optional fields as null") {
            val address =
                Address(
                    recipient = "John Doe",
                    phone = "010-1234-5678",
                    address1 = "123 Main Street",
                    country = "US",
                )

            address.recipient shouldBe "John Doe"
            address.address1 shouldBe "123 Main Street"
            address.zipCode shouldBe null
            address.address2 shouldBe null
            address.city shouldBe null
            address.state shouldBe null
        }

        it("should default country to KR") {
            val address =
                Address(
                    recipient = "홍길동",
                    phone = "010-1234-5678",
                    address1 = "서울시 강남구",
                )

            address.country shouldBe "KR"
        }
    }

    describe("Address validation") {

        it("should reject blank recipient") {
            shouldThrow<IllegalArgumentException> {
                Address(
                    recipient = "   ",
                    phone = "010-1234-5678",
                    address1 = "123 Main St",
                )
            }
        }

        it("should reject blank phone") {
            shouldThrow<IllegalArgumentException> {
                Address(
                    recipient = "John Doe",
                    phone = "",
                    address1 = "123 Main St",
                )
            }
        }

        it("should reject blank address1") {
            shouldThrow<IllegalArgumentException> {
                Address(
                    recipient = "John Doe",
                    phone = "010-1234-5678",
                    address1 = "   ",
                )
            }
        }

        it("should reject blank country") {
            shouldThrow<IllegalArgumentException> {
                Address(
                    recipient = "John Doe",
                    phone = "010-1234-5678",
                    address1 = "123 Main St",
                    country = "",
                )
            }
        }
    }

    describe("Address formatting") {

        it("should format full address with all fields") {
            val address =
                Address(
                    recipient = "홍길동",
                    phone = "010-1234-5678",
                    zipCode = "06234",
                    address1 = "테헤란로 123",
                    address2 = "ABC빌딩",
                    city = "강남구",
                    state = "서울특별시",
                    country = "KR",
                )

            val fullAddress = address.fullAddress()

            fullAddress shouldContain "테헤란로 123"
            fullAddress shouldContain "ABC빌딩"
            fullAddress shouldContain "강남구"
            fullAddress shouldContain "서울특별시"
            fullAddress shouldContain "06234"
            fullAddress shouldContain "KR"
        }

        it("should format address with only required fields") {
            val address =
                Address(
                    recipient = "John Doe",
                    phone = "010-1234-5678",
                    address1 = "123 Main Street",
                    country = "US",
                )

            val fullAddress = address.fullAddress()

            fullAddress shouldContain "123 Main Street"
            fullAddress shouldContain "US"
        }

        it("should omit null fields from full address") {
            val address =
                Address(
                    recipient = "Test User",
                    phone = "010-0000-0000",
                    address1 = "Street Address",
                    address2 = null,
                    city = null,
                    zipCode = "12345",
                    country = "KR",
                )

            val fullAddress = address.fullAddress()

            fullAddress shouldContain "Street Address"
            fullAddress shouldContain "12345"
            fullAddress shouldContain "KR"
        }
    }

    describe("Address value object equality") {

        it("should be equal when all fields match") {
            val address1 =
                Address(
                    recipient = "John Doe",
                    phone = "010-1111-1111",
                    zipCode = "12345",
                    address1 = "123 Street",
                    address2 = null,
                    city = "City",
                    state = "State",
                    country = "KR",
                )

            val address2 =
                Address(
                    recipient = "John Doe",
                    phone = "010-1111-1111",
                    zipCode = "12345",
                    address1 = "123 Street",
                    address2 = null,
                    city = "City",
                    state = "State",
                    country = "KR",
                )

            address1 shouldBe address2
        }

        it("should not be equal when recipient differs") {
            val address1 =
                Address(
                    recipient = "John Doe",
                    phone = "010-1111-1111",
                    address1 = "123 Street",
                    country = "KR",
                )

            val address2 =
                Address(
                    recipient = "Jane Doe",
                    phone = "010-1111-1111",
                    address1 = "123 Street",
                    country = "KR",
                )

            address1 shouldNotBe address2
        }

        it("should not be equal when address differs") {
            val address1 =
                Address(
                    recipient = "John Doe",
                    phone = "010-1111-1111",
                    address1 = "123 Street",
                    country = "KR",
                )

            val address2 =
                Address(
                    recipient = "John Doe",
                    phone = "010-1111-1111",
                    address1 = "456 Avenue",
                    country = "KR",
                )

            address1 shouldNotBe address2
        }
    }

    describe("Address factory methods") {

        it("should create Korean address using factory method") {
            val address =
                Address.korean(
                    recipient = "김철수",
                    phone = "010-9876-5432",
                    zipCode = "06234",
                    address1 = "강남대로 123",
                    address2 = "오피스텔 101호",
                )

            address.recipient shouldBe "김철수"
            address.country shouldBe "KR"
            address.city shouldBe "서울"
        }

        it("should create Korean address with custom city") {
            val address =
                Address.korean(
                    recipient = "이영희",
                    phone = "010-5555-6666",
                    zipCode = "48058",
                    address1 = "해운대로 123",
                    city = "부산",
                )

            address.city shouldBe "부산"
            address.country shouldBe "KR"
        }

        it("should create Korean address with minimal fields") {
            val address =
                Address.korean(
                    recipient = "박민수",
                    phone = "010-1234-5678",
                    zipCode = "12345",
                    address1 = "테스트로 1",
                )

            address.recipient shouldBe "박민수"
            address.country shouldBe "KR"
            address.address2 shouldBe null
        }
    }
})
