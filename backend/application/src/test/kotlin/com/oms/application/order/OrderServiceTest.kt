package com.oms.application.order

import com.oms.catalog.repository.ProductRepository
import com.oms.core.AbstractUnitTest
import com.oms.core.domain.Address
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import com.oms.core.exception.BusinessRuleException
import com.oms.core.exception.EntityNotFoundException
import com.oms.order.domain.*
import com.oms.order.repository.OrderRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import java.time.Instant
import java.util.UUID

class OrderServiceTest : AbstractUnitTest({

    lateinit var orderRepository: OrderRepository
    lateinit var productRepository: ProductRepository
    lateinit var orderService: OrderService

    beforeEach {
        orderRepository = mockk()
        productRepository = mockk()
        orderService = OrderService(orderRepository, productRepository)
    }

    describe("createOrder") {
        context("when creating a valid order") {
            it("should create order with items and return result") {
                // Given
                val command = createOrderCommand()
                val expectedOrder = createMockOrder()

                every { productRepository.findById(any()) } returns null
                every { orderRepository.save(any()) } returns expectedOrder

                // When
                val result = orderService.createOrder(command)

                // Then
                result.id.shouldNotBeNull()
                result.companyId shouldBe command.companyId
                result.channelId shouldBe command.channelId
                result.customer.name shouldBe command.customerName
                result.customer.phone shouldBe command.customerPhone
                result.items shouldHaveSize 2
                result.status shouldBe OrderStatus.PENDING_PAYMENT

                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 2) { productRepository.findById(any()) }
            }
        }

        context("when product exists in repository") {
            it("should use product details from repository") {
                // Given
                val command = createOrderCommand()
                val mockProduct = mockk<com.oms.catalog.domain.Product>()
                every { mockProduct.name.ko } returns "제품명"
                every { mockProduct.sku } returns "SKU-123"

                val expectedOrder = createMockOrder()

                every { productRepository.findById(any()) } returns mockProduct
                every { orderRepository.save(any()) } returns expectedOrder

                // When
                val result = orderService.createOrder(command)

                // Then
                result.shouldNotBeNull()
                verify(exactly = 1) { orderRepository.save(any()) }
            }
        }
    }

    describe("getOrder") {
        context("when order exists") {
            it("should return order result") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = createMockOrder()
                every { orderRepository.findById(orderId) } returns order

                // When
                val result = orderService.getOrder(orderId)

                // Then
                result.id shouldBe order.id
                result.companyId shouldBe order.companyId
                verify(exactly = 1) { orderRepository.findById(orderId) }
            }
        }

        context("when order does not exist") {
            it("should throw EntityNotFoundException") {
                // Given
                val orderId = UUID.randomUUID().toString()
                every { orderRepository.findById(orderId) } returns null

                // When & Then
                shouldThrow<EntityNotFoundException> {
                    orderService.getOrder(orderId)
                }
            }
        }
    }

    describe("getOrdersByCompany") {
        context("when company has orders") {
            it("should return list of orders") {
                // Given
                val companyId = UUID.randomUUID().toString()
                val orders = listOf(createMockOrder(), createMockOrder())
                every { orderRepository.findByCompanyId(companyId) } returns orders

                // When
                val result = orderService.getOrdersByCompany(companyId)

                // Then
                result shouldHaveSize 2
                verify(exactly = 1) { orderRepository.findByCompanyId(companyId) }
            }
        }

        context("when company has no orders") {
            it("should return empty list") {
                // Given
                val companyId = UUID.randomUUID().toString()
                every { orderRepository.findByCompanyId(companyId) } returns emptyList()

                // When
                val result = orderService.getOrdersByCompany(companyId)

                // Then
                result shouldHaveSize 0
            }
        }
    }

    describe("getOrdersByStatus") {
        it("should return orders filtered by status") {
            // Given
            val companyId = UUID.randomUUID().toString()
            val status = OrderStatus.PENDING_PAYMENT
            val orders = listOf(createMockOrder())
            every { orderRepository.findByCompanyIdAndStatus(companyId, status) } returns orders

            // When
            val result = orderService.getOrdersByStatus(companyId, status)

            // Then
            result shouldHaveSize 1
            verify(exactly = 1) { orderRepository.findByCompanyIdAndStatus(companyId, status) }
        }
    }

    describe("getOrdersByDateRange") {
        it("should return orders within date range") {
            // Given
            val companyId = UUID.randomUUID().toString()
            val startDate = Instant.now().minusSeconds(86400)
            val endDate = Instant.now()
            val orders = listOf(createMockOrder())
            every {
                orderRepository.findByCompanyIdAndDateRange(companyId, startDate, endDate)
            } returns orders

            // When
            val result = orderService.getOrdersByDateRange(companyId, startDate, endDate)

            // Then
            result shouldHaveSize 1
        }
    }

    describe("markOrderPaid") {
        context("when order exists") {
            it("should mark order as paid and return updated order") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = spyk(createMockOrder())
                every { orderRepository.findById(orderId) } returns order
                every { orderRepository.save(order) } returns order

                // When
                val result = orderService.markOrderPaid(orderId)

                // Then
                result.shouldNotBeNull()
                verify(exactly = 1) { order.markAsPaid() }
                verify(exactly = 1) { orderRepository.save(order) }
            }
        }

        context("when order does not exist") {
            it("should throw EntityNotFoundException") {
                // Given
                val orderId = UUID.randomUUID().toString()
                every { orderRepository.findById(orderId) } returns null

                // When & Then
                shouldThrow<EntityNotFoundException> {
                    orderService.markOrderPaid(orderId)
                }
            }
        }
    }

    describe("startPreparingOrder") {
        it("should start preparing order") {
            // Given
            val orderId = UUID.randomUUID().toString()
            val order = spyk(createMockOrder())
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(order) } returns order

            // When
            val result = orderService.startPreparingOrder(orderId)

            // Then
            result.shouldNotBeNull()
            verify(exactly = 1) { order.startPreparing() }
        }
    }

    describe("markOrderReadyToShip") {
        it("should mark order as ready to ship") {
            // Given
            val orderId = UUID.randomUUID().toString()
            val order = spyk(createMockOrder())
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(order) } returns order

            // When
            val result = orderService.markOrderReadyToShip(orderId)

            // Then
            result.shouldNotBeNull()
            verify(exactly = 1) { order.markReadyToShip() }
        }
    }

    describe("shipOrder") {
        context("when shipping an order") {
            it("should ship order with tracking information") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = spyk(createMockOrder())
                val command =
                    ShipOrderCommand(
                        carrier = Carrier.CJLOGISTICS,
                        trackingNumber = "123456789",
                    )
                every { orderRepository.findById(orderId) } returns order
                every { orderRepository.save(order) } returns order

                // When
                val result = orderService.shipOrder(orderId, command)

                // Then
                result.shouldNotBeNull()
                verify(exactly = 1) {
                    order.ship(command.carrier, command.trackingNumber)
                }
            }
        }
    }

    describe("markOrderInDelivery") {
        it("should mark order as in delivery") {
            // Given
            val orderId = UUID.randomUUID().toString()
            val order = spyk(createMockOrder())
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(order) } returns order

            // When
            val result = orderService.markOrderInDelivery(orderId)

            // Then
            result.shouldNotBeNull()
            verify(exactly = 1) { order.markInDelivery() }
        }
    }

    describe("markOrderDelivered") {
        it("should mark order as delivered") {
            // Given
            val orderId = UUID.randomUUID().toString()
            val order = spyk(createMockOrder())
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(order) } returns order

            // When
            val result = orderService.markOrderDelivered(orderId)

            // Then
            result.shouldNotBeNull()
            verify(exactly = 1) { order.markDelivered() }
        }
    }

    describe("cancelOrder") {
        context("when canceling an order") {
            it("should cancel order with reason") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = spyk(createMockOrder())
                val reason = "Customer requested cancellation"
                every { orderRepository.findById(orderId) } returns order
                every { orderRepository.save(order) } returns order

                // When
                val result = orderService.cancelOrder(orderId, reason)

                // Then
                result.shouldNotBeNull()
                verify(exactly = 1) { order.cancel(reason) }
            }
        }
    }

    describe("assignToWarehouse") {
        it("should assign order to warehouse") {
            // Given
            val orderId = UUID.randomUUID().toString()
            val warehouseId = UUID.randomUUID().toString()
            val routingLogic = "nearest-warehouse"
            val order = spyk(createMockOrder())
            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(order) } returns order

            // When
            val result = orderService.assignToWarehouse(orderId, warehouseId, routingLogic)

            // Then
            result.shouldNotBeNull()
            verify(exactly = 1) { order.assignToWarehouse(warehouseId, routingLogic) }
        }
    }

    describe("addTrackingEvent") {
        context("when order has shipping information") {
            it("should add tracking event") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = spyk(createMockOrder())
                val shipping =
                    spyk(
                        Shipping.create(
                            carrier = Carrier.CJLOGISTICS,
                            trackingNumber = "123456789",
                        ),
                    )
                every { order.shipping } returns shipping
                every { orderRepository.findById(orderId) } returns order
                every { orderRepository.save(order) } returns order

                // When
                val result =
                    orderService.addTrackingEvent(
                        orderId = orderId,
                        status = ShippingStatus.IN_TRANSIT,
                        location = "Seoul Hub",
                        description = "Package in transit",
                    )

                // Then
                result.shouldNotBeNull()
                verify(exactly = 1) {
                    shipping.addTrackingEvent(ShippingStatus.IN_TRANSIT, "Seoul Hub", "Package in transit")
                }
            }
        }

        context("when order has no shipping information") {
            it("should throw BusinessRuleException") {
                // Given
                val orderId = UUID.randomUUID().toString()
                val order = spyk(createMockOrder())
                every { order.shipping } returns null
                every { orderRepository.findById(orderId) } returns order

                // When & Then
                shouldThrow<BusinessRuleException> {
                    orderService.addTrackingEvent(
                        orderId = orderId,
                        status = ShippingStatus.IN_TRANSIT,
                        location = "Seoul Hub",
                        description = "Package in transit",
                    )
                }
            }
        }
    }

    describe("transactional boundaries") {
        it("should have @Transactional annotation on write operations") {
            // Given
            val method = OrderService::class.java.getMethod("createOrder", CreateOrderCommand::class.java)

            // Then
            val transactional = OrderService::class.java.getAnnotation(org.springframework.transaction.annotation.Transactional::class.java)
            transactional.shouldNotBeNull()
        }

        it("should have @Transactional(readOnly = true) on read operations") {
            // Given
            val method = OrderService::class.java.getMethod("getOrder", String::class.java)

            // Then
            val transactional = method.getAnnotation(org.springframework.transaction.annotation.Transactional::class.java)
            transactional.shouldNotBeNull()
            transactional.readOnly shouldBe true
        }
    }
})

