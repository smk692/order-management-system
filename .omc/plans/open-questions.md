# Open Questions

## Issue #4 Java 21 Implementation Plan - 2026-03-07

### Critical Decisions

- [ ] **Migration parallelization**: Should multiple developers work on Kotlin-to-Java migration simultaneously, or should it be sequential to avoid merge conflicts? -- Affects Phase 1 duration (8 days estimate assumes some parallelization)
- [ ] **React version**: Issue requires React 18, but current frontend uses React 19. Should we downgrade, or is React 19 acceptable given backward compatibility? -- Minor impact, but affects compliance statement
- [ ] **Database**: Issue mentions PostgreSQL but current config uses MySQL. Which database should be used? -- Affects Flyway migrations and Testcontainers setup

### Architecture Decisions

- [ ] **Domain consolidation**: When merging 9 DDD modules into 4, how should domain events between consolidated contexts be handled (e.g., OrderCreatedEvent and ClaimCreatedEvent now in same module)? -- Affects event infrastructure design
- [ ] **Shared module scope**: Should shared module include domain-identity, domain-catalog, and domain-strategy, or should these become separate modules? -- Affects module dependency graph
- [ ] **Cross-module communication**: For runtime communication between modules (order -> inventory), should we use in-process events or define explicit service interfaces? -- Affects architecture and testability

### Testing Strategy

- [ ] **Test coverage priority**: If time-constrained, which areas should reach 80% first? Current recommendation: shared/domain > order/domain > order/application. -- Determines Phase 3 task ordering
- [ ] **Integration test scope**: Should integration tests use H2 or Testcontainers with PostgreSQL? Testcontainers is more realistic but slower. -- Affects CI runtime and test reliability
- [ ] **E2E testing**: Should Playwright E2E tests be included in initial scope (adds 2-3 days) or deferred to future iteration? -- Affects overall timeline

### CI/CD

- [ ] **OpenAPI spec versioning**: Should OpenAPI spec use date-based versioning (2026-03-07) or semantic versioning (1.0.0)? -- Affects artifact naming in CI
- [ ] **Coverage reporting**: Should coverage reports be uploaded to Codecov, SonarQube, or both? -- Affects CI workflow configuration
- [ ] **Branch protection**: Should ArchUnit tests be required to pass for PR merge? -- Affects branch protection rules

### Frontend

- [ ] **Feature isolation strictness**: Should cross-feature imports be ERROR or WARN in ESLint? Some features may have legitimate dependencies through shared. -- Affects Phase 5 ESLint configuration
- [ ] **Generated API client**: Should generated TypeScript client be committed to repo or generated on-the-fly during build? -- Affects developer workflow and CI caching

---

## From Previous Analysis (Legacy)

These questions remain from the previous plan that proposed Kotlin retention:

- [x] **Kotlin vs Java**: ~~Does stakeholder accept Kotlin as equivalent to Java 21 requirement?~~ **RESOLVED**: New plan implements full Java 21 migration per explicit requirement.
- [ ] **Module naming convention**: Should domain modules be renamed to match issue terminology (e.g., domain-settlement -> payment, domain-order -> order)? -- **ADDRESSED**: New plan uses required naming (shared, order, inventory, payment)
- [x] **Database migration strategy**: ~~Should Flyway baseline be created from current schema or fresh schema design?~~ **RESOLVED**: Existing V1-V6 migrations are preserved; no schema changes required.
