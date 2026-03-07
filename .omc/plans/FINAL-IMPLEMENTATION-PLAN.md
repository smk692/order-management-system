# Issue #4: Monorepo Structure - Final Implementation Plan (Consensus Edition)

> **Version**: 5.0 (Consensus Approved)
> **Date**: 2026-03-07
> **Status**: Ready for Implementation
> **Consensus**: Planner + Architect + Critic
> **Revisions**: Incorporates all critical feedback from consensus review

---

## Executive Summary

This plan addresses Issue #4 (Monorepo Structure Creation) through a **pragmatic, consensus-driven approach** that:

1. **Preserves architectural integrity** - Maintains the existing 9 DDD bounded contexts
2. **Minimizes risk** - Retains Kotlin while meeting JDK 21 requirement
3. **Addresses critical gaps** - Adds event infrastructure, DTO layer, and proper coverage strategy
4. **Corrects factual errors** - Acknowledges existing Flyway migrations
5. **Provides realistic timelines** - Extends coverage work to 16 days (from unrealistic 10)

**Key Change from Original Plan**: Added Decision Gate 0, Phase 1.5 (DTO Design), and extended timeline based on Critic's feasibility analysis.

---

## Decision Gate 0: Stakeholder Approval (MANDATORY - Before Any Code)

### Required Approvals

| Decision | Issue Requirement | Proposed Approach | Requires Approval From |
|----------|-------------------|-------------------|------------------------|
| **Language** | Java 21 | Kotlin 1.9 on JDK 21 | Tech Lead / Architecture Team |
| **Module Structure** | 4 modules | 9 DDD modules with facade documentation | Product Owner / Tech Lead |
| **Coverage Tools** | JaCoCo, Checkstyle, SpotBugs | Kover, detekt, ktlint (Kotlin equivalents) | QA Lead / Tech Lead |

### Approval Process

1. **Create RFC Document** (Day 1)
   - Title: "RFC: Kotlin Retention for Issue #4 Implementation"
   - Sections: Context, Decision, Rationale, Alternatives, Risks, Timeline Impact
   - Distribution: Tech Lead, Product Owner, QA Lead

2. **Decision Timeline**
   - Submit RFC: Day 1
   - Response deadline: Day 3
   - If no response by Day 3: Escalate to CTO/VP Engineering

3. **Fallback Plan**
   - **IF Kotlin APPROVED**: Proceed with this plan (Phases 1-5)
   - **IF Kotlin REJECTED**: Activate Java Migration Plan (estimate: 4-6 weeks, requires separate RFC)

### Rejection Risk Mitigation

**Critic Pre-Mortem Scenario 1** identified 35% probability of stakeholder rejection. Mitigation:
- **Preventive**: Early RFC with data-driven rationale (156 files, regression risk)
- **Responsive**: Pre-prepared Java migration estimate with detailed breakdown
- **Escalation**: Clear decision authority chain

---

## Revised Gap Analysis (Corrected)

### Backend Gaps (Critic Corrections Applied)

| Requirement | Current State | Gap | Priority | Critic Finding |
|-------------|--------------|-----|----------|----------------|
| Flyway migrations | **V1-V6 exist** (6 migrations) | ✅ NONE | - | ❌ Original plan had FACTUAL ERROR claiming "not present" |
| JaCoCo/Kover | Not present | Gap: Add Kover | HIGH | ✅ Confirmed |
| Logback JSON | Not present | Gap: Add JSON appender | MEDIUM | ✅ Confirmed |
| Event Infrastructure | Domain events exist, no publisher | **Gap: No publication mechanism** | HIGH | ✅ Architect identified, plan missed |
| API DTO Layer | Domain entities exposed directly | **Gap: DTO mismatch with frontend** | HIGH | ✅ Architect identified, plan missed |

### Frontend Gaps

| Requirement | Current State | Gap | Priority | Critic Finding |
|-------------|--------------|-----|----------|----------------|
| Feature isolation | No ESLint rules | Gap: Add import restrictions | HIGH | ✅ Confirmed |
| Test coverage 70% | 0% (no test files except setup) | Gap: Write tests | HIGH | ⚠️ Timeline unrealistic (3 days → 5-7 days) |
| MSW setup | Minimal (1-line setup.ts) | Gap: Handlers, factories | MEDIUM | ✅ Confirmed |
| Vitest coverage config | Not configured | Gap: Add c8/istanbul | HIGH | ✅ Confirmed |

