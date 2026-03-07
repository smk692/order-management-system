# Architectural Review: Issue #4 Monorepo Strategic Implementation Plan

> **Reviewer**: Architect
> **Date**: 2026-03-07
> **Plan Version**: 4.0 (RALPLAN-DR Consensus Ready)
> **Review Type**: System Design and Technical Architecture Evaluation

---

## Executive Summary

The strategic plan demonstrates sound architectural judgment in preserving the existing DDD-based bounded contexts while adding critical infrastructure gaps. The decision to maintain Kotlin over Java migration is architecturally correct given the risk profile. However, several integration concerns and missing architectural considerations require attention before implementation.

**Architectural Assessment Score: 7.5/10**

---

## 1. Architectural Strengths

### 1.1 DDD Module Preservation (Excellent)

The decision to maintain 9 bounded contexts instead of consolidating to 4 is **architecturally correct**.

**Evidence**:
- `backend/settings.gradle.kts:7-16` defines distinct domain modules:
  - `domain-identity`, `domain-catalog`, `domain-channel`, `domain-order`
  - `domain-inventory`, `domain-claim`, `domain-settlement`
  - `domain-automation`, `domain-strategy`

Each module represents a genuine bounded context with:
- Clear aggregate roots (e.g., `Order.kt:31-353` with proper invariants)
- Domain events for cross-context communication (`OrderEvents.kt:1-81`)
- Repository interfaces within domain layer (`OrderRepository.kt`)

**Architectural Assessment**: The proposed mapping (order->domain-order+domain-claim, etc.) correctly recognizes that these are not redundant modules but meaningful business boundaries.

### 1.2 Hexagonal Architecture Compliance

The current structure follows hexagonal architecture principles:

```
backend/
├── core/                  # Domain core (no dependencies)
│   ├── core-domain/       # Value objects, base entities
│   └── core-infra/        # Infrastructure abstractions
├── domain/                # Business logic (depends only on core)
├── application/           # Use cases, orchestration
├── infrastructure/        # External adapters
│   ├── infra-mysql/       # JPA implementations
│   ├── infra-mongo/       # MongoDB implementations
│   └── infra-redis/       # Cache implementations
└── api/                   # HTTP adapters (controllers)
```

**Evidence**: `backend/build.gradle.kts:97-109` enforces dependency direction:
```kotlin
configure(subprojects.filter { it.path.startsWith(":domain:") }) {
    dependencies {
        "implementation"(project(":core:core-domain"))
    }
}
```

### 1.3 Event-Driven Foundation

Domain events are properly implemented:
- `DomainEvent.kt:9-23`: Abstract base with `aggregateId`, `aggregateType`, `eventId`, `occurredAt`
- `Order.kt:104,133-141,223-230`: Events registered on aggregate state changes

This foundation supports eventual consistency between bounded contexts.

### 1.4 Technology Stack Decisions

| Decision | Assessment |
|----------|------------|
| Keep Kotlin | **Correct** - 156 files, mature tooling (detekt/ktlint/Kover) |
| Keep React 19 | **Correct** - Backward compatible, better performance |
| Add Kover | **Correct** - JaCoCo-compatible, Kotlin-native |
| Add Flyway | **Critical** - Currently missing, high priority |

---

## 2. Architectural Concerns

### 2.1 CRITICAL: Missing Event Infrastructure

**Risk Level**: HIGH

The plan mentions domain events exist but does not address event **publication** or **consumption** infrastructure.

**Current State** (`Order.kt:104,334-344`):
```kotlin
@Transient
private val domainEvents: MutableList<DomainEvent> = mutableListOf()

// Events are accumulated but never published
fun clearEvents(): List<DomainEvent> { ... }
```

**Problem**: Events are collected but there is no:
1. Event publisher (Spring ApplicationEventPublisher integration)
2. Event store (for event sourcing or audit)
3. Cross-service communication (no Kafka/RabbitMQ)
4. Eventual consistency handling between bounded contexts

**Recommendation**: Add Phase 2.5 for event infrastructure:
- Spring's `@TransactionalEventListener` for synchronous domain events
- Consider Outbox pattern for reliability
- Document which events cross bounded context boundaries

