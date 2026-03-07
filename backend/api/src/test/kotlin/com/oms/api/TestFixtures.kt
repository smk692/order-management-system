package com.oms.api

import com.oms.core.domain.Address
import com.oms.core.domain.Currency
import com.oms.core.domain.Money
import com.oms.order.domain.Carrier
import com.oms.order.domain.Customer
import com.oms.order.domain.FulfillmentMethod
import com.oms.order.domain.Order
import java.math.BigDecimal
import java.util.UUID

/**
 * Test data fixtures for creating test objects easily and consistently.
 *
 * This object provides factory methods for creating test data with sensible defaults.
 * All methods support parameter overriding for flexibility.
 *
 * Usage:
 * ```kotlin
 * // Use defaults
 * val customer = TestFixtures.customerFixture()
 *
 * // Override specific fields
 * val address = TestFixtures.addressFixture(city = "Busan", country = "KR")
 *
 * // Create complete order
 * val order = TestFixtures.orderFixture(
 *     companyId = "company-123",
 *     items = listOf(TestFixtures.orderItemData())
 * )
 * ```
 *
 * Design Principles:
 * - Sensible defaults: All parameters have default values
 * - Named parameters: Clear what you're overriding
 * - Deterministic: Same inputs produce same outputs
 * - Minimal coupling: Uses value objects and primitives
 */
object TestFixtures {
    /**
     * Create a test Address with Korean defaults
     */
    fun addressFixture(
        recipient: String = "홍길동",
        phone: String = "010-1234-5678",
        zipCode: String = "06234",
        address1: String = "서울특별시 강남구 테헤란로 123",
        address2: String? = "ABC빌딩 4층",
        city: String = "서울",
        state: String = "서울특별시",
        country: String = "KR",
    ): Address =
        Address(
            recipient = recipient,
            phone = phone,
            zipCode = zipCode,
            address1 = address1,
            address2 = address2,
            city = city,
            state = state,
            country = country,
        )

    /**
     * Create a test Address with US defaults
     */
    fun usAddressFixture(
        recipient: String = "John Doe",
        phone: String = "+1-555-0123",
        zipCode: String = "94105",
        address1: String = "123 Market Street",
        address2: String? = "Suite 400",
        city: String = "San Francisco",
        state: String = "CA",
        country: String = "US",
    ): Address =
        Address(
            recipient = recipient,
            phone = phone,
            zipCode = zipCode,
            address1 = address1,
            address2 = address2,
            city = city,
            state = state,
            country = country,
        )

    /**
     * Create a test Customer
     */
    fun customerFixture(
        name: String = "김테스트",
        phone: String = "010-9999-8888",
        email: String? = "test@example.com",
    ): Customer =
        Customer(
            name = name,
            phone = phone,
            email = email,
        )

    /**
     * Create test Money (KRW)
     */
    fun moneyFixture(
        amount: BigDecimal = BigDecimal("10000"),
        currency: Currency = Currency.KRW,
    ): Money =
        Money(
            amount = amount,
            currency = currency,
        )

    /**
     * Create test Money (USD)
     */
    fun usdMoneyFixture(
        amount: BigDecimal = BigDecimal("100.00"),
        currency: Currency = Currency.USD,
    ): Money =
        Money(
            amount = amount,
            currency = currency,
        )

    /**
     * Create a test Order (domain entity)
     */
    fun orderFixture(
        companyId: String = UUID.randomUUID().toString(),
        channelId: String = UUID.randomUUID().toString(),
        customer: Customer = customerFixture(),
        shippingAddress: Address = addressFixture(),
        fulfillmentMethod: FulfillmentMethod = FulfillmentMethod.WMS,
        externalOrderId: String? = null,
    ): Order =
        Order.create(
            companyId = companyId,
            channelId = channelId,
            customer = customer,
            shippingAddress = shippingAddress,
            fulfillmentMethod = fulfillmentMethod,
            externalOrderId = externalOrderId,
        )

    /**
     * Create a test Order with items
     */
    fun orderWithItemsFixture(
        companyId: String = UUID.randomUUID().toString(),
        channelId: String = UUID.randomUUID().toString(),
        itemCount: Int = 2,
    ): Order {
        val order = orderFixture(companyId = companyId, channelId = channelId)

        repeat(itemCount) { index ->
            order.addItem(
                productId = "product-${index + 1}",
                productName = "테스트 상품 ${index + 1}",
                sku = "SKU-TEST-${String.format("%03d", index + 1)}",
                quantity = (index + 1),
                unitPrice = Money(BigDecimal("10000"), Currency.KRW),
            )
        }

        return order
    }

    /**
     * Data class for creating order items in tests
     */
    data class OrderItemData(
        val productId: String = UUID.randomUUID().toString(),
        val productName: String = "테스트 상품",
        val sku: String = "SKU-TEST-001",
        val quantity: Int = 1,
        val unitPrice: Money = Money(BigDecimal("10000"), Currency.KRW),
    )

    /**
     * Create test order item data
     */
    fun orderItemData(
        productId: String = UUID.randomUUID().toString(),
        productName: String = "테스트 상품",
        sku: String = "SKU-TEST-001",
        quantity: Int = 1,
        unitPrice: Money = Money(BigDecimal("10000"), Currency.KRW),
    ): OrderItemData =
        OrderItemData(
            productId = productId,
            productName = productName,
            sku = sku,
            quantity = quantity,
            unitPrice = unitPrice,
        )

    /**
     * Common carrier for shipping tests
     */
    fun carrierFixture(): Carrier = Carrier.CJ

    /**
     * Generate a valid tracking number
     */
    fun trackingNumberFixture(carrier: Carrier = Carrier.CJ): String {
        return when (carrier) {
            Carrier.CJ -> "CJ${UUID.randomUUID().toString().substring(0, 12).uppercase()}"
            Carrier.HANJIN -> "HJ${UUID.randomUUID().toString().substring(0, 12).uppercase()}"
            Carrier.LOGEN -> "LG${UUID.randomUUID().toString().substring(0, 12).uppercase()}"
            Carrier.POST -> "POST${UUID.randomUUID().toString().substring(0, 10).uppercase()}"
            Carrier.FEDEX -> "FEDEX${UUID.randomUUID().toString().substring(0, 9).uppercase()}"
            Carrier.DHL -> "DHL${UUID.randomUUID().toString().substring(0, 11).uppercase()}"
            Carrier.UPS -> "UPS${UUID.randomUUID().toString().substring(0, 11).uppercase()}"
        }
    }

    /**
     * Random company ID for multi-tenancy tests
     */
    fun companyId(): String = "company-${UUID.randomUUID()}"

    /**
     * Random channel ID for channel tests
     */
    fun channelId(): String = "channel-${UUID.randomUUID()}"

    /**
     * Random product ID
     */
    fun productId(): String = "product-${UUID.randomUUID()}"
}