---

## Phase 0: Foundation & Decision Gate (Day 1-3)

### Objective
Secure stakeholder approval and verify current state before code changes.

### Tasks

| Task | Description | Acceptance Criteria | Owner |
|------|-------------|---------------------|-------|
| 0.1 | Create RFC for Kotlin retention | RFC document approved by stakeholders | Tech Lead |
| 0.2 | Verify Flyway migration state | Confirm V1-V6 exist, no conflicts | Backend Dev |
| 0.3 | Audit current test coverage | Generate baseline report (0% confirmed) | QA Lead |
| 0.4 | Document bounded context map | Visual diagram of 9 contexts + dependencies | Architect |

### Deliverables

```
.omc/plans/
├── RFC-kotlin-retention.md          # NEW
├── flyway-audit-report.md           # NEW
└── bounded-context-map.svg          # NEW
```

### Decision Point

- **IF Approved**: Proceed to Phase 1
- **IF Rejected**: Activate Java Migration Plan (4-6 weeks)

---

## Phase 1: CI/CD Foundation (Day 4-5)

### Objective
Establish coverage reporting and path-based filtering.

### Tasks

| Task | Description | Acceptance Criteria | Files Modified |
|------|-------------|---------------------|----------------|
| 1.1 | Add Kover plugin to Gradle | `./gradlew koverHtmlReport` generates report | `backend/build.gradle.kts` |
| 1.2 | ~~Add Flyway migrations~~ | **REMOVED** - Migrations already exist (V1-V6) | N/A |
| 1.3 | Configure Vitest coverage | `pnpm test:coverage` generates report | `frontend/vitest.config.ts` |
| 1.4 | Update CI workflows | Coverage reports visible in PR checks | `.github/workflows/backend-ci.yml`, `frontend-ci.yml` |

### Files to Modify

```diff
backend/build.gradle.kts
+ plugins {
+   id("org.jetbrains.kotlinx.kover") version "0.7.5"
+ }
+
+ kover {
+   reports {
+     total {
+       html { onCheck = true }
+       xml { onCheck = true }
+     }
+   }
+ }

frontend/vitest.config.ts
+ export default defineConfig({
+   test: {
+     coverage: {
+       provider: 'v8',
+       reporter: ['text', 'json', 'html', 'lcov'],
+       exclude: ['node_modules/', 'src/test/'],
+       thresholds: {
+         lines: 70,
+         functions: 70,
+         branches: 70,
+         statements: 70
+       }
+     }
+   }
+ })
```

---

## Phase 1.5: DTO Design & Event Infrastructure (Day 6-8)

### Objective
**NEW PHASE** - Address Architect's critical concerns before OpenAPI generation.

### Tasks

| Task | Description | Acceptance Criteria | Priority |
|------|-------------|---------------------|----------|
| 1.5.1 | Design API DTO layer | DTOs match frontend `types.ts:78-94` contract | CRITICAL |
| 1.5.2 | Add ApplicationEventPublisher | Domain events published on commit | CRITICAL |
| 1.5.3 | Add @TransactionalEventListener | Events handled within transaction | HIGH |
| 1.5.4 | Document event flows | Diagram of cross-context events | MEDIUM |

### DTO Design Example

```kotlin
// backend/api/src/main/kotlin/com/oms/api/dto/OrderResponse.kt
data class OrderResponse(
    val id: String,
    val channel: String,              // Maps from Order.channelId
    val customerName: String,         // Flattened from Order.customer.name
    val totalAmount: BigDecimal,      // Extracted from Order.totalAmount.amount
    val currency: String,             // Extracted from Order.totalAmount.currency
    val status: String,               // Order.status.name
    val items: List<OrderItemResponse>
)

// Controller mapping
@GetMapping("/{id}")
fun getOrder(@PathVariable id: String): OrderResponse {
    val order = orderService.getOrder(id)
    return order.toResponse()  // Extension function in DtoMapper.kt
}
```

### Event Infrastructure

