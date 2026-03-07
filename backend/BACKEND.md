# Backend - Global OMS

This document provides detailed documentation for the Global OMS backend.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Bounded Contexts](#bounded-contexts)
- [Development](#development)
- [Database](#database)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Deployment](#deployment)

## Overview

The Global OMS backend is built using Domain-Driven Design (DDD) principles with Kotlin and Spring Boot. It leverages JDK 21's Virtual Threads for improved concurrency and uses PostgreSQL as the primary database with MongoDB for flexible schemas.

## Technology Stack

- **Language**: Kotlin 1.9
- **Runtime**: JDK 21 (Virtual Threads enabled)
- **Framework**: Spring Boot 3.2
- **Databases**:
  - PostgreSQL 16 (primary RDBMS)
  - MongoDB 7.0 (documents, events)
  - Redis 7 (caching)
- **Build Tool**: Gradle 8.5
- **Testing**: JUnit 5, MockK, Testcontainers
- **Code Quality**: detekt, ktlint
- **API Documentation**: SpringDoc OpenAPI (Swagger)

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│        API Layer (api)              │
│  - REST Controllers                 │
│  - Request/Response DTOs            │
│  - Exception Handlers               │
└─────────────────────────────────────┘
              ▼
┌─────────────────────────────────────┐
│   Application Layer (application)   │
│  - Application Services             │
│  - Use Cases                        │
│  - Event Handlers                   │
└─────────────────────────────────────┘
              ▼
┌─────────────────────────────────────┐
│     Domain Layer (domain)           │
│  - Entities & Value Objects         │
│  - Domain Services                  │
│  - Repository Interfaces            │
│  - Domain Events                    │
└─────────────────────────────────────┘
              ▼
┌─────────────────────────────────────┐
│  Infrastructure Layer (infra)       │
│  - JPA Repositories (PostgreSQL)    │
│  - MongoDB Repositories             │
│  - Redis Cache                      │
│  - External Service Clients         │
└─────────────────────────────────────┘
```

### Hexagonal Architecture

The backend follows hexagonal architecture (ports and adapters):

- **Domain**: Core business logic (independent of frameworks)
- **Ports**: Interfaces for inbound/outbound communication
- **Adapters**: Implementations of ports (REST API, JPA, etc.)

## Module Structure

```
backend/
├── api/                           # REST API layer
│   ├── src/main/kotlin/
│   │   └── com/globaloms/api/
│   │       ├── config/            # Spring configuration
│   │       ├── controller/        # REST controllers
│   │       ├── dto/               # Request/Response DTOs
│   │       └── exception/         # Global exception handling
│   └── build.gradle.kts
│
├── application/                   # Application services
│   ├── src/main/kotlin/
│   │   └── com/globaloms/application/
│   │       ├── order/             # Order use cases
│   │       ├── inventory/         # Inventory use cases
│   │       ├── channel/           # Channel use cases
│   │       └── ...
│   └── build.gradle.kts
│
├── domain/                        # Domain modules (bounded contexts)
│   ├── domain-order/              # Order context
│   ├── domain-inventory/          # Inventory context
│   ├── domain-channel/            # Channel context
│   ├── domain-claim/              # Claim context
│   ├── domain-settlement/         # Settlement context
│   ├── domain-automation/         # Automation context
│   ├── domain-strategy/           # Strategy context
│   ├── domain-catalog/            # Catalog context
│   └── domain-identity/           # Identity context
│
├── infrastructure/                # Infrastructure implementations
│   ├── infra-postgres/            # JPA repositories (PostgreSQL)
│   ├── infra-mongo/               # MongoDB repositories
│   └── infra-redis/               # Redis cache
│
└── core/
    └── core-domain/               # Shared domain
        ├── entity/                # Base entities
        ├── event/                 # Domain events
        └── common/                # Common utilities
```

## Bounded Contexts

### 1. Order Context (`domain-order`)

**Responsibility**: Manages the complete order lifecycle.

**Key Entities**:
- `Order`: Main order aggregate
- `OrderItem`: Items within an order
- `OrderStatus`: Order state enumeration

**Key Operations**:
- Create order
- Update order status
- Cancel order
- Split/merge orders

**Events**:
- `OrderCreatedEvent`
- `OrderConfirmedEvent`
- `OrderCancelledEvent`

### 2. Channel Context (`domain-channel`)

**Responsibility**: Multi-channel sales management.

**Key Entities**:
- `Channel`: Sales channel (marketplace, own store)
- `Warehouse`: Fulfillment center
- `ChannelPolicy`: Channel-specific rules

**Key Operations**:
- Register channel
- Manage warehouses
- Configure channel policies

**Events**:
- `ChannelRegisteredEvent`
- `WarehouseCreatedEvent`

### 3. Inventory Context (`domain-inventory`)

**Responsibility**: Stock management across warehouses.

**Key Entities**:
- `Stock`: Inventory item
- `StockReservation`: Reserved inventory
- `StockMovement`: Inventory history

**Key Operations**:
- Check availability
- Reserve stock
- Release reservation
- Update stock levels

**Events**:
- `StockReservedEvent`
- `StockReleasedEvent`
- `StockLevelChangedEvent`

### 4. Claim Context (`domain-claim`)

**Responsibility**: Returns, exchanges, and refunds.

**Key Entities**:
- `Claim`: Claim aggregate
- `ClaimType`: Return/Exchange/Refund
- `ClaimStatus`: Claim state

**Key Operations**:
- Create claim
- Process claim
- Approve/reject claim

**Events**:
- `ClaimCreatedEvent`
- `ClaimApprovedEvent`
- `RefundProcessedEvent`

### 5. Settlement Context (`domain-settlement`)

**Responsibility**: Financial settlement processing.

**Key Entities**:
- `Settlement`: Settlement aggregate
- `SettlementItem`: Individual settlement entry
- `FeePolicy`: Fee calculation rules

**Key Operations**:
- Calculate settlement
- Generate settlement report
- Process payment

**Events**:
- `SettlementCalculatedEvent`
- `PaymentProcessedEvent`

### 6. Automation Context (`domain-automation`)

**Responsibility**: Rule-based automation.

**Key Entities**:
- `AutomationRule`: Conditional rule
- `Action`: Automated action
- `Trigger`: Rule activation condition

**Key Operations**:
- Create automation rule
- Execute rule
- Enable/disable rule

### 7. Strategy Context (`domain-strategy`)

**Responsibility**: Operational strategies and readiness.

**Key Entities**:
- `AllocationStrategy`: Inventory distribution strategy
- `GlobalReadiness`: Operational readiness status

**Key Operations**:
- Define allocation strategy
- Check readiness status

### 8. Catalog Context (`domain-catalog`)

**Responsibility**: Product information management.

**Key Entities**:
- `Product`: Product aggregate
- `SKU`: Stock keeping unit
- `Category`: Product categorization

**Key Operations**:
- Create product
- Update product info
- Manage SKU

**Events**:
- `ProductCreatedEvent`
- `ProductUpdatedEvent`

### 9. Identity Context (`domain-identity`)

**Responsibility**: User and organization management.

**Key Entities**:
- `Company`: Organization/tenant
- `User`: User account
- `Role`: User role
- `Permission`: Access permission

**Key Operations**:
- Register company
- Create user
- Assign roles
- Check permissions

**Events**:
- `CompanyRegisteredEvent`
- `UserCreatedEvent`

## Development

### Prerequisites

- JDK 21+ (with Virtual Threads)
- Docker Desktop (for local infrastructure)
- IntelliJ IDEA (recommended) or any Kotlin IDE

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/smk692/order-management-system.git
   cd order-management-system/backend
   ```

2. **Start infrastructure**:
   ```bash
   # From project root
   docker compose -f docker-compose.dev.yml up -d
   ```

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

4. **Run the application**:
   ```bash
   ./gradlew :api:bootRun
   ```

5. **Access Swagger UI**:
   Open http://localhost:8080/swagger-ui/

### Gradle Tasks

```bash
# Build all modules
./gradlew build

# Run API server
./gradlew :api:bootRun

# Run tests
./gradlew test

# Run tests for specific module
./gradlew :domain:domain-order:test

# Code quality checks
./gradlew ktlintCheck detekt

# Format code
./gradlew ktlintFormat

# Generate test coverage report
./gradlew test jacocoTestReport

# Clean build artifacts
./gradlew clean
```

### IntelliJ IDEA Setup

1. **Import Project**: File → Open → Select `backend` directory
2. **Enable Kotlin**: IntelliJ should auto-detect Kotlin
3. **Configure JDK**: File → Project Structure → Project SDK → JDK 21
4. **Enable Virtual Threads**: Already configured in `application.yml`

## Database

### PostgreSQL

**Primary RDBMS** for transactional data.

**Schema Initialization**:
- Located in `docker/postgres/init/init.sql`
- Creates database and schema on container startup

**Connection Configuration**:
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/oms
    username: oms
    password: oms_password
```

**JPA Configuration**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway/Liquibase for migrations
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### MongoDB

**Document Store** for flexible schemas (events, logs, analytics).

**Connection Configuration**:
```yaml
# application.yml
spring:
  data:
    mongodb:
      uri: mongodb://oms:oms_password@localhost:27017/oms
      database: oms
```

**Usage Example**:
```kotlin
@Document(collection = "order_events")
data class OrderEventDocument(
    @Id val id: String? = null,
    val orderId: String,
    val eventType: String,
    val payload: Map<String, Any>,
    val timestamp: Instant
)
```

### Redis

**Cache Layer** for frequently accessed data.

**Connection Configuration**:
```yaml
# application.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

**Usage Example**:
```kotlin
@Cacheable(value = ["products"], key = "#productId")
fun getProduct(productId: String): Product {
    // Fetches from database if not in cache
}
```

### Database Migrations

**Recommended Tool**: Flyway or Liquibase (to be configured)

**Migration Structure**:
```
resources/db/migration/
├── V1__initial_schema.sql
├── V2__add_claim_tables.sql
└── V3__add_indexes.sql
```

## API Documentation

### Swagger/OpenAPI

- **Swagger UI**: http://localhost:8080/swagger-ui/
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### API Versioning

All APIs are versioned with `/api/v1` prefix.

### Example Endpoints

```bash
# Get all orders
GET /api/v1/orders

# Get specific order
GET /api/v1/orders/{orderId}

# Create order
POST /api/v1/orders

# Update order status
PATCH /api/v1/orders/{orderId}/status

# Cancel order
POST /api/v1/orders/{orderId}/cancel
```

### Request/Response Examples

**Create Order**:
```json
POST /api/v1/orders
{
  "customerId": "cust-123",
  "items": [
    {
      "productId": "prod-456",
      "quantity": 2,
      "price": 29.99
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "Seoul",
    "country": "KR"
  }
}
```

**Response**:
```json
{
  "orderId": "ord-789",
  "status": "CREATED",
  "totalAmount": 59.98,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

## Testing

### Unit Tests

**Framework**: JUnit 5, MockK

**Example**:
```kotlin
@Test
fun `should create order when inventory is sufficient`() {
    // Given
    val createOrderCommand = CreateOrderCommand(...)
    every { inventoryService.checkAvailability(...) } returns true

    // When
    val result = orderService.createOrder(createOrderCommand)

    // Then
    assertThat(result.status).isEqualTo(OrderStatus.CREATED)
    verify { inventoryService.reserveStock(...) }
}
```

### Integration Tests

**Framework**: Spring Boot Test, Testcontainers

**Example**:
```kotlin
@SpringBootTest
@Testcontainers
class OrderIntegrationTest {

    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:16")

    @Test
    fun `should persist order to database`() {
        // Test with real database via Testcontainers
    }
}
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :domain:domain-order:test

# With coverage
./gradlew test jacocoTestReport

# Open coverage report
open build/reports/jacoco/test/html/index.html
```

## Code Quality

### ktlint

**Kotlin Style Enforcement**

Configuration: `.editorconfig`

```bash
# Check style
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat
```

### detekt

**Static Code Analysis**

Configuration: `detekt.yml`

```bash
# Run detekt
./gradlew detekt

# View report
open build/reports/detekt/detekt.html
```

### Running All Checks

```bash
# Format, lint, analyze, and test
./gradlew ktlintFormat detekt test
```

## Deployment

### Docker Build

```bash
# Build backend Docker image
docker build -t global-oms-backend:latest -f docker/backend/Dockerfile .

# Run with Docker Compose
docker compose up -d
```

### Environment Variables

```bash
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=oms
POSTGRES_USER=oms
POSTGRES_PASSWORD=oms_password

# MongoDB
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DATABASE=oms
MONGO_USER=oms
MONGO_PASSWORD=oms_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Production Checklist

- [ ] Set `spring.profiles.active=prod`
- [ ] Enable HTTPS
- [ ] Configure proper database credentials
- [ ] Set up database backups
- [ ] Configure logging (centralized)
- [ ] Enable monitoring (Actuator endpoints)
- [ ] Set up APM (Application Performance Monitoring)
- [ ] Configure rate limiting
- [ ] Enable CORS properly
- [ ] Review security headers

## JDK 21 Virtual Threads

### Configuration

Virtual Threads are enabled in `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Benefits

- **Lightweight**: Millions of virtual threads vs. thousands of OS threads
- **Simplified Concurrency**: No need for reactive programming
- **Backward Compatible**: Works with existing blocking code

### Usage

No code changes needed. Spring Boot automatically uses virtual threads for:
- Request handling
- `@Async` methods
- `ExecutorService` beans

## Best Practices

### Domain Layer

- Keep domain logic in domain modules
- Use aggregates to enforce consistency
- Emit domain events for cross-context communication
- Use value objects for immutability

### Application Layer

- Application services orchestrate domain logic
- Handle transactions at the application layer
- Convert between domain models and DTOs

### Infrastructure Layer

- Repository implementations should be thin
- Don't leak infrastructure details to domain
- Use interfaces for external dependencies

### API Layer

- Validate input at the controller level
- Use DTOs for request/response
- Handle exceptions globally
- Document APIs with Swagger annotations

## Troubleshooting

### Common Issues

**Database Connection Failed**:
```bash
# Ensure infrastructure is running
docker compose -f docker-compose.dev.yml ps

# Check PostgreSQL
docker compose -f docker-compose.dev.yml logs postgres
```

**Build Failed**:
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

**Tests Failed**:
```bash
# Run tests with stack traces
./gradlew test --stacktrace
```

## Further Reading

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kotlin Official Docs](https://kotlinlang.org/docs/home.html)
- [JDK 21 Virtual Threads](https://openjdk.org/jeps/444)
- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
