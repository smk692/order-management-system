package com.oms.claim.domain

import com.oms.claim.domain.vo.ClaimPriority
import com.oms.claim.domain.vo.ClaimStatus
import com.oms.claim.domain.vo.ClaimType
import com.oms.core.AbstractUnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.LocalDateTime

class ClaimTest : AbstractUnitTest({

    describe("Claim entity creation") {
        context("when creating with valid data") {
            it("should create claim successfully") {
                val claim = Claim(
                    id = "CLM-123",
                    claimNumber = "CLM-20260307-001",
                    orderId = "ORD-123",
                    type = ClaimType.RETURN,
                    reason = "Product defect"
                )

                claim.id shouldBe "CLM-123"
                claim.claimNumber shouldBe "CLM-20260307-001"
                claim.orderId shouldBe "ORD-123"
                claim.type shouldBe ClaimType.RETURN
                claim.status shouldBe ClaimStatus.PENDING
                claim.reason shouldBe "Product defect"
                claim.priority shouldBe ClaimPriority.NORMAL
                claim.refundAmount shouldBe BigDecimal.ZERO
            }

            it("should create claim with custom priority") {
                val claim = Claim(
                    id = "CLM-123",
                    claimNumber = "CLM-20260307-001",
                    orderId = "ORD-123",
                    type = ClaimType.URGENT,
                    reason = "Critical issue",
                    priority = ClaimPriority.URGENT
                )

                claim.priority shouldBe ClaimPriority.URGENT
            }

            it("should create claim with memo") {
                val claim = Claim(
                    id = "CLM-123",
                    claimNumber = "CLM-20260307-001",
                    orderId = "ORD-123",
                    type = ClaimType.EXCHANGE,
                    reason = "Size issue",
                    memo = "Customer requested size L instead of M"
                )

                claim.memo shouldBe "Customer requested size L instead of M"
            }
        }

        context("when creating with invalid data") {
            it("should reject invalid claim number format") {
                shouldThrow<IllegalArgumentException> {
                    Claim(
                        id = "CLM-123",
                        claimNumber = "INVALID-FORMAT",
                        orderId = "ORD-123",
                        type = ClaimType.RETURN,
                        reason = "Product defect"
                    )
                }.message shouldBe "Claim number must be in format CLM-yyyyMMdd-nnn"
            }

            it("should reject blank reason") {
                shouldThrow<IllegalArgumentException> {
                    Claim(
                        id = "CLM-123",
                        claimNumber = "CLM-20260307-001",
                        orderId = "ORD-123",
                        type = ClaimType.RETURN,
                        reason = "   "
                    )
                }.message shouldBe "Reason cannot be blank"
            }

            it("should reject empty reason") {
                shouldThrow<IllegalArgumentException> {
                    Claim(
                        id = "CLM-123",
                        claimNumber = "CLM-20260307-001",
                        orderId = "ORD-123",
                        type = ClaimType.RETURN,
                        reason = ""
                    )
                }.message shouldBe "Reason cannot be blank"
            }
        }
    }

    describe("Claim item management") {
        context("when adding items to claim") {
            it("should add item successfully") {
                val claim = createValidClaim()
                val item = ClaimItem(
                    claimId = claim.id,
                    productId = "PROD-001",
                    productName = "Test Product",
                    quantity = 2,
                    unitPrice = BigDecimal("100.00")
                )

                claim.addItem(item)

                claim.items.size shouldBe 1
                claim.items[0].productId shouldBe "PROD-001"
            }

            it("should add multiple items") {
                val claim = createValidClaim()

                claim.addItem(createClaimItem("PROD-001", "Product 1", 2, "100.00"))
                claim.addItem(createClaimItem("PROD-002", "Product 2", 1, "50.00"))

                claim.items.size shouldBe 2
            }

            it("should calculate total items amount correctly") {
                val claim = createValidClaim()
                claim.addItem(createClaimItem("PROD-001", "Product 1", 2, "100.00"))
                claim.addItem(createClaimItem("PROD-002", "Product 2", 3, "50.00"))

                // 2 * 100 + 3 * 50 = 350
                claim.getTotalItemsAmount() shouldBe BigDecimal("350.00")
            }

            it("should return zero for claim with no items") {
                val claim = createValidClaim()

                claim.getTotalItemsAmount() shouldBe BigDecimal.ZERO
            }
        }
    }

    describe("Claim workflow - startProcessing") {
        context("when starting processing on PENDING claim") {
            it("should change status to PROCESSING") {
                val claim = createValidClaim()

                claim.startProcessing()

                claim.status shouldBe ClaimStatus.PROCESSING
                claim.processedAt shouldNotBe null
                claim.processedAt shouldBeGreaterThan LocalDateTime.now().minusSeconds(1)
            }
        }

        context("when starting processing on non-PENDING claim") {
            it("should reject PROCESSING claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.PROCESSING

                shouldThrow<IllegalArgumentException> {
                    claim.startProcessing()
                }.message shouldBe "Can only start processing a PENDING claim"
            }

            it("should reject COMPLETED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.COMPLETED

                shouldThrow<IllegalArgumentException> {
                    claim.startProcessing()
                }.message shouldBe "Can only start processing a PENDING claim"
            }

            it("should reject REJECTED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.REJECTED

                shouldThrow<IllegalArgumentException> {
                    claim.startProcessing()
                }.message shouldBe "Can only start processing a PENDING claim"
            }
        }
    }

    describe("Claim workflow - complete") {
        context("when completing PROCESSING claim") {
            it("should complete with valid refund amount") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.PROCESSING

                claim.complete(BigDecimal("100.00"))

                claim.status shouldBe ClaimStatus.COMPLETED
                claim.refundAmount shouldBe BigDecimal("100.00")
                claim.refundedAt shouldNotBe null
                claim.refundedAt shouldBeGreaterThan LocalDateTime.now().minusSeconds(1)
            }

            it("should complete with zero refund amount") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.PROCESSING

                claim.complete(BigDecimal.ZERO)

                claim.status shouldBe ClaimStatus.COMPLETED
                claim.refundAmount shouldBe BigDecimal.ZERO
            }
        }

        context("when completing with invalid state or amount") {
            it("should reject negative refund amount") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.PROCESSING

                shouldThrow<IllegalArgumentException> {
                    claim.complete(BigDecimal("-10.00"))
                }.message shouldBe "Refund amount must be non-negative"
            }

            it("should reject completing PENDING claim") {
                val claim = createValidClaim()

                shouldThrow<IllegalArgumentException> {
                    claim.complete(BigDecimal("100.00"))
                }.message shouldBe "Can only complete a PROCESSING claim"
            }

            it("should reject completing COMPLETED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.COMPLETED

                shouldThrow<IllegalArgumentException> {
                    claim.complete(BigDecimal("100.00"))
                }.message shouldBe "Can only complete a PROCESSING claim"
            }

            it("should reject completing REJECTED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.REJECTED

                shouldThrow<IllegalArgumentException> {
                    claim.complete(BigDecimal("100.00"))
                }.message shouldBe "Can only complete a PROCESSING claim"
            }
        }
    }

    describe("Claim workflow - reject") {
        context("when rejecting PENDING claim") {
            it("should reject with valid reason") {
                val claim = createValidClaim()

                claim.reject("Customer fraud detected")

                claim.status shouldBe ClaimStatus.REJECTED
                claim.memo shouldBe "Customer fraud detected"
            }
        }

        context("when rejecting PROCESSING claim") {
            it("should reject with valid reason") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.PROCESSING

                claim.reject("Cannot verify product condition")

                claim.status shouldBe ClaimStatus.REJECTED
                claim.memo shouldBe "Cannot verify product condition"
            }
        }

        context("when rejecting with invalid state or reason") {
            it("should reject blank rejection reason") {
                val claim = createValidClaim()

                shouldThrow<IllegalArgumentException> {
                    claim.reject("   ")
                }.message shouldBe "Rejection reason cannot be blank"
            }

            it("should reject empty rejection reason") {
                val claim = createValidClaim()

                shouldThrow<IllegalArgumentException> {
                    claim.reject("")
                }.message shouldBe "Rejection reason cannot be blank"
            }

            it("should reject rejecting COMPLETED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.COMPLETED

                shouldThrow<IllegalArgumentException> {
                    claim.reject("Invalid reason")
                }.message shouldBe "Can only reject PENDING or PROCESSING claims"
            }

            it("should reject rejecting already REJECTED claim") {
                val claim = createValidClaim()
                claim.status = ClaimStatus.REJECTED

                shouldThrow<IllegalArgumentException> {
                    claim.reject("Invalid reason")
                }.message shouldBe "Can only reject PENDING or PROCESSING claims"
            }
        }
    }

    describe("ClaimType enum") {
        it("should have all expected types") {
            ClaimType.values().map { it.name } shouldBe listOf("CANCEL", "RETURN", "EXCHANGE")
        }
    }

    describe("ClaimStatus enum") {
        it("should have all expected statuses") {
            ClaimStatus.values().map { it.name } shouldBe listOf("PENDING", "PROCESSING", "COMPLETED", "REJECTED")
        }
    }

    describe("ClaimPriority enum") {
        it("should have all expected priorities") {
            ClaimPriority.values().map { it.name } shouldBe listOf("URGENT", "NORMAL")
        }
    }
})

private fun createValidClaim(
    id: String = "CLM-123",
    claimNumber: String = "CLM-20260307-001",
    orderId: String = "ORD-123"
) = Claim(
    id = id,
    claimNumber = claimNumber,
    orderId = orderId,
    type = ClaimType.RETURN,
    reason = "Product defect"
)

private fun createClaimItem(
    productId: String,
    productName: String,
    quantity: Int,
    unitPrice: String
) = ClaimItem(
    claimId = "CLM-123",
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = BigDecimal(unitPrice)
)