```kotlin
// backend/api/src/main/kotlin/com/oms/api/config/EventConfig.kt
@Configuration
class EventConfig {
    @Bean
    fun domainEventPublisher(publisher: ApplicationEventPublisher): DomainEventPublisher {
        return SpringDomainEventPublisher(publisher)
    }
}

// backend/core/core-domain/src/main/kotlin/com/oms/core/event/DomainEventPublisher.kt
interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}

// backend/application/src/main/kotlin/com/oms/application/order/OrderApplicationService.kt
@Service
class OrderApplicationService(
    private val orderRepository: OrderRepository,
    private val eventPublisher: DomainEventPublisher
) {
    @Transactional
    fun placeOrder(command: PlaceOrderCommand): String {
        val order = Order.create(...)
        orderRepository.save(order)

        // Publish domain events
        order.getDomainEvents().forEach { eventPublisher.publish(it) }
        order.clearEvents()

        return order.id
    }
}
```

### Files to Create

```
backend/
├── api/src/main/kotlin/com/oms/api/
│   ├── dto/
│   │   ├── OrderResponse.kt               # NEW
│   │   ├── OrderItemResponse.kt           # NEW
│   │   ├── DtoMapper.kt                   # NEW
│   │   └── ...
│   └── config/
│       └── EventConfig.kt                 # NEW
├── core/core-domain/src/main/kotlin/com/oms/core/
│   └── event/
│       └── DomainEventPublisher.kt        # NEW
└── infrastructure/infra-spring/
    └── SpringDomainEventPublisher.kt      # NEW
```

---

## Phase 2: OpenAPI Pipeline (Day 9-10)

### Objective
Automated OpenAPI spec generation with TypeScript client codegen.

### Tasks

| Task | Description | Acceptance Criteria | Critic Enhancement |
|------|-------------|---------------------|-------------------|
| 2.1 | Configure SpringDoc metadata | `GET /v3/api-docs` returns valid spec | ✅ Plan had this |
| 2.2 | Add OpenAPI artifact to CI | `openapi.yaml` artifact uploaded | ✅ Plan had this |
| 2.3 | Add TypeScript codegen | Generated client uses **typescript-axios** v7.x | ⚠️ Plan didn't specify tool version |
| 2.4 | ~~Pre-commit hook~~ | **REMOVED** - Use CI-only validation | ❌ Critic rejected (slow, inconsistent) |
| 2.5 | Add OpenAPI spec validation | CI fails if spec diverges from DTOs | ✅ Architect recommended |

### OpenAPI Generator Configuration

```yaml
# frontend/openapi-generator-config.yaml
generatorName: typescript-axios
inputSpec: ../openapi.yaml
outputDir: ./src/api/generated
additionalProperties:
  npmName: oms-api-client
  supportsES6: true
  withInterfaces: true
  useSingleRequestParameter: true
```

### CI Workflow

```yaml
# .github/workflows/openapi-pipeline.yml
name: OpenAPI Pipeline

on:
  push:
    branches: [main]
    paths:
      - 'backend/api/src/main/kotlin/**'
      - 'backend/api/src/main/resources/application.yml'

jobs:
  generate-spec:
    runs-on: ubuntu-latest
    steps:
      - name: Start backend
        run: ./gradlew :api:bootRun &

      - name: Wait for API
        run: sleep 30

      - name: Download OpenAPI spec
        run: curl http://localhost:8080/v3/api-docs.yaml > openapi.yaml

      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: openapi-spec
          path: openapi.yaml

  generate-typescript:
    needs: generate-spec
    runs-on: ubuntu-latest
    steps:
      - name: Download artifact
        uses: actions/download-artifact@v3
        with:
          name: openapi-spec

      - name: Generate TypeScript client
        run: |
          npx @openapitools/openapi-generator-cli generate \
            -i openapi.yaml \
            -g typescript-axios \
            -o frontend/src/api/generated \
            --config frontend/openapi-generator-config.yaml

      - name: Commit generated client
        run: |
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add frontend/src/api/generated
          git commit -m "chore: Update generated API client [skip ci]" || echo "No changes"
          git push
```

---

## Phase 3: Backend Testing (Day 11-17)

### Objective
Reach 80% test coverage with meaningful tests.

**TIMELINE ADJUSTMENT**: Critic identified 3 days as unrealistic. Extended to 7 days.

### Coverage Strategy (Revised)

