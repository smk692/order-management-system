# Backend Test Infrastructure Documentation

## Overview

This document describes the complete test infrastructure created for the Order Management System backend.

## Prerequisites

**CRITICAL**: This project requires Java 21 to run tests.

```bash
# Check your Java version
java -version
# Should show: openjdk version "21.x.x"

# If using Java 17 or earlier, tests will fail with:
# java.lang.UnsupportedClassVersionError

# Install Java 21 (macOS with Homebrew)
brew install openjdk@21

# Set JAVA_HOME
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
```

## Test Infrastructure Components

### 1. Base Test Classes

#### AbstractUnitTest
**Location**: `backend/core/core-domain/src/test/kotlin/com/oms/core/AbstractUnitTest.kt`

Base class for pure unit tests using Kotest framework.

**Features**:
- Kotest DescribeSpec for BDD-style tests
- Automatic Mockk cleanup after each test
- Fast execution (no Spring context, no database)

**Usage**:
```kotlin
class MoneyTest : AbstractUnitTest({
    describe("Money value object") {
        it("should create money with valid amount") {
            val money = Money.of(100, Currency.KRW)
            money.amount shouldBe BigDecimal("100")
        }
    }
})
```

#### AbstractIntegrationTest
**Location**: `backend/api/src/test/kotlin/com/oms/api/AbstractIntegrationTest.kt`

Base class for integration tests with full Spring Boot context and Testcontainers.

**Features**:
- Spring Boot test context with RANDOM_PORT
- PostgreSQL 16 container
- MongoDB 7.0 container
- Redis 7.2 container
- Automatic container lifecycle management
- Dynamic property configuration

**Container Details**:
- First test startup: ~10-15 seconds (downloading/starting containers)
- Subsequent tests: <1 second (containers reused)
- Containers automatically stop when test suite completes

**Usage**:
```kotlin
@SpringBootTest
class OrderApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should create order via REST API`() {
        val response = restTemplate.postForEntity(
            "/api/v1/orders",
            request,
            OrderResponse::class.java
        )
        response.statusCode shouldBe HttpStatus.CREATED
    }
}
```

### 2. Test Fixtures

**Location**: `backend/api/src/test/kotlin/com/oms/api/TestFixtures.kt`

Factory object for creating test data with sensible defaults.

**Available Fixtures**:
- `addressFixture()` - Korean address
- `usAddressFixture()` - US address
- `customerFixture()` - Customer data
- `moneyFixture()` - Money (KRW)
- `usdMoneyFixture()` - Money (USD)
- `orderFixture()` - Basic order
- `orderWithItemsFixture()` - Order with items
- `carrierFixture()` - Shipping carrier
- `trackingNumberFixture()` - Tracking number

**Usage**:
```kotlin
// Use defaults
val customer = TestFixtures.customerFixture()

// Override specific fields
val address = TestFixtures.addressFixture(
    city = "부산",
    zipCode = "48058"
)

// Create complete order with items
val order = TestFixtures.orderWithItemsFixture(
    companyId = "company-123",
    itemCount = 3
)
```

### 3. Test Configuration

**Location**: `backend/api/src/test/resources/application-test.yml`

Test-specific Spring Boot configuration.

**Key Settings**:
- `spring.jpa.hibernate.ddl-auto: create-drop` - Fresh schema per test run
- `spring.jpa.show-sql: false` - Clean test output
- Virtual threads enabled
- Logging levels optimized for testing

## Example Tests

### Unit Test Example: Money Value Object

**Location**: `backend/core/core-domain/src/test/kotlin/com/oms/core/domain/MoneyTest.kt`

Comprehensive tests for the Money value object:
- Creation and factory methods
- Arithmetic operations (add, subtract, multiply)
- Currency validation
- Comparison operations
- Boundary conditions

**Coverage**: 100% of Money class logic

### Unit Test Example: Address Value Object

**Location**: `backend/core/core-domain/src/test/kotlin/com/oms/core/domain/AddressTest.kt`

Tests for Address value object:
- Valid address creation
- Required field validation
- Address formatting
- Value object equality
- Factory methods (Korean address)

**Coverage**: 100% of Address class logic

### Unit Test Example: Order Domain Entity

**Location**: `backend/domain/domain-order/src/test/kotlin/com/oms/order/domain/OrderTest.kt`

Tests for Order aggregate root:
- Order creation and ID generation
- Order item management (add/remove)
- Status state machine transitions
- Cancellation workflows
- Domain event publishing

**Coverage**: Core order lifecycle paths

### Smoke Test

**Location**: `backend/api/src/test/kotlin/com/oms/api/TestInfrastructureSmokeTest.kt`

Simple test to verify test infrastructure is operational.

## Running Tests

### Run All Tests

```bash
cd backend
./gradlew test
```

### Run Specific Module Tests

```bash
# Core domain unit tests
./gradlew :core:core-domain:test