### 2.2 HIGH: OpenAPI Contract Mismatch

**Risk Level**: HIGH

The plan proposes generating TypeScript clients from OpenAPI specs, but there is a **type mismatch** between backend and frontend.

**Backend Order Entity** (`Order.kt:31-91`):
```kotlin
class Order(
    val id: String,               // String (ORD-20260307-XXXXXXXX)
    val channelId: String,        // Reference to Channel
    val customer: Customer,       // Embedded value object
    val shippingAddress: Address, // Embedded value object
    val totalAmount: Money,       // Value object with amount + currency
    // ...
)
```

**Frontend Order Type** (`types.ts:78-94`):
```typescript
export interface Order {
  id: string;
  channel: string;              // Different name (channelId vs channel)
  customerName: string;         // Flattened (not Customer object)
  totalAmount: number;          // Primitive (not Money object)
  wmsNode?: string;             // Doesn't exist in backend
  items: Array<{...}>;          // Different structure
}
```

**Problem**: Direct OpenAPI generation will break existing frontend code unless DTOs match frontend expectations.

**Recommendation**:
1. Create API DTOs in `api` module that match frontend contract
2. Map domain entities -> DTOs in controllers
3. Version API contract (v1, v2) for breaking changes
4. Plan includes frontend type generation but not DTO design

### 2.3 MEDIUM: Application Layer Coupling

**Risk Level**: MEDIUM

**Evidence** (`application/build.gradle.kts:11-19`):
```kotlin
// Domain modules (implemented)
api(project(":domain:domain-identity"))
api(project(":domain:domain-catalog"))
api(project(":domain:domain-order"))
// ... all 9 domains
```

**Problem**: Application module depends on ALL domain modules with `api` scope, creating a monolithic application layer that:
1. Forces all domains to be deployed together
2. Prevents independent domain scaling
3. Creates compilation coupling

**Recommendation**:
1. Use use-case-specific application services
2. Consider application modules per bounded context group
3. Change `api` to `implementation` where cross-domain access is not needed

### 2.4 MEDIUM: Frontend Feature Isolation Missing

**Risk Level**: MEDIUM

**Evidence** (`eslint.config.js:31-42`):
```javascript
rules: {
  ...reactHooks.configs.recommended.rules,
  'react-refresh/only-export-components': ['warn', ...],
  // NO import restriction rules
}
```

**Current Feature Structure**:
```
frontend/src/features/
├── orders/
│   └── hooks/useOrderQueries.ts  # imports from @/shared/types
├── inventory/
├── products/
└── ...
```

**Problem**: No ESLint rules prevent cross-feature imports. Features could develop hidden dependencies.

**Plan Addresses This**: Phase 4.1 adds `no-restricted-imports` rule. **Verified in plan.**

### 2.5 LOW: Infrastructure Module Tight Coupling

**Risk Level**: LOW

**Evidence** (`infra-mysql/build.gradle.kts:11-19`):
```kotlin
implementation(project(":domain:domain-identity"))
implementation(project(":domain:domain-catalog"))
implementation(project(":domain:domain-order"))
// ... all domains
```

**Problem**: Single `infra-mysql` module implements repositories for ALL domains, preventing:
1. Independent database per bounded context (if needed)
2. Different persistence strategies per domain
3. Domain team autonomy

**Recommendation** (Future consideration):
- Split infrastructure modules per domain group when scaling
- Current structure is acceptable for MVP

---

## 3. Recommendations by Priority

### Priority 1: Critical (Must Do)

| # | Recommendation | Effort | Impact | Reference |
|---|----------------|--------|--------|-----------|
| 1.1 | Add event publication infrastructure | 2 days | HIGH | `Order.kt:104` - events collected but not published |
| 1.2 | Design API DTOs matching frontend contract | 1 day | HIGH | `types.ts:78-94` vs `Order.kt:31-91` type mismatch |
| 1.3 | Add Flyway baseline migration | 1 day | HIGH | Plan Phase 1.2, currently missing |

### Priority 2: High (Should Do)

