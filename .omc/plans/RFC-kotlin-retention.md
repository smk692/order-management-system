# RFC: Kotlin Retention for Issue #4 Implementation

**Status:** Pending Approval
**Created:** 2026-03-07
**Decision Required By:** Day 3
**Approvers:** Tech Lead, Product Owner

---

## Context

Issue #4 mandates a **Java 21** upgrade as part of the monorepo migration. However, the current codebase contains:

- **156 Kotlin files** across **9 DDD modules** (domain, application, infrastructure)
- Modern Kotlin 1.9 syntax leveraging JDK 21 features
- Well-structured domain model with extensive use of data classes, sealed types, and coroutines
- Production-ready implementation with comprehensive test coverage

A literal interpretation of "Java 21" would require a full Kotlin → Java migration.

---

## Decision

**Retain Kotlin 1.9 on JDK 21** instead of migrating to pure Java.

This approach:
- Fully satisfies the **JDK 21 requirement** (Kotlin compiles to JVM bytecode)
- Preserves the existing, stable implementation
- Enables immediate focus on monorepo migration tasks

---

## Rationale

### 1. **Time and Risk**
A full Kotlin → Java migration would require:
- **4-6 weeks** of dedicated effort for 156 files
- Manual translation of:
  - Data classes → Java records/POJOs
  - Sealed classes → Java sealed types (verbose)
  - Extension functions → static utility methods
  - Null-safety → `@Nullable`/`@NonNull` annotations
  - Coroutines → CompletableFuture or virtual threads
- High regression risk requiring extensive re-testing

### 2. **Technical Equivalence**
Kotlin on JDK 21 provides:
- Full JDK 21 feature access (virtual threads, pattern matching, sequenced collections)
- Modern tooling ecosystem:
  - **Kover** (JaCoCo equivalent for coverage)
  - **detekt** (Checkstyle/PMD equivalent for static analysis)
  - Native Gradle/Maven support
- Production-grade interoperability with Java libraries

### 3. **Strategic Alignment**
The core issue (#4) goals are:
- Monorepo structure → ✅ (language-agnostic)
- CI/CD pipeline → ✅ (works with Kotlin)
- Local development → ✅ (Docker Compose supports both)
- JDK 21 runtime → ✅ (Kotlin compiles to JVM 21 bytecode)

Language choice is **orthogonal** to these objectives.

### 4. **Industry Precedent**
Major projects run Kotlin on modern JDKs:
- Spring Framework (mixed Java/Kotlin)
- Ktor (Kotlin-first on JDK 17+)
- Gradle (Kotlin DSL on JDK 21)

---

## Alternatives Considered

### ❌ **Option A: Full Java Migration**
**Rejected** due to:
- 4-6 week delay
- High regression risk
- Loss of Kotlin productivity features (data classes, null safety)
- No technical benefit for monorepo goals

### ❌ **Option B: Hybrid Approach (New code in Java, keep Kotlin)**
**Rejected** due to:
- Increased cognitive load (two languages)
- Mixed conventions and idioms
- Long-term maintenance complexity

### ✅ **Option C: Kotlin Retention (Chosen)**
**Accepted** as the pragmatic path forward.

---

## Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| Stakeholder expectation mismatch ("Java 21" literally means Java source code) | **This RFC** documents the technical rationale; seek explicit approval |
| Team unfamiliarity with Kotlin | Provide training resources; Kotlin/Java interop is seamless |
| Future Java-only mandates | Re-evaluate if organizational policy changes; migration remains feasible later |

---

## Timeline Impact

| Approach | Duration | Impact on Issue #4 |
|----------|----------|-------------------|
| **Kotlin Retention** | Immediate | ✅ 26-day monorepo implementation starts immediately |
| Full Java Migration | 4-6 weeks | ⚠️ Delays monorepo work; cascading schedule impact |

---

## Implementation Notes

If approved, the Issue #4 implementation proceeds with:

1. **Build Configuration**
   - Gradle Kotlin plugin 1.9.x
   - Target JVM 21 bytecode
   - Enable JDK 21 preview features

2. **CI/CD Pipeline**
   - Use `openjdk:21-slim` Docker base
   - Integrate Kover for coverage reports
   - Add detekt for Kotlin linting

3. **Documentation**
   - Update README to specify "Kotlin 1.9 on JDK 21"
   - Add Kotlin coding standards to CONTRIBUTING.md

---

## Decision

**[ ] Approved** – Proceed with Kotlin retention
**[ ] Rejected** – Initiate Java migration plan
**[ ] Deferred** – Requires additional discussion

**Approval Signatures:**

- **Tech Lead:** _____________________ Date: _____
- **Product Owner:** _____________________ Date: _____

---

## References

- [Kotlin JDK 21 Compatibility](https://kotlinlang.org/docs/compatibility-modes.html)
- [Kover Coverage Tool](https://github.com/Kotlin/kotlinx-kover)
- [detekt Static Analyzer](https://detekt.dev/)
- Issue #4 Implementation Plan: `.omc/plans/issue-4-monorepo-strategic-plan.md`
