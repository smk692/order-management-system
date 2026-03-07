# Issue #6: Quality and Test Infrastructure Implementation Plan

## Context Clarification

### What the Original Plan Assumed (INCORRECT)
- Kotlin to Java 21 migration required
- Module consolidation from 9 to 4 modules
- 60-day implementation timeline for language migration

### What the Codebase Actually Has (VERIFIED)
- **Language**: Kotlin 1.9.22 (164 backend files, ~11,452 LOC)
- **JVM Target**: Java 21 (already configured in gradle.properties)
- **Virtual Threads**: Already configured in `VirtualThreadsConfig.kt`
- **Spring Boot**: 3.2.2 (native virtual thread support)
- **Module Structure**: 18 well-organized modules with clean DDD architecture
- **Frontend**: React 19 + TypeScript + Vite (54 source files)
- **Testing**: ZERO test files exist (neither backend nor frontend)
- **CI/CD**: Workflows exist but skip tests (no tests to run)

### Actual Goals (Re-scoped)
1. **Test Infrastructure**: Build comprehensive test coverage (target: 70%)
2. **Virtual Thread Verification**: Confirm VT is active and performing
3. **ArchUnit Tests**: Enforce architectural boundaries programmatically
4. **API DTO Verification**: DTOs already exist - verify OpenAPI generation
5. **Frontend Testing**: Vitest + Testing Library (already configured, no tests)
6. **Performance Baselines**: Measure and document VT throughput

---

## Phase 0: Environment Verification (30 minutes)

### Objective
Verify the development environment is functional before writing tests.

### Tasks

#### 0.1 Backend Build Verification
```bash
cd /Users/sonmingi/.openclaw/workspace/issue-pilot/.repos/order-management-system/backend
./gradlew clean build --no-daemon
```

**Acceptance Criteria**:
- Build completes without errors
- All 18 modules compile successfully
- Detekt and ktlint pass

#### 0.2 Docker Infrastructure
```bash
cd /Users/sonmingi/.openclaw/workspace/issue-pilot/.repos/order-management-system
docker-compose up -d postgres mongodb redis
docker-compose ps
```

**Acceptance Criteria**:
- All 3 infrastructure services healthy
- Postgres on port 5432
- MongoDB on port 27017
- Redis on port 6379

#### 0.3 Application Startup
```bash
cd /Users/sonmingi/.openclaw/workspace/issue-pilot/.repos/order-management-system/backend
./gradlew :api:bootRun
```

**Acceptance Criteria**:
- Application starts on port 8080
- Actuator health endpoint responds: `curl http://localhost:8080/actuator/health`
- OpenAPI docs available: `curl http://localhost:8080/swagger-ui.html`

#### 0.4 Virtual Thread Verification
Create a quick verification script:

```kotlin
// backend/api/src/main/kotlin/com/oms/api/controller/DiagnosticsController.kt
@RestController
@RequestMapping("/api/v1/diagnostics")
class DiagnosticsController {
    @GetMapping("/thread-info")
    fun getThreadInfo(): Map<String, Any> {
        val currentThread = Thread.currentThread()
        return mapOf(
            "threadName" to currentThread.name,
            "isVirtual" to currentThread.isVirtual,
            "threadId" to currentThread.threadId()
        )
    }
}
```

**Verification**:
```bash
curl http://localhost:8080/api/v1/diagnostics/thread-info
# Expected: {"threadName":"...","isVirtual":true,"threadId":...}
```

**Acceptance Criteria**:
- `isVirtual: true` in response

---

## Phase 1: Backend Test Infrastructure (2-3 hours)

### Objective
Set up the testing framework and create test directories.

### 1.1 Create Test Directory Structure

