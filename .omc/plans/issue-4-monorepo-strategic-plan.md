# Issue #4: Monorepo Architecture Strategic Implementation Plan

> **Version**: 4.0 (RALPLAN-DR Consensus Ready)
> **Date**: 2026-03-07
> **Status**: Draft - Awaiting Architect/Critic Review
> **Plan Type**: Consensus Mode (Short Deliberation)

---

## RALPLAN-DR Summary

### Principles (5)

1. **Minimal Disruption**: Preserve working code; avoid unnecessary rewrites
2. **Incremental Delivery**: Ship in phases; each phase independently verifiable
3. **Backward Compatibility**: Existing 9 DDD modules must continue to function
4. **CI/CD First**: Path-filtered CI must gate all subsequent work
5. **OpenAPI Contract-First**: Backend API contracts drive frontend integration

### Decision Drivers (Top 3)

1. **Risk Mitigation**: Kotlin to Java migration carries high regression risk (156 files)
2. **Time to Value**: CI/CD and OpenAPI pipeline provide immediate value
3. **DDD Integrity**: 9 bounded contexts represent significant architectural investment

### Viable Options

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Preserve Kotlin + Enhance** | Keep Kotlin, add missing tooling (JaCoCo, OpenAPI), align CI/CD | Low risk, fast delivery (1-2 weeks), preserves DDD | Diverges from issue spec (Java requirement) |
| **B: Hybrid Migration** | Keep core Kotlin, add Java facade modules for new features | Gradual transition, both languages coexist | Complexity of dual-language build |
| **C: Full Java Migration** | Rewrite all 156 Kotlin files to Java | Exact issue compliance | High risk (4-6 weeks), regression-prone |

**Recommended**: Option A with documented deviation from issue spec. If stakeholder requires Java, escalate for explicit approval before proceeding with Option C.

---

## 1. Gap Analysis

### 1.1 Backend Gaps

| Requirement | Current State | Gap | Priority |
|-------------|--------------|-----|----------|
| Java 21 | Kotlin 1.9, JDK 21 | **MAJOR**: Language mismatch | HIGH - Decision Needed |
| Spring Boot 3.2 | Spring Boot 3.2.2 | None (compliant) | - |
| 4 modules (order/inventory/payment/shared) | 9 DDD modules | **MODERATE**: Module structure differs | MEDIUM |
| Checkstyle | Not present | Gap (Java-specific) | LOW - Kotlin has detekt |
| SpotBugs | Not present | Gap (Java-specific) | LOW - Kotlin has detekt |
| JaCoCo | Not present | **Gap**: No coverage reporting | HIGH |
| Flyway migrations | Not present | **Gap**: No DB versioning | HIGH |
| Logback JSON | Not present | **Gap**: No structured logging | MEDIUM |
| SpringDoc OpenAPI | Present (v2.3.0) | None (compliant) | - |
| PostgreSQL 16 | PostgreSQL 16 | None (compliant) | - |

### 1.2 Frontend Gaps

| Requirement | Current State | Gap | Priority |
|-------------|--------------|-----|----------|
| React 18 | React 19.2.3 | None (exceeds requirement) | - |
| TypeScript 5 | TypeScript 5.8.2 | None (compliant) | - |
| Vite | Vite 6.2.0 | None (compliant) | - |
| pnpm | pnpm (configured) | None (compliant) | - |
| Feature isolation (no cross-imports) | No enforcement | **Gap**: ESLint rules missing | HIGH |
| TanStack Query | v5.62.0 installed | None (compliant) | - |
| Zustand | v4.5.7 installed | None (compliant) | - |
| 70% test coverage | 0% (no tests) | **MAJOR Gap**: No tests | HIGH |

### 1.3 CI/CD Gaps

| Requirement | Current State | Gap | Priority |
|-------------|--------------|-----|----------|
| Path-based filtering | Implemented | None (compliant) | - |
| OpenAPI artifact generation | Not present | **Gap**: No artifact pipeline | HIGH |
| OpenAPI -> TypeScript codegen | Not present | **Gap**: No type generation | HIGH |
| Coverage reporting | Not present | **Gap**: No coverage in CI | HIGH |
| Docker image publishing | Present | None (compliant) | - |

### 1.4 Local Development Gaps