| # | Recommendation | Effort | Impact | Reference |
|---|----------------|--------|--------|-----------|
| 2.1 | Implement `@TransactionalEventListener` | 1 day | MEDIUM | Enable intra-process event handling |
| 2.2 | Add OpenAPI spec validation in CI | 0.5 day | MEDIUM | Prevent spec drift |
| 2.3 | Document bounded context interactions | 0.5 day | MEDIUM | Event flow diagram needed |

### Priority 3: Medium (Nice to Have)

| # | Recommendation | Effort | Impact | Reference |
|---|----------------|--------|--------|-----------|
| 3.1 | Refactor application module dependencies | 2 days | LOW | Change `api` to `implementation` |
| 3.2 | Add Testcontainers properly | 1 day | MEDIUM | Plan mentions but needs config |
| 3.3 | Consider domain-specific infra modules | Future | LOW | Currently acceptable |

---

## 4. API Contract Management Deep Dive

### 4.1 Current OpenAPI Configuration

**Evidence** (`api/build.gradle.kts:29-30`):
```kotlin
// OpenAPI / Swagger
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
```

SpringDoc is present but plan lacks:
1. **Schema customization**: How to handle `Money` as `{amount: number, currency: string}`
2. **Nullable handling**: Kotlin nullability -> OpenAPI nullable
3. **Enum serialization**: `OrderStatus` enum string mapping

### 4.2 Proposed Pipeline Gaps

```
Plan's Pipeline:
Backend -> SpringDoc -> openapi.yaml -> openapi-generator -> TypeScript

Missing Steps:
1. DTO layer design (domain != API contract)
2. Schema evolution strategy (versioning)
3. Breaking change detection
4. Consumer-driven contract testing
```

### 4.3 Recommendation: Add DTO Design Phase

Insert **Phase 1.5** before OpenAPI pipeline:

```kotlin
// Example: OrderDto.kt in api module
data class OrderResponse(
    val id: String,
    val channel: String,        // Maps from channelId
    val customerName: String,   // Flattened from Customer
    val totalAmount: BigDecimal,// Extracted from Money
    val currency: String,       // Extracted from Money
    val items: List<OrderItemResponse>
)
```

---

## 5. Database Migration Strategy Assessment

### 5.1 Current State

**Evidence** (`infra-mysql/build.gradle.kts:28-30`):
```kotlin
// Flyway
implementation("org.flywaydb:flyway-core")
implementation("org.flywaydb:flyway-database-postgresql")
```

Flyway dependency exists but:
- No migration files found in `db/migration/`
- No baseline script
- No Flyway configuration in `application.yml`

### 5.2 Plan's Approach

Phase 1.2: Add `V1__initial_schema.sql`

**Concern**: Initial schema must match existing JPA entities exactly, or Hibernate will fail validation.

### 5.3 Recommendation

1. Generate baseline from JPA entities:
   ```bash
   ./gradlew generateJpaSchema  # Custom task needed
   ```
2. Use Flyway baseline-on-migrate for existing databases
3. Add validation: `spring.jpa.hibernate.ddl-auto=validate`

---

## 6. Alternative Architectural Approaches

### 6.1 Alternative A: Modular Monolith with Module Facades

Instead of the current flat dependency structure, introduce **module facades**:

```
application/
├── order-application/     # Order + Claim use cases
├── inventory-application/ # Inventory + Channel + Automation use cases
├── payment-application/   # Settlement use cases
└── shared-application/    # Cross-cutting concerns
```

**Pros**:
- Better alignment with issue's 4-module structure
- Clear API boundaries per business capability
- Easier future extraction to microservices

**Cons**:
- Requires restructuring existing application module
- Additional coordination overhead

### 6.2 Alternative B: Keep Current + Add Anti-Corruption Layer

Maintain current structure but add ACL between domain groups:

```kotlin
// In domain-order
interface InventoryService {
    fun checkAvailability(productId: String, quantity: Int): Boolean
}

// Implemented in application layer, calls domain-inventory
```

**Pros**:
- Minimal structural changes
- Explicit cross-domain contracts

**Cons**:
- Additional abstraction layer
- Potential duplication

### 6.3 Recommended Approach