```bash
# Create test directories for all modules
cd /Users/sonmingi/.openclaw/workspace/issue-pilot/.repos/order-management-system/backend

# Core modules
mkdir -p core/core-domain/src/test/kotlin/com/oms/core/domain
mkdir -p core/core-infra/src/test/kotlin/com/oms/core/infra

# Domain modules
mkdir -p domain/domain-order/src/test/kotlin/com/oms/order
mkdir -p domain/domain-inventory/src/test/kotlin/com/oms/inventory
mkdir -p domain/domain-settlement/src/test/kotlin/com/oms/settlement
mkdir -p domain/domain-channel/src/test/kotlin/com/oms/channel
mkdir -p domain/domain-catalog/src/test/kotlin/com/oms/catalog
mkdir -p domain/domain-identity/src/test/kotlin/com/oms/identity
mkdir -p domain/domain-claim/src/test/kotlin/com/oms/claim
mkdir -p domain/domain-automation/src/test/kotlin/com/oms/automation
mkdir -p domain/domain-strategy/src/test/kotlin/com/oms/strategy

# Application module
mkdir -p application/src/test/kotlin/com/oms/application

# API module
mkdir -p api/src/test/kotlin/com/oms/api
mkdir -p api/src/test/kotlin/com/oms/api/controller
mkdir -p api/src/test/kotlin/com/oms/api/integration

# Infrastructure modules
mkdir -p infrastructure/infra-mysql/src/test/kotlin/com/oms/infra/mysql
mkdir -p infrastructure/infra-mongo/src/test/kotlin/com/oms/infra/mongo
mkdir -p infrastructure/infra-redis/src/test/kotlin/com/oms/infra/redis
```

### 1.2 Add Test Dependencies to Root build.gradle.kts

**File**: `backend/build.gradle.kts`

Add to `subprojects` block (already partially present):

```kotlin
dependencies {
    // Existing test deps (already present)
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("io.mockk:mockk:1.13.9")
    "testImplementation"("io.kotest:kotest-runner-junit5:5.8.0")
    "testImplementation"("io.kotest:kotest-assertions-core:5.8.0")

    // Add these new dependencies
    "testImplementation"("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    "testImplementation"("com.tngtech.archunit:archunit-junit5:1.2.1")
    "testImplementation"("org.testcontainers:testcontainers:1.19.4")
    "testImplementation"("org.testcontainers:junit-jupiter:1.19.4")
    "testImplementation"("org.testcontainers:postgresql:1.19.4")
    "testImplementation"("org.testcontainers:mongodb:1.19.4")
}
```

### 1.3 Create Test Configuration Base Class

**File**: `backend/api/src/test/kotlin/com/oms/api/TestConfig.kt`

```kotlin
package com.oms.api

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("oms_test")
            .withUsername("test")
            .withPassword("test")

        @Container
        @JvmStatic
        val mongodb = MongoDBContainer("mongo:7.0")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl)
        }
    }
}
```

### 1.4 Create application-test.yml

**File**: `backend/api/src/test/resources/application-test.yml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.oms: DEBUG
    org.springframework: INFO
    org.testcontainers: INFO
```

**Acceptance Criteria**:
- `./gradlew test` runs without configuration errors
- Test containers start successfully
- 0 tests run (expected - no tests written yet)

---

## Phase 2: Core Domain Unit Tests (3-4 hours)

### Objective
Test domain entities and value objects (pure logic, no infrastructure).

### 2.1 Money Value Object Tests

**File**: `backend/core/core-domain/src/test/kotlin/com/oms/core/domain/MoneyTest.kt`

```kotlin
package com.oms.core.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.math.BigDecimal

class MoneyTest : DescribeSpec({
    describe("Money value object") {
        it("should create money with valid amount and currency") {
            val money = Money(BigDecimal("100.00"), Currency.KRW)
            money.amount shouldBe BigDecimal("100.00")
            money.currency shouldBe Currency.KRW
        }

        it("should add two money values of same currency") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("50.00"), Currency.USD)
            val result = m1 + m2
            result.amount shouldBe BigDecimal("150.00")
        }

        it("should throw when adding different currencies") {
            val m1 = Money(BigDecimal("100.00"), Currency.USD)
            val m2 = Money(BigDecimal("100.00"), Currency.KRW)
            shouldThrow<IllegalArgumentException> {
                m1 + m2
            }
        }

        it("should multiply by quantity") {
            val money = Money(BigDecimal("25.00"), Currency.USD)
            val result = money * 4
            result.amount shouldBe BigDecimal("100.00")
        }
    }
})
```

### 2.2 Address Value Object Tests

**File**: `backend/core/core-domain/src/test/kotlin/com/oms/core/domain/AddressTest.kt`