// Test fixtures
private fun createOrderCommand(): CreateOrderCommand {
    return CreateOrderCommand(
        companyId = UUID.randomUUID().toString(),
        channelId = UUID.randomUUID().toString(),
        customerName = "홍길동",
        customerPhone = "010-1234-5678",
        customerEmail = "hong@example.com",
        shippingAddress =
            Address(
                zipCode = "12345",
                address1 = "서울시 강남구",
                address2 = "테헤란로 123",
                city = "서울",
                state = "서울",
                country = "KR",
            ),
        fulfillmentMethod = FulfillmentMethod.WMS,
        items =
            listOf(
                CreateOrderItemCommand(
                    productId = "prod-1",
                    productName = "상품 1",
                    sku = "SKU-001",
                    quantity = 2,
                    unitPrice = Money.of(10000, Currency.KRW),
                ),
                CreateOrderItemCommand(
                    productId = "prod-2",
                    productName = "상품 2",
                    sku = "SKU-002",
                    quantity = 1,
                    unitPrice = Money.of(20000, Currency.KRW),
                ),
            ),
    )
}

private fun createMockOrder(): Order {
    val order =
        Order.create(
            companyId = UUID.randomUUID().toString(),
            channelId = UUID.randomUUID().toString(),
            customer =
                Customer(
                    name = "홍길동",
                    phone = "010-1234-5678",
                    email = "hong@example.com",
                ),
            shippingAddress =
                Address(
                    zipCode = "12345",
                    address1 = "서울시 강남구",
                    address2 = "테헤란로 123",
                    city = "서울",
                    state = "서울",
                    country = "KR",
                ),
            fulfillmentMethod = FulfillmentMethod.WMS,
        )

    order.addItem(
        productId = "prod-1",
        productName = "상품 1",
        sku = "SKU-001",
        quantity = 2,
        unitPrice = Money.of(10000, Currency.KRW),
    )

    return order
}