| Requirement | Current State | Gap | Priority |
|-------------|--------------|-----|----------|
| docker-compose.yml | Present | None | - |
| Hot reload | Backend: devtools, Frontend: Vite HMR | None | - |
| Profile support | SPRING_PROFILES_ACTIVE | None | - |

---

## 2. Critical Decisions

### Decision 1: Language (Kotlin vs Java)

**Context**: Issue #4 specifies Java 21, but codebase uses Kotlin 1.9 with 156 source files.

**Options**:

| Option | Risk | Effort | Compliance |
|--------|------|--------|------------|
| A: Keep Kotlin | Low | 1 day | Non-compliant |
| B: Migrate to Java | High | 4-6 weeks | Compliant |

**Recommendation**: **Option A (Keep Kotlin)** with documented deviation.

**Rationale**:
- Kotlin compiles to JVM bytecode; JDK 21 requirement is still met
- detekt + ktlint provide equivalent static analysis to Checkstyle/SpotBugs
- Kover provides JaCoCo-compatible coverage reports
- Migration risk far outweighs compliance benefit

**Action Required**: Stakeholder confirmation that Kotlin is acceptable, OR explicit approval for 4-6 week migration timeline.

---

### Decision 2: Module Structure (4 vs 9 modules)

**Context**: Issue requests 4 modules (order, inventory, payment, shared). Codebase has 9 DDD bounded contexts.

**Options**:

| Option | Structure | Complexity |
|--------|-----------|------------|
| A: Merge to 4 | Combine domains | High (breaks DDD) |
| B: Keep 9 + Facade | 9 domains + 4 facade modules | Medium |
| C: Keep 9, Update Docs | Document actual structure | Low |

**Recommendation**: **Option C (Keep 9, Update Documentation)**

**Rationale**:
- 9 modules represent meaningful business boundaries (identity, catalog, channel, order, inventory, claim, settlement, automation, strategy)
- Merging would violate Single Responsibility and increase coupling
- Issue requirement likely assumed simpler domain; actual domain is richer

**Proposed Mapping**:
```
Issue Requirement  ->  Actual Modules
-----------------      --------------
order              ->  domain-order, domain-claim
inventory          ->  domain-inventory, domain-channel, domain-automation
payment            ->  domain-settlement
shared             ->  core-domain, core-infra, domain-identity, domain-catalog, domain-strategy
```

---

### Decision 3: React Version (18 vs 19)

**Context**: Issue specifies React 18; codebase has React 19.2.3.

**Recommendation**: **Keep React 19**

**Rationale**:
- React 19 is backward compatible with 18
- Downgrading provides no benefit
- React 19 has better performance and concurrent features

---

## 3. Implementation Plan

### Phase 1: Foundation (Day 1-2)

**Objective**: Establish CI/CD infrastructure and coverage baseline.

#### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 1.1 | Add JaCoCo/Kover to backend | `./gradlew koverHtmlReport` generates coverage report |
| 1.2 | Add Flyway migrations | `./gradlew flywayMigrate` runs without error |
| 1.3 | Add ESLint import rules | Cross-feature imports fail lint |
| 1.4 | Update CI to publish coverage | Coverage report visible in PR checks |

#### Files to Create/Modify

```
backend/
  build.gradle.kts                    # Add Kover plugin
  api/src/main/resources/
    db/migration/
      V1__initial_schema.sql          # NEW: Flyway baseline

frontend/
  eslint.config.js                    # Add import restrictions

.github/workflows/
  backend-ci.yml                      # Add coverage step
  frontend-ci.yml                     # Add coverage step
```

---

### Phase 2: OpenAPI Pipeline (Day 3-4)

**Objective**: Automated OpenAPI spec generation and TypeScript client codegen.

#### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 2.1 | Configure SpringDoc OpenAPI | `GET /v3/api-docs` returns valid OpenAPI 3.0 spec |
| 2.2 | Add openapi-generator to CI | `openapi.yaml` artifact uploaded on merge to main |
| 2.3 | Add TypeScript client generation | `frontend/src/api/generated/` contains typed client |
| 2.4 | Add pre-commit hook for spec sync | Outdated spec fails commit |

#### Files to Create/Modify