```kotlin
package com.oms.core.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class AddressTest : DescribeSpec({
    describe("Address value object") {
        it("should create valid address") {
            val address = Address(
                recipient = "John Doe",
                phone = "010-1234-5678",
                zipCode = "12345",
                address1 = "123 Main St",
                address2 = "Apt 4B",
                city = "Seoul",
                state = "Seoul",
                country = "KR"
            )
            address.recipient shouldBe "John Doe"
            address.country shouldBe "KR"
        }

        it("should be equal when all fields match") {
            val a1 = Address("John", "010-1111-1111", "12345", "Street", null, "City", "State", "KR")
            val a2 = Address("John", "010-1111-1111", "12345", "Street", null, "City", "State", "KR")
            a1 shouldBe a2
        }

        it("should not be equal when fields differ") {
            val a1 = Address("John", "010-1111-1111", "12345", "Street", null, "City", "State", "KR")
            val a2 = Address("Jane", "010-1111-1111", "12345", "Street", null, "City", "State", "KR")
            a1 shouldNotBe a2
        }
    }
})
```

### 2.3 Order Domain Entity Tests

**File**: `backend/domain/domain-order/src/test/kotlin/com/oms/order/OrderTest.kt`

```kotlin
package com.oms.order

import com.oms.core.domain.*
import com.oms.order.domain.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import java.math.BigDecimal
import java.util.UUID

class OrderTest : DescribeSpec({
    describe("Order state machine") {
        val validAddress = Address("John", "010-1234-5678", "12345", "123 Main", null, "Seoul", "Seoul", "KR")
        val validCustomer = Customer("John Doe", "010-1234-5678", "john@example.com")

        fun createTestOrder(): Order {
            return Order.create(
                companyId = UUID.randomUUID().toString(),
                channelId = UUID.randomUUID().toString(),
                customer = validCustomer,
                shippingAddress = validAddress,
                fulfillmentMethod = FulfillmentMethod.WMS
            )
        }

        it("should create order in PENDING status") {
            val order = createTestOrder()
            order.status shouldBe OrderStatus.PENDING
        }

        it("should transition from PENDING to PAID") {
            val order = createTestOrder()
            order.markAsPaid()
            order.status shouldBe OrderStatus.PAID
        }

        it("should not allow shipping before payment") {
            val order = createTestOrder()
            shouldThrow<IllegalStateException> {
                order.ship(Carrier.CJ_LOGISTICS, "TRACK123")
            }
        }

        it("should follow complete order lifecycle") {
            val order = createTestOrder()
            order.addItem("product-1", "Test Product", "SKU001", 2, Money(BigDecimal("100"), Currency.KRW))

            // PENDING -> PAID
            order.markAsPaid()
            order.status shouldBe OrderStatus.PAID

            // PAID -> PREPARING
            order.startPreparing()
            order.status shouldBe OrderStatus.PREPARING

            // PREPARING -> READY_TO_SHIP
            order.markReadyToShip()
            order.status shouldBe OrderStatus.READY_TO_SHIP

            // READY_TO_SHIP -> SHIPPED
            order.ship(Carrier.CJ_LOGISTICS, "TRACK123")
            order.status shouldBe OrderStatus.SHIPPED

            // SHIPPED -> DELIVERED
            order.markDelivered()
            order.status shouldBe OrderStatus.DELIVERED
        }

        it("should calculate total correctly") {
            val order = createTestOrder()
            order.addItem("p1", "Product 1", "SKU1", 2, Money(BigDecimal("100"), Currency.KRW))
            order.addItem("p2", "Product 2", "SKU2", 1, Money(BigDecimal("50"), Currency.KRW))

            order.totalAmount.amount shouldBe BigDecimal("250")
        }
    }
})
```

**Acceptance Criteria**:
- `./gradlew :core:core-domain:test` passes
- `./gradlew :domain:domain-order:test` passes
- Coverage report shows >80% for tested classes

---

## Phase 3: Service Layer Tests (3-4 hours)

### Objective
Test application services with mocked repositories.

### 3.1 OrderService Unit Tests

**File**: `backend/application/src/test/kotlin/com/oms/application/OrderServiceTest.kt`

