package com.oms.order

import com.oms.core.domain.Address
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import com.oms.order.domain.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

class OrderTest : DescribeSpec({

    val testCompanyId = "company-123"
    val testChannelId = "channel-456"
    val testCustomer = Customer(
        name = "홍길동",
        phone = "010-1234-5678",
        email = "hong@example.com"
    )
    val testAddress = Address(
        recipient = "홍길동",
        phone = "010-1234-5678",
        address1 = "서울시 강남구 테헤란로 123",
        country = "KR"
    )

    describe("Order creation") {

        it("should create order with NEW status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress,
                fulfillmentMethod = FulfillmentMethod.WMS
            )

            order.status shouldBe OrderStatus.NEW
            order.channelId shouldBe testChannelId
            order.customer shouldBe testCustomer
            order.shippingAddress shouldBe testAddress
            order.fulfillmentMethod shouldBe FulfillmentMethod.WMS
            order.totalAmount shouldBe Money.zero()
            order.items shouldBe emptyList()
        }

        it("should generate order ID with date prefix") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.id shouldStartWith "ORD-"
        }

        it("should create order with external order ID") {
            val externalId = "EXT-ORDER-123"
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress,
                externalOrderId = externalId
            )

            order.externalOrderId shouldBe externalId
        }

        it("should create order with default WMS fulfillment method") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.fulfillmentMethod shouldBe FulfillmentMethod.WMS
        }

        it("should create order with DIRECT fulfillment method") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress,
                fulfillmentMethod = FulfillmentMethod.DIRECT
            )

            order.fulfillmentMethod shouldBe FulfillmentMethod.DIRECT
        }

        it("should emit OrderCreatedEvent on creation") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            val events = order.getDomainEvents()
            events.size shouldBe 1
        }
    }

    describe("Adding items") {

        it("should add item to order") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            val item = order.addItem(
                productId = "product-123",
                productName = "테스트 상품",
                sku = "SKU-001",
                quantity = 2,
                unitPrice = Money.of(10000, Currency.KRW)
            )

            order.items.size shouldBe 1
            order.items[0] shouldBe item
            item.productName shouldBe "테스트 상품"
            item.quantity shouldBe 2
            item.unitPrice shouldBe Money.of(10000, Currency.KRW)
        }

        it("should recalculate total when adding items") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.addItem(
                productId = "product-1",
                productName = "상품1",
                sku = "SKU-001",
                quantity = 2,
                unitPrice = Money.of(10000, Currency.KRW)
            )

            order.totalAmount shouldBe Money.of(20000, Currency.KRW)

            order.addItem(
                productId = "product-2",
                productName = "상품2",
                sku = "SKU-002",
                quantity = 3,
                unitPrice = Money.of(5000, Currency.KRW)
            )

            order.totalAmount shouldBe Money.of(35000, Currency.KRW)
        }

        it("should throw exception when adding item with zero quantity") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            shouldThrow<IllegalArgumentException> {
                order.addItem(
                    productId = "product-1",
                    productName = "상품1",
                    sku = "SKU-001",
                    quantity = 0,
                    unitPrice = Money.of(10000, Currency.KRW)
                )
            }.message shouldBe "Quantity must be positive"
        }

        it("should throw exception when adding item with negative quantity") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            shouldThrow<IllegalArgumentException> {
                order.addItem(
                    productId = "product-1",
                    productName = "상품1",
                    sku = "SKU-001",
                    quantity = -1,
                    unitPrice = Money.of(10000, Currency.KRW)
                )
            }.message shouldBe "Quantity must be positive"
        }

        it("should allow adding items in PAYMENT_PENDING status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            order.addItem(
                productId = "product-1",
                productName = "상품1",
                sku = "SKU-001",
                quantity = 1,
                unitPrice = Money.of(10000, Currency.KRW)
            )

            order.items.size shouldBe 1
        }

        it("should not allow adding items in PAID status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.transitionTo(OrderStatus.PAID)

            shouldThrow<IllegalArgumentException> {
                order.addItem(
                    productId = "product-1",
                    productName = "상품1",
                    sku = "SKU-001",
                    quantity = 1,
                    unitPrice = Money.of(10000, Currency.KRW)
                )
            }.message shouldBe "Cannot add items to order in status: PAID"
        }
    }

    describe("Status transitions") {

        it("should transition from NEW to PAYMENT_PENDING") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.canTransitionTo(OrderStatus.PAYMENT_PENDING) shouldBe true
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.status shouldBe OrderStatus.PAYMENT_PENDING
        }

        it("should transition from PAYMENT_PENDING to PAID") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            order.canTransitionTo(OrderStatus.PAID) shouldBe true
            order.markAsPaid()
            order.status shouldBe OrderStatus.PAID
        }

        it("should transition from PAID to PREPARING") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()

            order.canTransitionTo(OrderStatus.PREPARING) shouldBe true
            order.startPreparing()
            order.status shouldBe OrderStatus.PREPARING
        }

        it("should transition from PREPARING to READY_TO_SHIP") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()

            order.canTransitionTo(OrderStatus.READY_TO_SHIP) shouldBe true
            order.markReadyToShip()
            order.status shouldBe OrderStatus.READY_TO_SHIP
        }

        it("should transition from READY_TO_SHIP to SHIPPED") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()

            order.ship(Carrier.CJ, "TRACK-123456")
            order.status shouldBe OrderStatus.SHIPPED
            order.shipping shouldNotBe null
            order.shipping?.trackingNumber shouldBe "TRACK-123456"
            order.shipping?.carrier shouldBe Carrier.CJ
        }

        it("should transition from SHIPPED to IN_DELIVERY") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")

            order.canTransitionTo(OrderStatus.IN_DELIVERY) shouldBe true
            order.markInDelivery()
            order.status shouldBe OrderStatus.IN_DELIVERY
        }

        it("should transition from IN_DELIVERY to DELIVERED") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")
            order.markInDelivery()

            order.canTransitionTo(OrderStatus.DELIVERED) shouldBe true
            order.markDelivered()
            order.status shouldBe OrderStatus.DELIVERED
        }

        it("should throw exception for invalid transition") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            shouldThrow<IllegalArgumentException> {
                order.transitionTo(OrderStatus.DELIVERED)
            }.message shouldBe "Cannot transition from NEW to DELIVERED"
        }

        it("should emit OrderStatusChangedEvent on transition") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.clearEvents()

            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            val events = order.getDomainEvents()
            events.size shouldBe 1
        }
    }

    describe("Cancellation") {

        it("should cancel order from NEW status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.cancel("고객 요청")
            order.status shouldBe OrderStatus.CANCELLED
            order.isCancelled() shouldBe true
        }

        it("should cancel order from PAYMENT_PENDING status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)

            order.cancel("결제 실패")
            order.status shouldBe OrderStatus.CANCELLED
        }

        it("should cancel order from PAID status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()

            order.cancel("재고 부족")
            order.status shouldBe OrderStatus.CANCELLED
        }

        it("should not allow cancelling from DELIVERED status") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")
            order.markInDelivery()
            order.markDelivered()

            shouldThrow<IllegalArgumentException> {
                order.cancel("테스트")
            }.message shouldBe "Cannot cancel order in status: DELIVERED"
        }

        it("should emit OrderCancelledEvent on cancellation") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.clearEvents()

            order.cancel("고객 요청")

            val events = order.getDomainEvents()
            events.size shouldBe 1
        }
    }

    describe("Warehouse assignment") {

        it("should assign order to warehouse for WMS fulfillment") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress,
                fulfillmentMethod = FulfillmentMethod.WMS
            )

            order.assignToWarehouse("warehouse-123", "Closest to customer")
            order.assignedWarehouseId shouldBe "warehouse-123"
            order.routingLogic shouldBe "Closest to customer"
        }

        it("should throw exception when assigning warehouse for DIRECT fulfillment") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress,
                fulfillmentMethod = FulfillmentMethod.DIRECT
            )

            shouldThrow<IllegalArgumentException> {
                order.assignToWarehouse("warehouse-123", "Logic")
            }.message shouldBe "Can only assign warehouse for WMS fulfillment"
        }
    }

    describe("Order state queries") {

        it("should identify completed orders") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")
            order.markInDelivery()
            order.markDelivered()

            order.isCompleted() shouldBe true
        }

        it("should identify cancelled orders") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.cancel("테스트")

            order.isCancelled() shouldBe true
        }

        it("should identify modifiable orders") {
            val newOrder = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            newOrder.canBeModified() shouldBe true

            val pendingOrder = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            pendingOrder.transitionTo(OrderStatus.PAYMENT_PENDING)
            pendingOrder.canBeModified() shouldBe true

            val paidOrder = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            paidOrder.transitionTo(OrderStatus.PAYMENT_PENDING)
            paidOrder.markAsPaid()
            paidOrder.canBeModified() shouldBe false
        }
    }

    describe("Exchange and return") {

        it("should request exchange after delivery") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")
            order.markInDelivery()
            order.markDelivered()

            order.requestExchange()
            order.status shouldBe OrderStatus.EXCHANGE_REQUESTED
        }

        it("should request return after delivery") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )
            order.transitionTo(OrderStatus.PAYMENT_PENDING)
            order.markAsPaid()
            order.startPreparing()
            order.markReadyToShip()
            order.ship(Carrier.CJ, "TRACK-123456")
            order.markInDelivery()
            order.markDelivered()

            order.requestReturn()
            order.status shouldBe OrderStatus.RETURN_REQUESTED
        }
    }

    describe("Domain events") {

        it("should track domain events") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            order.getDomainEvents().size shouldBe 1
        }

        it("should clear domain events") {
            val order = Order.create(
                companyId = testCompanyId,
                channelId = testChannelId,
                customer = testCustomer,
                shippingAddress = testAddress
            )

            val events = order.clearEvents()
            events.size shouldBe 1
            order.getDomainEvents().size shouldBe 0
        }
    }
})