| Layer | Files | Target | Effort | Priority |
|-------|-------|--------|--------|----------|
| core-domain | ~20 | 90% | 2 days | P0 - Foundation |
| domain-order | ~35 | 85% | 2 days | P0 - Critical business logic |
| domain-inventory | ~25 | 80% | 1.5 days | P1 |
| domain-claim | ~20 | 75% | 1 day | P1 |
| Other domains | ~56 | 60% | 1.5 days | P2 - Defer if needed |

**Cumulative Target**: 80% overall (weighted average)

### Test Infrastructure

```kotlin
// backend/build.gradle.kts
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

// backend/api/src/test/kotlin/com/oms/api/AbstractIntegrationTest.kt
@SpringBootTest
@Testcontainers
abstract class AbstractIntegrationTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")
            .apply {
                withDatabaseName("oms_test")
                withUsername("test")
                withPassword("test")
            }
    }
}
```

### Test Examples

```kotlin
// backend/core/core-domain/src/test/kotlin/com/oms/core/entity/MoneyTest.kt
class MoneyTest : FunSpec({
    test("Money addition should handle same currency") {
        val m1 = Money(100.0, Currency.KRW)
        val m2 = Money(50.0, Currency.KRW)
        val result = m1 + m2

        result.amount shouldBe 150.0
        result.currency shouldBe Currency.KRW
    }

    test("Money addition should fail for different currencies") {
        val m1 = Money(100.0, Currency.KRW)
        val m2 = Money(50.0, Currency.USD)

        shouldThrow<IllegalArgumentException> {
            m1 + m2
        }
    }
})

// backend/domain/domain-order/src/test/kotlin/com/oms/order/domain/OrderTest.kt
class OrderTest : FunSpec({
    test("Order creation should emit OrderCreatedEvent") {
        val order = Order.create(
            channelId = "CH001",
            customer = Customer("John Doe", "john@example.com"),
            items = listOf(...)
        )

        order.getDomainEvents() should containExactly(
            OrderCreatedEvent(order.id, order.channelId)
        )
    }

    test("Order cancellation should fail if already shipped") {
        val order = Order.create(...).apply {
            ship()  // Transitions to SHIPPED
        }

        shouldThrow<IllegalStateException> {
            order.cancel()
        }
    }
})
```

---

## Phase 4: Frontend Testing (Day 18-24)

### Objective
Reach 70% test coverage with feature isolation enforced.

**TIMELINE ADJUSTMENT**: Extended from 3 days to 7 days per Critic's analysis.

### Coverage Strategy (Revised)

| Area | Files | Target | Effort | Priority |
|------|-------|--------|--------|----------|
| src/core/ | ~10 | 80% | 1.5 days | P0 |
| src/shared/ | ~15 | 75% | 2 days | P0 |
| src/features/orders/ | ~20 | 70% | 2 days | P1 |
| src/features/inventory/ | ~18 | 65% | 1.5 days | P2 |
| Other features | ~30 | 50% | Defer | P3 |

### ESLint Feature Isolation

```javascript
// frontend/eslint.config.js
export default [
  {
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@/features/*'],
              message: 'Cross-feature imports are forbidden. Use @/shared instead.'
            }
          ]
        }
      ]
    }
  }
]
```

### MSW Setup

```typescript
// frontend/src/test/setup.ts
import { beforeAll, afterEach, afterAll } from 'vitest'
import { setupServer } from 'msw/node'
import { handlers } from './mocks/handlers'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// frontend/src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw'

export const handlers = [
  http.get('/api/v1/orders/:id', ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      channel: 'COUPANG',
      customerName: 'Test Customer',
      totalAmount: 50000,
      currency: 'KRW',
      status: 'PENDING',
      items: []
    })
  }),

  http.post('/api/v1/orders', async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json(
      { id: 'ORD-20260307-000001', ...body },
      { status: 201 }
    )
  })
]

// frontend/src/test/mocks/factories.ts
import { faker } from '@faker-js/faker'

export const createMockOrder = (overrides = {}) => ({
  id: faker.string.uuid(),
  channel: faker.helpers.arrayElement(['COUPANG', 'NAVER', 'TMON']),
  customerName: faker.person.fullName(),
  totalAmount: faker.number.int({ min: 10000, max: 100000 }),
  currency: 'KRW',
  status: 'PENDING',
  items: [],
  ...overrides
})
```