```kotlin
package com.oms.application

import com.oms.application.order.*
import com.oms.core.domain.*
import com.oms.order.domain.*
import com.oms.order.repository.OrderRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import java.math.BigDecimal
import java.util.UUID

class OrderServiceTest : DescribeSpec({
    val orderRepository = mockk<OrderRepository>()
    val orderService = OrderService(orderRepository)

    beforeTest {
        clearMocks(orderRepository)
    }

    describe("createOrder") {
        it("should create order and save to repository") {
            val companyId = UUID.randomUUID().toString()
            val channelId = UUID.randomUUID().toString()

            every { orderRepository.save(any()) } answers { firstArg() }

            val command = CreateOrderCommand(
                companyId = companyId,
                channelId = channelId,
                customerName = "John Doe",
                customerPhone = "010-1234-5678",
                customerEmail = "john@example.com",
                shippingAddress = Address("John", "010-1234-5678", "12345", "Main St", null, "Seoul", "Seoul", "KR"),
                fulfillmentMethod = FulfillmentMethod.WMS,
                items = listOf(
                    CreateOrderItemCommand("p1", "Product 1", "SKU1", 2, Money(BigDecimal("100"), Currency.KRW))
                )
            )

            val result = orderService.createOrder(command)

            result shouldNotBe null
            result.companyId shouldBe companyId
            result.status shouldBe OrderStatus.PENDING

            verify(exactly = 1) { orderRepository.save(any()) }
        }
    }

    describe("markOrderPaid") {
        it("should transition order to PAID status") {
            val orderId = UUID.randomUUID().toString()
            val order = Order.create(
                companyId = UUID.randomUUID().toString(),
                channelId = UUID.randomUUID().toString(),
                customer = Customer("John", "010-1234-5678", null),
                shippingAddress = Address("John", "010", "12345", "St", null, "City", "State", "KR"),
                fulfillmentMethod = FulfillmentMethod.WMS
            )

            every { orderRepository.findById(orderId) } returns order
            every { orderRepository.save(any()) } answers { firstArg() }

            val result = orderService.markOrderPaid(orderId)

            result.status shouldBe OrderStatus.PAID
            verify { orderRepository.save(match { it.status == OrderStatus.PAID }) }
        }
    }
})
```

### 3.2 SettlementService Tests

**File**: `backend/application/src/test/kotlin/com/oms/application/SettlementServiceTest.kt`

```kotlin
package com.oms.application

import com.oms.application.settlement.*
import com.oms.application.settlement.dto.*
import com.oms.settlement.domain.*
import com.oms.settlement.repository.SettlementRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class SettlementServiceTest : DescribeSpec({
    val settlementRepository = mockk<SettlementRepository>()
    val settlementService = SettlementService(settlementRepository)

    beforeTest {
        clearMocks(settlementRepository)
    }

    describe("createSettlement") {
        it("should create settlement with calculated amounts") {
            val companyId = UUID.randomUUID().toString()

            every { settlementRepository.save(any()) } answers { firstArg() }

            val command = CreateSettlementCommand(
                companyId = companyId,
                channelId = UUID.randomUUID().toString(),
                periodStart = LocalDate.now().minusDays(7),
                periodEnd = LocalDate.now(),
                orderIds = listOf("order-1", "order-2"),
                grossAmount = BigDecimal("10000"),
                fees = listOf(
                    FeeCommand("platform_fee", BigDecimal("300")),
                    FeeCommand("payment_fee", BigDecimal("200"))
                )
            )

            val result = settlementService.createSettlement(command)

            result.grossAmount shouldBe BigDecimal("10000")
            result.totalFees shouldBe BigDecimal("500")
            result.netAmount shouldBe BigDecimal("9500")
            result.status shouldBe SettlementStatus.PENDING
        }
    }

    describe("approveSettlement") {
        it("should transition settlement to APPROVED") {
            val settlementId = UUID.randomUUID().toString()
            val settlement = Settlement.create(/* ... mock data ... */)

            every { settlementRepository.findById(settlementId) } returns settlement
            every { settlementRepository.save(any()) } answers { firstArg() }

            val result = settlementService.approveSettlement(settlementId, "admin-user")

            result.status shouldBe SettlementStatus.APPROVED
        }
    }
})
```

**Acceptance Criteria**:
- `./gradlew :application:test` passes
- All service methods have at least one test
- Mocking correctly isolates service logic

---

## Phase 4: Integration Tests with Testcontainers (4-5 hours)

### Objective
Test complete flows with real database connections.

### 4.1 Order API Integration Test

**File**: `backend/api/src/test/kotlin/com/oms/api/integration/OrderApiIntegrationTest.kt`