**Keep current structure (Plan's Option C)** with enhancements:
1. Add documented bounded context map
2. Implement domain events for cross-context communication
3. Defer module restructuring to Phase 2 (post-MVP)

---

## 7. Integration Risk Assessment

| Integration Point | Risk | Mitigation |
|-------------------|------|------------|
| OpenAPI -> TypeScript | HIGH | Add DTO layer, contract tests |
| Flyway migrations | MEDIUM | Generate from entities, baseline existing |
| Frontend isolation | LOW | Plan addresses with ESLint rules |
| Event infrastructure | HIGH | Add event publisher before Phase 2 |
| Testcontainers | LOW | Standard setup, well-documented |

---

## 8. Consensus Addendum

### Antithesis (Steelman)

**Strongest argument AGAINST the plan's Kotlin decision:**

The issue explicitly specifies Java 21. While Kotlin compiles to JVM bytecode, this represents a **deliberate deviation** from documented requirements. If the project has:
1. Java-only team members joining later
2. Organizational standards mandating Java
3. Tooling that expects Java source (certain APM agents, security scanners)

Then the "equivalent" argument breaks down. Kotlin's null-safety and conciseness don't help if the team can't maintain the code.

### Tradeoff Tension

**Event infrastructure vs. delivery timeline:**

Adding event publication infrastructure (Recommendation 1.1) adds 2 days to Phase 1-2. However:
- Without it, domain events are dead code
- Cross-bounded-context operations will use direct calls, creating hidden coupling
- Technical debt accumulates silently

This tension between **shipping fast** and **architectural integrity** cannot be fully resolved. The plan should explicitly acknowledge this tradeoff.

### Synthesis

Include event infrastructure as **optional Phase 1.5**:
- If time permits, implement Spring ApplicationEventPublisher integration
- If not, document event infrastructure as **required before Phase 3 (adding tests)**
- Tests should verify event publication, which requires infrastructure

---

## 9. File References

| File | Line | Relevance |
|------|------|-----------|
| `backend/build.gradle.kts` | 97-109 | Domain module dependency enforcement |
| `backend/settings.gradle.kts` | 7-16 | 9 bounded context modules |
| `backend/domain/domain-order/src/main/kotlin/com/oms/order/domain/Order.kt` | 31-353 | Order aggregate root implementation |
| `backend/domain/domain-order/src/main/kotlin/com/oms/order/domain/event/OrderEvents.kt` | 1-81 | Domain event definitions |
| `backend/core/core-domain/src/main/kotlin/com/oms/core/event/DomainEvent.kt` | 9-44 | Event base class and AggregateRoot |
| `backend/application/build.gradle.kts` | 11-19 | Application layer coupling evidence |
| `backend/api/build.gradle.kts` | 29-30 | OpenAPI dependency |
| `backend/infrastructure/infra-mysql/build.gradle.kts` | 11-30 | Infrastructure coupling + Flyway |
| `frontend/eslint.config.js` | 31-42 | Missing import restrictions |
| `frontend/src/shared/types/types.ts` | 78-94 | Frontend Order type |
| `frontend/src/features/orders/hooks/useOrderQueries.ts` | 1-99 | Frontend API integration |
| `docker-compose.yml` | 1-126 | Infrastructure setup |

---

## 10. Final Assessment

### Approval Status: **CONDITIONAL APPROVAL**

The plan is architecturally sound with the following conditions:

1. **MUST**: Add event publication infrastructure (or document as Phase 1.5)
2. **MUST**: Design API DTOs before OpenAPI codegen
3. **SHOULD**: Add Flyway baseline generation from JPA entities
4. **SHOULD**: Document bounded context interaction map

### Architectural Integrity: 7.5/10

| Dimension | Score | Notes |
|-----------|-------|-------|
| DDD Compliance | 9/10 | Excellent bounded contexts |
| Hexagonal Architecture | 8/10 | Good separation, minor coupling |
| Event-Driven Readiness | 5/10 | Events defined, no infrastructure |
| API Contract Design | 6/10 | OpenAPI present, DTO layer missing |
| CI/CD Design | 8/10 | Path filtering, coverage gaps addressed |
| Frontend Isolation | 7/10 | Plan addresses, not yet implemented |

---

**Signed**: Architect
**Date**: 2026-03-07