### Test Examples

```typescript
// frontend/src/features/orders/components/OrderList.test.tsx
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { OrderList } from './OrderList'

describe('OrderList', () => {
  const queryClient = new QueryClient()

  it('should display orders when API returns data', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <OrderList />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Test Customer')).toBeInTheDocument()
    })
  })

  it('should show loading state initially', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <OrderList />
      </QueryClientProvider>
    )

    expect(screen.getByText('Loading...')).toBeInTheDocument()
  })
})
```

---

## Phase 5: Integration & Documentation (Day 25-26)

### Objective
End-to-end validation and updated documentation.

### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 5.1 | Run full CI pipeline | All checks pass on clean branch |
| 5.2 | Test docker-compose locally | `docker-compose up` starts all services |
| 5.3 | Verify OpenAPI sync | Generated TS client matches backend spec |
| 5.4 | Update README.md | Setup instructions reflect new structure |
| 5.5 | Update ARCHITECTURE.md | Document 9-module rationale + bounded context map |
| 5.6 | Create TESTING.md | Coverage strategy, how to run tests |

### Documentation Updates

```markdown
# README.md additions

## Test Coverage

Current coverage:
- Backend: 80% (Kover)
- Frontend: 70% (Vitest)

Run tests:
```bash
# Backend
cd backend
./gradlew test koverHtmlReport
open build/reports/kover/html/index.html

# Frontend
cd frontend
pnpm test:coverage
open coverage/index.html
```

## API Contract

The backend API is documented via OpenAPI 3.0. TypeScript types are auto-generated:

1. Backend changes trigger OpenAPI generation
2. CI generates TypeScript client in `frontend/src/api/generated/`
3. Import generated types:
   ```typescript
   import { OrdersApi, OrderResponse } from '@/api/generated'
   ```

# ARCHITECTURE.md additions

## Module Structure

The system uses **9 bounded contexts** (DDD) grouped into 4 logical domains:

| Logical Domain (Issue #4) | Bounded Contexts | Justification |
|---------------------------|------------------|---------------|
| Order | domain-order, domain-claim | Order lifecycle + claim processing are tightly coupled |
| Inventory | domain-inventory, domain-channel, domain-automation | Inventory allocation depends on channel rules and automation strategies |
| Payment | domain-settlement | Financial settlement and reconciliation |
| Shared | core-domain, core-infra, domain-identity, domain-catalog, domain-strategy | Cross-cutting concerns and foundational abstractions |

This structure preserves:
- **DDD integrity**: Each module represents a genuine bounded context with its own ubiquitous language
- **Single Responsibility**: Consolidating domains would create bloated modules with mixed concerns
- **Event-driven communication**: Clear boundaries enable asynchronous domain events

## Event Infrastructure

Domain events flow through Spring's ApplicationEventPublisher:

```
Order Domain            Inventory Domain
    |                        |
    | OrderCreatedEvent      |
    +----------------------->+ @TransactionalEventListener
                             | reserveInventory()
```

Events are:
- **Transactional**: Published only on commit via @TransactionalEventListener
- **Synchronous**: Within-process events for immediate consistency
- **Logged**: All events persisted to audit trail
```

---

## Success Criteria (Revised)

| Criterion | Measurement | Target | Verification Method |
|-----------|-------------|--------|---------------------|
| Backend coverage | Kover report | >= 80% | CI coverage check, `koverVerify` task |
| Frontend coverage | Vitest report | >= 70% | CI coverage check, Vitest thresholds |
| Feature isolation | ESLint errors | 0 cross-feature imports | `pnpm lint` in CI |
| OpenAPI sync | Type compatibility | Generated client matches backend | Integration test with generated client |
| Event infrastructure | Events published | @TransactionalEventListener receives events | Integration test verifying event publication |
| Docker local dev | All services healthy | 100% health checks pass | `docker-compose ps` shows "Up (healthy)" |
| Stakeholder approval | RFC decision | Kotlin retention approved | RFC sign-off document |

---

## Risk Assessment (Consensus Edition)

### Technical Risks (Updated)