```kotlin
package com.oms.api.integration

import com.oms.api.IntegrationTestBase
import com.oms.api.controller.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.util.UUID

class OrderApiIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @org.junit.jupiter.api.Test
    fun `should create order via REST API`() {
        val request = CreateOrderRequest(
            companyId = UUID.randomUUID().toString(),
            channelId = UUID.randomUUID().toString(),
            customer = CustomerDto(
                name = "Integration Test Customer",
                phone = "010-9999-8888",
                email = "test@example.com"
            ),
            shippingAddress = AddressDto(
                recipient = "Test Recipient",
                phone = "010-9999-8888",
                zipCode = "12345",
                address1 = "123 Test Street",
                address2 = "Suite 100",
                city = "Seoul",
                state = "Seoul",
                country = "KR"
            ),
            fulfillmentMethod = "WMS",
            items = listOf(
                CreateOrderItemDto(
                    productId = UUID.randomUUID().toString(),
                    productName = "Test Product",
                    sku = "TEST-SKU-001",
                    quantity = 2,
                    unitPrice = MoneyDto("10000", "KRW")
                )
            )
        )

        val response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse::class.java
        )

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldNotBe null
        response.body!!.status shouldBe "PENDING"
        response.body!!.items.size shouldBe 1
    }

    @org.junit.jupiter.api.Test
    fun `should complete full order lifecycle`() {
        // Create order
        val createRequest = createTestOrderRequest()
        val createResponse = restTemplate.postForEntity("/api/v1/orders", createRequest, OrderResponse::class.java)
        val orderId = createResponse.body!!.id

        // Mark as paid
        val paidResponse = restTemplate.postForEntity("/api/v1/orders/$orderId/pay", null, OrderResponse::class.java)
        paidResponse.body!!.status shouldBe "PAID"

        // Start preparing
        val preparingResponse = restTemplate.postForEntity("/api/v1/orders/$orderId/prepare", null, OrderResponse::class.java)
        preparingResponse.body!!.status shouldBe "PREPARING"

        // Ready to ship
        val readyResponse = restTemplate.postForEntity("/api/v1/orders/$orderId/ready-to-ship", null, OrderResponse::class.java)
        readyResponse.body!!.status shouldBe "READY_TO_SHIP"

        // Ship
        val shipRequest = ShipOrderRequest(carrier = "CJ_LOGISTICS", trackingNumber = "CJ123456789")
        val shippedResponse = restTemplate.postForEntity("/api/v1/orders/$orderId/ship", shipRequest, OrderResponse::class.java)
        shippedResponse.body!!.status shouldBe "SHIPPED"

        // Deliver
        val deliveredResponse = restTemplate.postForEntity("/api/v1/orders/$orderId/deliver", null, OrderResponse::class.java)
        deliveredResponse.body!!.status shouldBe "DELIVERED"
    }

    private fun createTestOrderRequest() = CreateOrderRequest(
        companyId = UUID.randomUUID().toString(),
        channelId = UUID.randomUUID().toString(),
        customer = CustomerDto("Test", "010-1111-1111", null),
        shippingAddress = AddressDto("R", "010", "123", "St", null, "C", "S", "KR"),
        items = listOf(CreateOrderItemDto("p1", "Prod", "SKU", 1, MoneyDto("1000", "KRW")))
    )
}
```

### 4.2 Virtual Thread Integration Test

**File**: `backend/api/src/test/kotlin/com/oms/api/integration/VirtualThreadIntegrationTest.kt`

```kotlin
package com.oms.api.integration

import com.oms.api.IntegrationTestBase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

class VirtualThreadIntegrationTest : IntegrationTestBase() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should execute requests on virtual threads`() {
        val response = restTemplate.getForEntity(
            "/api/v1/diagnostics/thread-info",
            Map::class.java
        )

        response.statusCode shouldBe HttpStatus.OK
        response.body!!["isVirtual"] shouldBe true
    }

    @Test
    fun `should handle concurrent requests with virtual threads`() {
        val results = (1..100).map { i ->
            Thread.startVirtualThread {
                restTemplate.getForEntity("/api/v1/diagnostics/thread-info", Map::class.java)
            }
        }.map { it.join() }

        // All 100 concurrent requests should complete
        results.size shouldBe 100
    }
}
```

**Acceptance Criteria**:
- `./gradlew :api:test` passes
- Testcontainers start automatically
- Full order lifecycle test passes
- Virtual thread verification confirms `isVirtual: true`

---

## Phase 5: ArchUnit Architecture Tests (2 hours)

### Objective
Enforce architectural boundaries programmatically.

### 5.1 Architecture Rules

**File**: `backend/api/src/test/kotlin/com/oms/api/ArchitectureTest.kt`

```kotlin
package com.oms.api

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll

