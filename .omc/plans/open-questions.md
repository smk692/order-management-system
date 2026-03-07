# Open Questions

## Issue #6 Quality and Test Infrastructure - 2026-03-07

### Pre-Implementation Decisions

- [ ] **Database seeding strategy**: Should integration tests seed specific test data via SQL scripts, or rely on test setup creating necessary entities programmatically? — Affects test isolation and repeatability

- [ ] **Performance baseline metrics**: What specific metrics should we capture for virtual thread performance? Options: requests/sec, latency p50/p95/p99, thread pool utilization, memory footprint. — Affects instrumentation needed

- [ ] **Frontend E2E scope**: Should we add Playwright/Cypress for E2E tests in this phase, or defer to a future iteration? — Affects timeline and Phase 6 scope

- [ ] **API contract testing**: Should we add Spring Cloud Contract or Pact for consumer-driven contract testing between frontend and backend? — Important for type safety but adds complexity

### Clarifications on Original Plan

- [x] **Language migration**: The original plan mentioned "Kotlin to Java 21 migration" but the codebase is already on JVM 21 with Kotlin. **RESOLVED**: No language migration needed - focus on testing and quality.

- [x] **Module consolidation**: Original plan mentioned "9 to 4 modules" but codebase has 18 well-organized DDD modules. **RESOLVED**: No consolidation needed - current architecture is clean.

- [ ] **Coverage targets**: The plan states 70% coverage target. Is this line coverage, branch coverage, or both? — Affects Kover/JaCoCo configuration

### Technical Decisions

- [ ] **Test container lifecycle**: Should we use `@Testcontainers` annotation (per-test containers) or singleton pattern (shared across tests)? — Trade-off between isolation and speed

- [ ] **Kotest vs JUnit style**: Backend is set up for both Kotest and JUnit5. Should we standardize on one, or allow both? — Affects consistency

- [ ] **MSW version**: MSW 2.x has breaking changes from 1.x. Confirm we want MSW 2.0 for frontend mocking. — Affects test setup code

### CI/CD

- [ ] **Coverage reporting**: Should coverage reports be uploaded to Codecov, SonarQube, or just GitHub artifacts? — Affects CI workflow

- [ ] **Branch protection**: Should ArchUnit tests be required to pass for PR merge? — Affects branch protection rules

---

## Archived Questions (Previous Plan - Issue #4)

These questions from the previous misunderstood plan are now obsolete:

- [x] ~~Migration parallelization~~ **OBSOLETE**: No Kotlin-to-Java migration happening
- [x] ~~React version downgrade~~ **NOT NEEDED**: React 19 is acceptable
- [x] ~~Domain consolidation~~ **NOT NEEDED**: Current 18-module architecture is correct
- [x] ~~Database MySQL vs PostgreSQL~~ **RESOLVED**: docker-compose uses PostgreSQL