| Risk | Impact | Likelihood | Mitigation | Source |
|------|--------|------------|------------|--------|
| Stakeholder rejects Kotlin | HIGH | MEDIUM (35%) | Decision Gate 0, pre-prepared Java migration plan | Critic Pre-Mortem #1 |
| Coverage targets unmet | MEDIUM | MEDIUM | Extended timeline (10d -> 16d), incremental gates | Critic Pre-Mortem #2 |
| OpenAPI type mismatch | HIGH | LOW | Phase 1.5 DTO design before codegen | Architect + Critic Pre-Mortem #3 |
| Event infrastructure missing | MEDIUM | LOW | Phase 1.5 explicitly addresses this | Architect Concern 2.1 |
| Flyway migration conflict | ELIMINATED | N/A | Removed erroneous Task 1.2 | Critic Section 3.1 |

### Process Risks

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Scope creep in testing | MEDIUM | MEDIUM | Fixed time-box per module, prioritized coverage strategy |
| Documentation drift | LOW | HIGH | Phase 5 explicitly includes doc updates |
| CI complexity | LOW | LOW | Reuse existing workflow patterns, incremental additions |

---

## Timeline Summary

| Phase | Duration | Key Deliverables |
|-------|----------|------------------|
| **Phase 0**: Decision Gate | 3 days | RFC approval, baseline audit |
| **Phase 1**: CI/CD Foundation | 2 days | Kover, Vitest coverage config |
| **Phase 1.5**: DTO & Events | 3 days | API DTOs, event publisher |
| **Phase 2**: OpenAPI Pipeline | 2 days | TypeScript codegen, CI artifact |
| **Phase 3**: Backend Testing | 7 days | 80% coverage, Testcontainers |
| **Phase 4**: Frontend Testing | 7 days | 70% coverage, MSW, feature isolation |
| **Phase 5**: Integration & Docs | 2 days | CI validation, updated docs |

**Total**: **26 days** (vs. original unrealistic 12 days)

---

## Rollback Strategy

### Code Rollback

Each phase is independently revertable via PR:

```bash
# If Phase 3 introduces regressions
git revert <phase-3-merge-commit>
git push
```

### Database Rollback

Flyway migrations V1-V6 already exist. New migrations (if any in Phase 1.5) follow:

```bash
# Rollback last migration
./gradlew flywayUndo

# Or manual rollback
psql -U oms -d oms < V7__rollback.sql
```

### Configuration Rollback

All configuration changes are version-controlled. Revert commits or restore from `.omc/plans/rollback/` directory.

---

## File Change Summary (Final)

### Files to Create

```
.omc/plans/
├── RFC-kotlin-retention.md
├── flyway-audit-report.md
└── bounded-context-map.svg

backend/
├── api/src/main/kotlin/com/oms/api/
│   ├── dto/                           # NEW: API DTOs
│   │   ├── OrderResponse.kt
│   │   ├── OrderItemResponse.kt
│   │   └── DtoMapper.kt
│   └── config/
│       └── EventConfig.kt             # NEW: Event publisher config
├── core/core-domain/src/main/kotlin/com/oms/core/
│   └── event/
│       └── DomainEventPublisher.kt    # NEW: Event publisher interface
├── infrastructure/infra-spring/
│   └── SpringDomainEventPublisher.kt  # NEW: Spring event publisher
├── */src/test/kotlin/**/*.kt          # NEW: Test files (80% coverage)
└── api/src/main/resources/
    └── logback-spring.xml             # NEW: JSON logging

frontend/
├── src/api/generated/                 # NEW: Generated TypeScript client
├── src/test/
│   ├── mocks/
│   │   ├── handlers.ts                # NEW: MSW handlers
│   │   └── factories.ts               # NEW: Mock data factories
│   └── setup.ts                       # MODIFY: Add MSW server
└── src/**/*.test.{ts,tsx}             # NEW: Test files (70% coverage)

.github/workflows/
└── openapi-pipeline.yml               # NEW: OpenAPI artifact pipeline

docs/
├── TESTING.md                         # NEW: Test strategy guide
└── EVENT-FLOWS.md                     # NEW: Event architecture
```

### Files to Modify