class ArchitectureTest {

    companion object {
        private val classes = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.oms")
    }

    @Test
    fun `domain layer should not depend on infrastructure`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..infra..")
            .check(classes)
    }

    @Test
    fun `domain layer should not depend on Spring framework`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..")
            .check(classes)
    }

    @Test
    fun `application layer should not depend on API layer`() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..api..")
            .check(classes)
    }

    @Test
    fun `controllers should only depend on application services`() {
        classes()
            .that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..controller..",
                "..application..",
                "..domain..",
                "..dto..",
                "java..",
                "kotlin..",
                "org.springframework..",
                "io.swagger..",
                "jakarta.."
            )
            .check(classes)
    }

    @Test
    fun `layered architecture should be respected`() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("API").definedBy("..api..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..", "..infra..")
            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("API")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("API", "Application", "Infrastructure")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .check(classes)
    }

    @Test
    fun `domain modules should not have cyclic dependencies`() {
        slices()
            .matching("com.oms.(*)..")
            .should().beFreeOfCycles()
            .check(classes)
    }

    @Test
    fun `entities should not be exposed in API responses`() {
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat()
            .areAnnotatedWith(jakarta.persistence.Entity::class.java)
            .check(classes)
    }
}
```

**Acceptance Criteria**:
- All architecture tests pass
- Violations clearly reported if any
- CI fails on architecture violations

---

## Phase 6: Frontend Testing Setup (2-3 hours)

### Objective
Add component and integration tests to React frontend.

### 6.1 Test Setup Enhancement

**File**: `frontend/src/test/setup.ts` (update existing)

```typescript
import '@testing-library/jest-dom'
import { cleanup } from '@testing-library/react'
import { afterEach, vi } from 'vitest'

// Cleanup after each test
afterEach(() => {
  cleanup()
})

// Mock IntersectionObserver
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})
```

### 6.2 Test Utilities

**File**: `frontend/src/test/utils.tsx`

```typescript
import { render, RenderOptions } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactElement, ReactNode } from 'react'

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
    },
  })

interface WrapperProps {
  children: ReactNode
}

const AllTheProviders = ({ children }: WrapperProps) => {
  const queryClient = createTestQueryClient()
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

const customRender = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => render(ui, { wrapper: AllTheProviders, ...options })

export * from '@testing-library/react'
export { customRender as render }
```

### 6.3 Component Test Example

**File**: `frontend/src/features/orders/OrderList.test.tsx`

```typescript
import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '../../test/utils'
import { OrderList } from './OrderList'
import { rest } from 'msw'
import { setupServer } from 'msw/node'

const mockOrders = [
  {
    id: 'order-1',
    companyId: 'company-1',
    channelId: 'channel-1',
    status: 'PENDING',
    orderDate: '2024-01-15T10:00:00Z',
    customer: { name: 'John Doe', phone: '010-1234-5678', email: 'john@example.com' },
    shippingAddress: { recipient: 'John', phone: '010', zipCode: '123', address1: 'St', country: 'KR' },
    totalAmount: { amount: '10000', currency: 'KRW' },
    items: [],
  },
]

const server = setupServer(
  rest.get('/api/v1/orders/company/:companyId', (req, res, ctx) => {
    return res(ctx.json(mockOrders))
  })
)

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('OrderList', () => {
  it('should display loading state initially', () => {
    render(<OrderList companyId="company-1" />)
    expect(screen.getByText(/loading/i)).toBeInTheDocument()
  })

  it('should display orders after loading', async () => {
    render(<OrderList companyId="company-1" />)

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument()
    })

    expect(screen.getByText('PENDING')).toBeInTheDocument()
    expect(screen.getByText('10,000 KRW')).toBeInTheDocument()
  })

  it('should display error on API failure', async () => {
    server.use(
      rest.get('/api/v1/orders/company/:companyId', (req, res, ctx) => {
        return res(ctx.status(500))
      })
    )

    render(<OrderList companyId="company-1" />)

    await waitFor(() => {
      expect(screen.getByText(/error/i)).toBeInTheDocument()
    })
  })
})
```

### 6.4 Add MSW to package.json

```bash
cd /Users/sonmingi/.openclaw/workspace/issue-pilot/.repos/order-management-system/frontend
npm install -D msw@2.0.0
```

**Acceptance Criteria**:
- `npm test` runs successfully
- Component tests render with providers
- MSW intercepts API calls
- Test coverage report generated

---

## Phase 7: CI/CD Integration (1-2 hours)

### Objective
Update CI workflows to run all tests.

### 7.1 Update Backend CI

**File**: `.github/workflows/backend-ci.yml` (update test job)

```yaml
  test:
    name: Test Backend
    runs-on: ubuntu-latest
    needs: build

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_PASSWORD: test
          POSTGRES_USER: test
          POSTGRES_DB: oms_test
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run tests with coverage
        working-directory: ./backend
        run: ./gradlew test koverReport --no-daemon

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: backend/build/reports/kover
          retention-days: 14

      - name: Check coverage threshold
        working-directory: ./backend
        run: |
          COVERAGE=$(cat build/reports/kover/report.xml | grep -o 'line-rate="[0-9.]*"' | head -1 | cut -d'"' -f2)
          echo "Coverage: $COVERAGE"
          if (( $(echo "$COVERAGE < 0.70" | bc -l) )); then
            echo "Coverage below 70% threshold!"
            exit 1
          fi