```
backend/
  api/src/main/kotlin/.../config/
    OpenApiConfig.kt                  # MODIFY: Add metadata
  api/build.gradle.kts                # Add openapi-generator task

frontend/
  src/api/generated/                  # NEW: Generated TypeScript client
  package.json                        # Add codegen script

.github/workflows/
  openapi-codegen.yml                 # NEW: Artifact pipeline
```

---

### Phase 3: Backend Enhancements (Day 5-7)

**Objective**: Add missing backend tooling and reach 80% test coverage.

#### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 3.1 | Add Logback JSON appender | `docker logs oms-backend` shows JSON |
| 3.2 | Write unit tests (core-domain) | 80% coverage on core-domain |
| 3.3 | Write unit tests (domain-order) | 80% coverage on domain-order |
| 3.4 | Write integration tests (api) | API endpoints have happy-path tests |
| 3.5 | Add test containers for integration | Tests run against real DB |

#### Files to Create/Modify

```
backend/
  api/src/main/resources/
    logback-spring.xml                # NEW: JSON logging config
  core/core-domain/src/test/kotlin/   # NEW: Unit tests
  domain/domain-order/src/test/kotlin/ # NEW: Unit tests
  api/src/test/kotlin/                # NEW: Integration tests
  build.gradle.kts                    # Add Testcontainers
```

---

### Phase 4: Frontend Enhancements (Day 8-10)

**Objective**: Add feature isolation and reach 70% test coverage.

#### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 4.1 | Enforce feature isolation | `import from '../features/other'` fails ESLint |
| 4.2 | Add Vitest coverage | `pnpm test:coverage` shows report |
| 4.3 | Write tests for core/api | 70% coverage on core/ |
| 4.4 | Write tests for features/orders | 70% coverage on features/orders |
| 4.5 | Add MSW for API mocking | Tests use mock service worker |

#### Files to Create/Modify

```
frontend/
  eslint.config.js                    # MODIFY: Add no-restricted-imports
  vitest.config.ts                    # MODIFY: Add coverage
  src/test/setup.ts                   # NEW: Test setup with MSW
  src/core/**/*.test.ts               # NEW: Unit tests
  src/features/orders/**/*.test.tsx   # NEW: Component tests
  src/mocks/                          # NEW: MSW handlers
```

---

### Phase 5: Integration & Verification (Day 11-12)

**Objective**: End-to-end validation and documentation.

#### Tasks

| Task | Description | Acceptance Criteria |
|------|-------------|---------------------|
| 5.1 | Run full CI pipeline | All checks pass on clean branch |
| 5.2 | Test docker-compose locally | `docker-compose up` starts all services |
| 5.3 | Verify OpenAPI sync | Generated client matches backend spec |
| 5.4 | Update README | Setup instructions current |
| 5.5 | Update ARCHITECTURE.md | Module structure documented |

---

## 4. Risk Assessment

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Kotlin tooling gaps vs Java | Medium | Low | Kover/detekt are mature |
| Flyway migration conflicts | High | Medium | Version control + review gates |
| OpenAPI codegen drift | Medium | Medium | CI validation of spec freshness |
| Test coverage targets unmet | Medium | Medium | Incremental coverage gates |
| React 19 compatibility | Low | Low | Already working |

### Process Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Stakeholder rejects Kotlin decision | High | Medium | Early escalation, documented rationale |
| Scope creep in Phase 3-4 | Medium | High | Fixed time-box per phase |
| CI complexity slows delivery | Medium | Low | Reuse existing workflow patterns |

---

## 5. Testing Strategy

### Backend Testing

| Layer | Framework | Coverage Target | Scope |
|-------|-----------|-----------------|-------|
| Unit | Kotest + MockK | 80% | Domain logic, services |
| Integration | Spring Boot Test + Testcontainers | 60% | API endpoints, repositories |
| Contract | SpringDoc validation | N/A | OpenAPI spec accuracy |

### Frontend Testing

| Layer | Framework | Coverage Target | Scope |
|-------|-----------|-----------------|-------|
| Unit | Vitest | 70% | Utilities, hooks, stores |
| Component | Testing Library | 60% | Feature components |
| Integration | Vitest + MSW | 50% | API integration |
| E2E | Playwright (future) | N/A | Critical user flows |

---

## 6. Rollout Plan

### Incremental Merge Strategy