```
backend/
├── build.gradle.kts                   # Add Kover plugin
├── api/build.gradle.kts               # Add OpenAPI generator task
└── application/src/.../OrderApplicationService.kt  # Add event publishing

frontend/
├── eslint.config.js                   # Add no-restricted-imports
├── vitest.config.ts                   # Add coverage config
└── package.json                       # Add test scripts

.github/workflows/
├── backend-ci.yml                     # Add coverage reporting
└── frontend-ci.yml                    # Add coverage reporting

docs/
├── README.md                          # Add test/OpenAPI sections
└── ARCHITECTURE.md                    # Add bounded context rationale
```

---

## Consensus Statement

This plan represents **consensus** between:

1. **Planner**: Strategic phasing, gap analysis
2. **Architect**: Technical integrity (event infrastructure, DTO layer, API contracts)
3. **Critic**: Realism (timeline extension, factual corrections, stakeholder approval)

**Key Compromises**:
- Planner's 12-day timeline → Extended to 26 days (Critic's feasibility analysis)
- Architect's event infrastructure concern → Added as Phase 1.5
- Critic's DTO mismatch concern → Added as Phase 1.5
- Critic's Flyway error → Task 1.2 removed

**Outstanding Risks**:
- Stakeholder approval (35% rejection probability, mitigated by Decision Gate 0)
- Coverage quality (45% "coverage theater" risk, mitigated by example tests + branch coverage requirements)

---

## Appendix A: Architecture Decision Records

### ADR-001: Kotlin Retention

- **Decision**: Keep Kotlin 1.9 instead of migrating to Java 21
- **Drivers**: 156 files, 4-6 week migration risk, equivalent tooling (Kover/detekt)
- **Alternatives**: Full Java migration (rejected), Hybrid (rejected)
- **Consequences**: Requires stakeholder sign-off, documented deviation from Issue #4

### ADR-002: 9-Module Retention

- **Decision**: Keep 9 DDD bounded contexts instead of consolidating to 4
- **Drivers**: DDD integrity, clear business boundaries, event-driven architecture
- **Alternatives**: Consolidation (rejected - violates SRP), Facade modules (deferred)
- **Consequences**: Documentation must map 9 → 4 logical groupings

### ADR-003: React 19 Retention

- **Decision**: Keep React 19 instead of downgrading to 18
- **Drivers**: Backward compatible, better performance, no benefit to downgrade
- **Alternatives**: Downgrade (rejected)
- **Consequences**: None

### ADR-004: DTO Layer Addition

- **Decision**: Add explicit DTO layer between domain entities and API
- **Drivers**: Frontend type mismatch, API versioning support, domain encapsulation
- **Alternatives**: Direct entity exposure (current, rejected - causes tight coupling)
- **Consequences**: Additional mapping layer, but enables independent API evolution

---

## Appendix B: References

### Planner Documents
- `.omc/plans/issue-4-monorepo-strategic-plan.md`
- `.omc/plans/open-questions.md`

### Architect Review
- `.omc/plans/architect-review.md`
- Architectural Assessment Score: 7.5/10
- Key Concerns: Event infrastructure, DTO mismatch, application coupling

### Critic Review
- `.omc/plans/critic-review.md`
- Verdict: CONDITIONAL REJECT (now addressed)
- Key Findings: Flyway factual error, unrealistic timeline, missing stakeholder gate

### Codebase Evidence
- `backend/settings.gradle.kts:7-16` - 9 bounded contexts
- `backend/api/src/main/resources/db/migration/V1-V6*.sql` - Existing Flyway migrations
- `backend/domain/domain-order/src/main/kotlin/com/oms/order/domain/Order.kt` - Domain events
- `frontend/src/shared/types/types.ts:78-94` - Frontend Order type

---

**Plan Status**: ✅ **READY FOR IMPLEMENTATION**

**Next Steps**:
1. Submit RFC for Kotlin retention (Day 1)
2. Wait for stakeholder approval (Day 1-3)
3. Begin Phase 1 upon approval

**Plan Owners**:
- **Technical Lead**: Approve RFC
- **Backend Lead**: Phases 1, 1.5, 3
- **Frontend Lead**: Phases 4
- **DevOps Lead**: Phases 1, 2
- **QA Lead**: Phases 3, 4, 5

---

*This consensus plan incorporates feedback from Planner, Architect, and Critic to ensure technical soundness, realistic timelines, and stakeholder alignment.*