```

### 7.2 Update Frontend CI

**File**: `.github/workflows/frontend-ci.yml` (add test job)

```yaml
  test:
    name: Test Frontend
    runs-on: ubuntu-latest
    needs: build

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci

      - name: Run tests
        working-directory: ./frontend
        run: npm run test:coverage

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: frontend-coverage
          path: frontend/coverage
          retention-days: 14
```

**Acceptance Criteria**:
- CI runs all tests on PR/push
- Coverage reports uploaded as artifacts
- Build fails if coverage < 70%

---

## Verification Commands

After each phase, run these commands to verify progress:

```bash
# Phase 0: Environment
cd backend && ./gradlew build --no-daemon
docker-compose up -d && docker-compose ps
curl http://localhost:8080/actuator/health

# Phase 1-4: Backend tests
./gradlew test --no-daemon
./gradlew koverReport
open build/reports/kover/html/index.html

# Phase 5: Architecture tests
./gradlew :api:test --tests "*ArchitectureTest*"

# Phase 6: Frontend tests
cd frontend && npm test
npm run test:coverage

# Phase 7: Full CI simulation
./gradlew check
cd frontend && npm run lint && npm test
```

---

## Timeline Estimate

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 0: Environment Verification | 30 min | None |
| Phase 1: Test Infrastructure | 2-3 hours | Phase 0 |
| Phase 2: Domain Unit Tests | 3-4 hours | Phase 1 |
| Phase 3: Service Layer Tests | 3-4 hours | Phase 1 |
| Phase 4: Integration Tests | 4-5 hours | Phase 2, 3 |
| Phase 5: ArchUnit Tests | 2 hours | Phase 1 |
| Phase 6: Frontend Testing | 2-3 hours | None (parallel) |
| Phase 7: CI/CD Integration | 1-2 hours | Phase 4-6 |

**Total: ~18-24 hours of implementation work**

---

## Success Criteria

1. **Backend test coverage >= 70%** (verified via Kover report)
2. **All ArchUnit tests pass** (no architectural violations)
3. **Virtual threads confirmed active** (`isVirtual: true` in diagnostics)
4. **Frontend test coverage >= 60%** (verified via Vitest coverage)
5. **CI/CD runs all tests on PR** (GitHub Actions green)
6. **Zero critical lint/detekt violations**

---

## Open Questions

1. **Database seeding**: Should integration tests seed specific test data, or rely on test setup creating necessary entities?
2. **Performance baselines**: What specific metrics should we capture for virtual thread performance? (requests/sec, latency p99, thread pool utilization)
3. **Frontend E2E**: Should we add Playwright/Cypress for E2E tests in this phase, or defer to a future phase?
4. **API contract testing**: Should we add Spring Cloud Contract or Pact for consumer-driven contract testing?

---

## Notes for Autopilot Executor

1. **Start with Phase 0** to verify the environment works before writing tests
2. **Phase 1 is critical** - without test infrastructure, nothing else works
3. **Phases 2-5 can be partially parallelized** (unit tests don't block architecture tests)
4. **Phase 6 is independent** and can run in parallel with backend phases
5. **Use `./gradlew test --continuous`** for fast feedback during development
6. **Check existing code** before writing tests - DTOs and controllers already exist with good patterns
7. **The VirtualThreadsConfig already exists** - just need to verify it's working