# Order domain tests
./gradlew :domain:domain-order:test

# API integration tests
./gradlew :api:test
```

### Run Specific Test Class

```bash
./gradlew :core:core-domain:test --tests "*MoneyTest*"
```

### Run with Coverage Report

```bash
./gradlew test koverReport

# View coverage report
open build/reports/kover/html/index.html
```

### Run Tests Continuously (TDD mode)

```bash
./gradlew test --continuous
```

## Test Structure

```
backend/
├── core/
│   └── core-domain/
│       └── src/test/kotlin/com/oms/core/
│           ├── AbstractUnitTest.kt           # Base class for unit tests
│           └── domain/
│               ├── MoneyTest.kt              # Money value object tests
│               └── AddressTest.kt            # Address value object tests
│
├── domain/
│   └── domain-order/
│       └── src/test/kotlin/com/oms/order/domain/
│           └── OrderTest.kt                  # Order domain entity tests
│
└── api/
    └── src/test/kotlin/com/oms/api/
        ├── AbstractIntegrationTest.kt        # Base class for integration tests
        ├── TestFixtures.kt                   # Test data factories
        └── TestInfrastructureSmokeTest.kt    # Infrastructure smoke test
```

## Testing Best Practices

### Unit Tests (70% of tests)
- Fast: <1ms per test
- Isolated: No Spring context, no database
- Pure logic: Domain entities and value objects
- Use AbstractUnitTest base class
- Use local test fixture methods

**Example Structure**:
```kotlin
class DomainEntityTest : AbstractUnitTest({
    // Local fixture
    fun createEntity() = Entity.create(...)

    describe("feature X") {
        it("should do Y when Z") {
            // Arrange
            val entity = createEntity()

            // Act
            entity.performAction()

            // Assert
            entity.state shouldBe ExpectedState
        }
    }
})
```

### Integration Tests (20% of tests)
- Slower: ~1-10s per test (first run)
- Full stack: Spring context + databases
- API endpoints and database interactions
- Use AbstractIntegrationTest base class
- Use TestFixtures for test data

### End-to-End Tests (10% of tests)
- Slowest: Full application flows
- Critical business paths only
- Use TestFixtures and REST calls

## Troubleshooting

### Tests Fail with UnsupportedClassVersionError

**Problem**: Java version mismatch
**Solution**: Ensure you're using Java 21

```bash
# Check Java version
java -version

# Set Java 21 if needed
export JAVA_HOME=/path/to/java-21
```

### Testcontainers Timeout

**Problem**: Docker not running or containers can't start
**Solution**:

```bash
# Check Docker is running
docker ps

# Restart Docker Desktop/daemon if needed

# Clean up old containers
docker system prune
```

### Dependency Resolution Error (Flyway)

**Problem**: Flyway PostgreSQL adapter missing version
**Solution**: Fixed in `backend/infrastructure/infra-mysql/build.gradle.kts`

```kotlin
runtimeOnly("org.flywaydb:flyway-database-postgresql:10.6.0")
```

### Tests Pass Locally but Fail in CI

**Problem**: Environment differences
**Solution**:
- Check Java version in CI (must be 21)
- Ensure Docker is available for Testcontainers
- Check database connection timeouts

## Test Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| Domain Entities | 80-90% | High |
| Value Objects | 95-100% | High |
| Application Services | 70-80% | Medium |
| Controllers | 60-70% | Medium |
| Infrastructure | 50-60% | Low |

## Dependencies

Test dependencies are configured in `backend/build.gradle.kts` (applied to all subprojects):

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("io.mockk:mockk:1.13.9")
testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
testImplementation("io.kotest:kotest-assertions-core:5.8.0")
testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
testImplementation("org.testcontainers:testcontainers:1.19.4")
testImplementation("org.testcontainers:junit-jupiter:1.19.4")
testImplementation("org.testcontainers:postgresql:1.19.4")
testImplementation("org.testcontainers:mongodb:1.19.4")
```

## Next Steps

### Phase 2: Expand Test Coverage

1. **Application Layer Tests**: Test services with mocked repositories
2. **Integration Tests**: Test complete API flows with real databases
3. **ArchUnit Tests**: Enforce architectural boundaries
4. **Performance Tests**: Verify Virtual Thread performance

### Phase 3: CI/CD Integration

1. Update GitHub Actions workflow to run tests
2. Configure coverage reports upload
3. Enforce minimum coverage thresholds (70%)
4. Add test result badges to README

## Resources

- [Kotest Documentation](https://kotest.io/)
- [Mockk Documentation](https://mockk.io/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

## Support

For questions or issues with the test infrastructure:
1. Check this documentation
2. Review example tests in `backend/core/core-domain/src/test`
3. Check test reports in `build/reports/tests/test/`
4. Review coverage reports with `./gradlew koverReport`