```
main
  |
  +-- PR #1: Phase 1 (CI/coverage foundation)
  |     - Merge when: CI green, coverage reporting works
  |
  +-- PR #2: Phase 2 (OpenAPI pipeline)
  |     - Merge when: Artifact generation works
  |
  +-- PR #3: Phase 3 (Backend tests)
  |     - Merge when: 80% coverage achieved
  |
  +-- PR #4: Phase 4 (Frontend tests)
  |     - Merge when: 70% coverage achieved
  |
  +-- PR #5: Phase 5 (Final integration)
        - Merge when: All checks pass, docs updated
```

### Rollback Strategy

Each PR is independently revertable. If any phase introduces regressions:

1. Revert the specific PR
2. Fix in isolation on feature branch
3. Re-merge with fix

---

## 7. ADR: Architecture Decision Record

### Decision

Keep Kotlin 1.9 instead of migrating to Java 21 as specified in Issue #4.

### Drivers

1. 156 Kotlin source files would require complete rewrite
2. Kotlin on JDK 21 meets the JVM version requirement
3. detekt/ktlint/Kover provide equivalent tooling to Checkstyle/SpotBugs/JaCoCo
4. Migration risk outweighs compliance benefit

### Alternatives Considered

| Alternative | Why Rejected |
|-------------|--------------|
| Full Java migration | 4-6 week delay, high regression risk |
| Hybrid Kotlin/Java | Build complexity, inconsistent codebase |
| Ignore Issue #4 | Non-compliance without documentation |

### Why Chosen

Kotlin provides equivalent functionality to Java with lower migration risk. The JDK 21 requirement is satisfied. Documented deviation with stakeholder approval is the pragmatic path.

### Consequences

- Issue #4 requirement partially unmet (language, not JVM version)
- Checkstyle/SpotBugs replaced with detekt/ktlint (equivalent)
- JaCoCo replaced with Kover (compatible reports)
- Requires stakeholder sign-off

### Follow-ups

1. Document Kotlin decision in ARCHITECTURE.md
2. Add Kover coverage gate to CI
3. Configure detekt rules equivalent to SpotBugs checks
4. Update Issue #4 with deviation rationale

---

## 8. Success Criteria

| Criterion | Measurement | Target |
|-----------|-------------|--------|
| Backend test coverage | Kover report | >= 80% |
| Frontend test coverage | Vitest coverage | >= 70% |
| CI path filtering | Changed file triggers only relevant workflow | 100% |
| OpenAPI sync | Generated client matches spec | Automated check |
| Feature isolation | Cross-feature imports blocked | ESLint error |
| Docker local dev | `docker-compose up` succeeds | All services healthy |

---

## Open Questions

1. **Kotlin vs Java**: Does stakeholder accept Kotlin as equivalent to Java 21 requirement?
2. **Module naming**: Should we rename domain modules to match issue terminology (e.g., domain-settlement -> payment)?
3. **Test coverage priority**: If time-constrained, which modules are highest priority for testing?
4. **E2E testing**: Should Playwright E2E tests be included in initial scope or deferred?

---

## Appendix: File Change Summary

### Files to Create (New)

```
backend/
  api/src/main/resources/db/migration/V1__initial_schema.sql
  api/src/main/resources/logback-spring.xml
  core/core-domain/src/test/kotlin/**/*.kt
  domain/domain-order/src/test/kotlin/**/*.kt
  api/src/test/kotlin/**/*.kt

frontend/
  src/api/generated/**/*.ts
  src/test/setup.ts
  src/mocks/**/*.ts
  src/**/*.test.ts(x)

.github/workflows/
  openapi-codegen.yml
```

### Files to Modify (Existing)

```
backend/
  build.gradle.kts                    # Add Kover
  api/build.gradle.kts                # Add OpenAPI generator
  api/src/main/kotlin/**/OpenApiConfig.kt

frontend/
  eslint.config.js                    # Add import restrictions
  vitest.config.ts                    # Add coverage
  package.json                        # Add scripts

.github/workflows/
  backend-ci.yml                      # Add coverage
  frontend-ci.yml                     # Add coverage

docs/
  README.md
  ARCHITECTURE.md
```

---

**Plan saved to:** `.omc/plans/issue-4-monorepo-strategic-plan.md`

**Ready for Architect and Critic review.**
