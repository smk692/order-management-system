package com.oms.order.domain

import com.oms.core.AbstractUnitTest
import com.oms.core.domain.Address
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal

/**
 * Unit tests for Order aggregate root.
 *
 * Tests verify:
 * - Order creation and lifecycle
 * - Status transitions (state machine)
 * - Order item management
 * - Total amount calculation
 * - Domain event publishing
 * - Business rules enforcement
 */
class OrderTest : AbstractUnitTest({

    // Test fixture helpers
    fun createTestAddress() = Address(
        recipient = "홍길동",
        phone = "010-1234-5678",
        zipCode = "06234",
        address1 = "서울특별시 강남구 테헤란로 123",
        city = "서울",
        country = "KR"
    )

    fun createTestCustomer() = Customer(
        name = "김테스트",
        phone = "010-9999-8888",
        email = "test@example.com"
    )

    fun createTestOrder() = Order.create(
        companyId = "company-test",
        channelId = "channel-test",
        customer = createTestCustomer(),
        shippingAddress = createTestAddress(),
        fulfillmentMethod = FulfillmentMethod.WMS
    )

    describe("Order creation") {

        it("should create order in NEW status") {
            val order = createTestOrder()

            order.status shouldBe OrderStatus.NEW
            order.id shouldNotBe null
            order.items shouldHaveSize 0
            order.totalAmount shouldBe Money.zero()
        }

        it("should generate unique order ID with date prefix") {
            val order1 = createTestOrder()
            val order2 = createTestOrder()

            order1.id shouldNotBe order2.id
            order1.id.startsWith("ORD-") shouldBe true
            order2.id.startsWith("ORD-") shouldBe true
        }

        it("should assign to company") {
            val companyId = "company-123"
            val order = Order.create(
                companyId = companyId,
                channelId = "channel-test",
                customer = createTestCustomer(),
                shippingAddress = createTestAddress(),
                fulfillmentMethod = FulfillmentMethod.WMS
            )

            order.companyId shouldBe companyId
        }

        it("should publish OrderCreatedEvent") {
            val order = createTestOrder()

            val events = order.getDomainEvents()
            events shouldHaveSize 1
            events[0].javaClass.simpleName shouldBe "OrderCreatedEvent"
        }
    }

    describe("Order item management") {

        it("should add item to order") {
            val order = createTestOrder()

            val item = order.addItem(
                productId = "product-1",
                productName = "테스트 상품",
                sku = "SKU-001",
                quantity = 2,
                unitPrice = Money.of(10000, Currency.KRW)
            )

            order.items shouldHaveSize 1
            item.productName shouldBe "테스트 상품"
            item.quantity shouldBe 2
        }

        it("should recalculate total when adding items") {
            val order = createTestOrder()

            order.addItem("p1", "Product 1", "SKU1", 2, Money.of(10000, Currency.KRW))
            order.addItem("p2", "Product 2", "SKU2", 1, Money.of(5000, Currency.KRW))

            order.totalAmount.amount shouldBe BigDecimal("25000")
        }

        it("should reject adding items with negative quantity") {
            val order = createTestOrder()

            shouldThrow<IllegalArgumentException> {
                order.addItem("p1", "Product", "SKU1", -1, Money.of(10000, Currency.KRW))
            }
        }
    }

    describe("Order status state machine") {

        it("should transition from NEW to PAYMENT_PENDING") {
            val order = createTestOrder()

            order.canTransitionTo(OrderStatus.PAYMENT_PENDING) shouldBe true
            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            order.status shouldBe OrderStatus.PAYMENT_PENDING
        }

        it("should transition from PAYMENT_PENDING to PAID") {
            val order = createTestOrder()
            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            order.markAsPaid()

            order.status shouldBe OrderStatus.PAID
        }

        it("should not allow invalid transitions") {
            val order = createTestOrder()

            order.canTransitionTo(OrderStatus.SHIPPED) shouldBe false

            shouldThrow<IllegalArgumentException> {
                order.transitionTo(OrderStatus.SHIPPED)
            }
        }
    }

    describe("Order cancellation") {

        it("should cancel order in NEW status") {
            val order = createTestOrder()

            order.cancel("Customer requested cancellation")

            order.status shouldBe OrderStatus.CANCELLED
            order.isCancelled() shouldBe true
        }

        it("should publish OrderCancelledEvent") {
            val order = createTestOrder()
            order.clearEvents()

            order.cancel("Test cancellation")

            val events = order.getDomainEvents()
            events shouldHaveSize 1
            events[0].javaClass.simpleName shouldBe "OrderCancelledEvent"
        }
    }
})