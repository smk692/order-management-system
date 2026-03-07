# Issue #4: Monorepo Restructuring - Java 21 Implementation Plan

> **Version**: 1.0
> **Date**: 2026-03-07
> **Status**: Ready for Implementation
> **Plan Type**: Comprehensive Implementation Plan (Kotlin to Java 21 Migration + Monorepo Restructuring)

---

## Executive Summary

This plan implements Issue #4 requirements with **strict adherence to the Java 21 mandate**:

1. **Full Kotlin-to-Java migration** - Convert all 158 Kotlin files to Java 21
2. **Virtual Threads enablement** - `spring.threads.virtual.enabled=true`
3. **Simplified module structure** - 4 logical modules (shared, order, inventory, payment)
4. **ArchUnit architecture enforcement** - Module boundaries, layered architecture, @Transactional rules
5. **Contract-first OpenAPI workflow** - Auto-generated TypeScript types
6. **80%/70% test coverage** - Backend/Frontend with proper tooling

**Key Difference from Previous Plan**: This plan **does not retain Kotlin**. It implements the explicit requirement "Kotlin 사용 안 함. Java 21 고정."

---

## Current State Analysis

### Backend Status

| Aspect | Current State | Gap |
|--------|---------------|-----|
| Language | Kotlin 1.9 (158 files) | **FULL MIGRATION REQUIRED** |
| JDK Version | 21 | OK (already configured) |
| Virtual Threads | Not enabled | Add `spring.threads.virtual.enabled=true` |
| Module Structure | 9 DDD modules | Simplify to 4 logical modules |
| Build Tool | Gradle Kotlin DSL | OK (reuse with Java) |
| Quality Tools | detekt, ktlint | Replace with Checkstyle, SpotBugs |
| Coverage Tool | None | Add JaCoCo |
| Flyway | V1-V6 exist | OK |
| ArchUnit | Not present | Add module/layer tests |

### Frontend Status

| Aspect | Current State | Gap |
|--------|---------------|-----|
| Framework | React 19 + TypeScript 5 + Vite 6 | Requirement says React 18 (minor) |
| Package Manager | pnpm | OK |
| Feature Structure | 12 features exist | Add ESLint cross-import rules |
| State Management | TanStack Query + Zustand | OK |
| Test Coverage | ~0% | Target 70% |
| OpenAPI Types | Not auto-generated | Add codegen pipeline |

### CI/CD Status

| Aspect | Current State | Gap |
|--------|---------------|-----|
| Path-based triggers | Partially configured | Enhance with independent pipelines |
| Backend CI | Build + detekt + ktlint | Replace with Checkstyle + SpotBugs + JaCoCo |
| Frontend CI | Build + lint + test | Add coverage thresholds, OpenAPI sync |
| OpenAPI artifact | Not present | Add artifact upload |

---

## Phase 0: Pre-Migration Setup (Day 1-2)

### Objective
Prepare infrastructure for Kotlin-to-Java migration and set up new tooling.

### Tasks

| Task | Description | Acceptance Criteria | Files |
|------|-------------|---------------------|-------|
| 0.1 | Update `build.gradle.kts` for Java-only | Remove Kotlin plugins, add Java toolchain | `backend/build.gradle.kts` |
| 0.2 | Add Checkstyle configuration | `./gradlew checkstyleMain` passes | `backend/config/checkstyle/checkstyle.xml` |
| 0.3 | Add SpotBugs configuration | `./gradlew spotbugsMain` passes | `backend/build.gradle.kts` |
| 0.4 | Add JaCoCo with 80% threshold | `./gradlew jacocoTestReport` generates report | `backend/build.gradle.kts` |
| 0.5 | Add ArchUnit dependency | Test infrastructure ready | `backend/build.gradle.kts` |
| 0.6 | Create target module structure | Empty `modules/` directory tree | See structure below |

### Target Module Structure

```
backend/
├── modules/
│   ├── shared/                    # Cross-cutting concerns
│   │   ├── src/main/java/com/oms/shared/
│   │   │   ├── domain/           # Core domain (Money, Address, etc.)
│   │   │   ├── event/            # Domain event infrastructure
│   │   │   ├── exception/        # Common exceptions
│   │   │   └── config/           # Shared configurations
│   │   └── build.gradle.kts
│   ├── order/                     # Order bounded context
│   │   ├── src/main/java/com/oms/order/
│   │   │   ├── domain/           # Order, Claim entities
│   │   │   ├── application/      # OrderService, ClaimService
│   │   │   ├── api/              # OrderController, ClaimController
│   │   │   └── infrastructure/   # JPA repositories
│   │   └── build.gradle.kts
│   ├── inventory/                 # Inventory bounded context
│   │   ├── src/main/java/com/oms/inventory/
│   │   │   ├── domain/           # Stock, Warehouse, Channel
│   │   │   ├── application/      # StockService, ChannelService
│   │   │   ├── api/              # Controllers
│   │   │   └── infrastructure/   # Repositories
│   │   └── build.gradle.kts
│   └── payment/                   # Payment bounded context
│       ├── src/main/java/com/oms/payment/
│       │   ├── domain/           # Settlement entities
│       │   ├── application/      # SettlementService
│       │   ├── api/              # Controllers
│       │   └── infrastructure/   # Repositories
│       └── build.gradle.kts
├── api/                           # API entry point (main app)
│   ├── src/main/java/com/oms/api/
│   │   └── OmsApplication.java
│   └── build.gradle.kts
└── build.gradle.kts               # Root build file
```

### New `settings.gradle.kts`

```kotlin
rootProject.name = "oms-backend"

// Module structure per Issue #4
include(":modules:shared")
include(":modules:order")
include(":modules:inventory")
include(":modules:payment")
include(":api")

pluginManagement {
    plugins {
        id("org.springframework.boot") version "3.2.0"
        id("io.spring.dependency-management") version "1.1.4"
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

### New Root `build.gradle.kts`

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.6" apply false
    id("jacoco")
}

allprojects {
    group = "com.oms"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
        }
    }

    dependencies {
        // Test dependencies
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
        testImplementation("org.testcontainers:postgresql:1.19.3")
        testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf(
            "--enable-preview",
            "-Xlint:all",
            "-Werror"
        ))
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }

    checkstyle {
        toolVersion = "10.12.5"
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    }
}

// Module dependency constraints
configure(subprojects.filter { it.path.startsWith(":modules:") && it.name != "shared" }) {
    dependencies {
        implementation(project(":modules:shared"))
    }
}
```

---

## Phase 1: Kotlin-to-Java Migration (Day 3-10)

### Objective
Convert all 158 Kotlin files to Java 21 with Virtual Threads support.

### Migration Strategy

**Approach**: Module-by-module migration with automated conversion + manual cleanup

**Tools**:
1. IntelliJ IDEA's "Convert Kotlin to Java" (automated baseline)
2. Manual Java 21 idiom updates (records, sealed, pattern matching)
3. Virtual Thread annotations where applicable

### Migration Order

| Order | Module | Kotlin Files | Est. Days | Priority |
|-------|--------|--------------|-----------|----------|
| 1 | core-domain -> shared/domain | ~20 | 1 | P0 |
| 2 | core-infra -> shared/config | ~5 | 0.5 | P0 |
| 3 | domain-order + domain-claim -> order/domain | ~30 | 2 | P0 |
| 4 | domain-inventory + domain-channel -> inventory/domain | ~35 | 2 | P1 |
| 5 | domain-settlement -> payment/domain | ~15 | 1 | P1 |
| 6 | application -> */application | ~40 | 1.5 | P1 |
| 7 | api -> api + */api | ~13 | 1 | P1 |

**Total**: ~158 files, 8 days

### Java 21 Conversion Patterns

#### Pattern 1: Data Class -> Record

```kotlin
// Kotlin (before)
data class Money(
    val amount: BigDecimal,
    val currency: Currency
)
```

```java
// Java 21 (after)
public record Money(
    BigDecimal amount,
    Currency currency
) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }

    public Money add(Money other) {
        if (this.currency != other.currency) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

#### Pattern 2: Sealed Class -> Sealed Interface

```kotlin
// Kotlin (before)
sealed class DomainEvent {
    abstract val occurredAt: Instant
}

data class OrderCreatedEvent(
    val orderId: String,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()
```

```java
// Java 21 (after)
public sealed interface DomainEvent permits OrderCreatedEvent, OrderCancelledEvent {
    Instant occurredAt();
}

public record OrderCreatedEvent(
    String orderId,
    Instant occurredAt
) implements DomainEvent {
    public OrderCreatedEvent(String orderId) {
        this(orderId, Instant.now());
    }
}
```

#### Pattern 3: When Expression -> Switch Expression

```kotlin
// Kotlin (before)
fun getStatusLabel(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING -> "주문 대기"
    OrderStatus.CONFIRMED -> "주문 확정"
    OrderStatus.SHIPPED -> "배송 중"
    OrderStatus.DELIVERED -> "배송 완료"
    OrderStatus.CANCELLED -> "주문 취소"
}
```

```java
// Java 21 (after)
public String getStatusLabel(OrderStatus status) {
    return switch (status) {
        case PENDING -> "주문 대기";
        case CONFIRMED -> "주문 확정";
        case SHIPPED -> "배송 중";
        case DELIVERED -> "배송 완료";
        case CANCELLED -> "주문 취소";
    };
}
```

#### Pattern 4: Extension Function -> Static Utility

```kotlin
// Kotlin (before)
fun Order.toResponse(): OrderResponse = OrderResponse(
    id = this.id,
    channel = this.channelId,
    ...
)
```

```java
// Java 21 (after)
public final class OrderMapper {
    private OrderMapper() {}

    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.id(),
            order.channelId(),
            ...
        );
    }
}
```

#### Pattern 5: Coroutine -> Virtual Thread

```kotlin
// Kotlin with coroutines (before)
suspend fun processOrders(orders: List<Order>): List<OrderResult> = coroutineScope {
    orders.map { order ->
        async { processOrder(order) }
    }.awaitAll()
}
```

```java
// Java 21 with Virtual Threads (after)
public List<OrderResult> processOrders(List<Order> orders) {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        return orders.stream()
            .map(order -> executor.submit(() -> processOrder(order)))
            .toList()
            .stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
    }
}
```

### Virtual Threads Configuration

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
```

```java
// WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
```

### Migration Acceptance Criteria

| Criterion | Verification |
|-----------|--------------|
| No `.kt` files remain | `find backend -name "*.kt" \| wc -l` returns 0 |
| All Java files compile | `./gradlew compileJava` succeeds |
| Java 21 features used | Records for DTOs, sealed interfaces for events |
| Virtual Threads enabled | Config present in application.yml |
| Tests pass | `./gradlew test` succeeds |

---

## Phase 2: ArchUnit Architecture Tests (Day 11-12)

### Objective
Enforce module boundaries and layered architecture via ArchUnit tests.

### ArchUnit Test Suite

```java
// modules/shared/src/test/java/com/oms/architecture/ModuleDependencyTest.java
package com.oms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class ModuleDependencyTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.oms");
    }

    /**
     * Rule: order/inventory/payment modules CANNOT depend on each other
     * They can only depend on shared
     */
    @Test
    void modulesShouldNotDependOnEachOther() {
        SlicesRuleDefinition.slices()
            .matching("com.oms.(*)..")
            .should().notDependOnEachOther()
            .ignoreDependencyToPackage("com.oms.shared..")
            .check(classes);
    }

    /**
     * Rule: Layered architecture within each module
     * Controller -> Service -> Repository
     */
    @Test
    void shouldFollowLayeredArchitecture() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("API").definedBy("..api..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            .whereLayer("API").mayOnlyBeAccessedByLayers()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("API")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("API", "Application", "Infrastructure")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application")

            .check(classes);
    }

    /**
     * Rule: @Transactional only on Service layer
     */
    @Test
    void transactionalShouldOnlyBeOnServiceLayer() {
        noClasses()
            .that().resideInAPackage("..api..")
            .or().resideInAPackage("..domain..")
            .or().resideInAPackage("..infrastructure..")
            .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
            .check(classes);
    }

    /**
     * Rule: Controllers should not access repositories directly
     */
    @Test
    void controllersShouldNotAccessRepositories() {
        noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(classes);
    }

    /**
     * Rule: Domain entities should not depend on Spring
     */
    @Test
    void domainShouldNotDependOnSpring() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .check(classes);
    }

    /**
     * Rule: No circular dependencies between modules
     */
    @Test
    void shouldNotHaveCircularDependencies() {
        SlicesRuleDefinition.slices()
            .matching("com.oms.(*)..")
            .should().beFreeOfCycles()
            .check(classes);
    }
}
```

### Additional Architecture Rules

```java
// modules/shared/src/test/java/com/oms/architecture/NamingConventionTest.java
package com.oms.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class NamingConventionTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter().importPackages("com.oms");
    }

    @Test
    void controllersShouldBeSuffixed() {
        classes()
            .that().resideInAPackage("..api..")
            .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().haveSimpleNameEndingWith("Controller")
            .check(classes);
    }

    @Test
    void servicesShouldBeSuffixed() {
        classes()
            .that().resideInAPackage("..application..")
            .and().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().haveSimpleNameEndingWith("Service")
            .check(classes);
    }

    @Test
    void repositoriesShouldBeSuffixed() {
        classes()
            .that().resideInAPackage("..infrastructure..")
            .and().areAssignableTo("org.springframework.data.repository.Repository")
            .should().haveSimpleNameEndingWith("Repository")
            .check(classes);
    }
}
```

---

## Phase 3: Backend Testing (Day 13-19)

### Objective
Achieve 80% test coverage with meaningful tests.

### Coverage Strategy

| Module | Target | Priority | Test Types |
|--------|--------|----------|------------|
| shared/domain | 90% | P0 | Unit tests (records, value objects) |
| order/domain | 85% | P0 | Unit + state machine tests |
| order/application | 80% | P0 | Service integration tests |
| inventory/domain | 80% | P1 | Unit tests |
| inventory/application | 75% | P1 | Service tests |
| payment/domain | 80% | P1 | Unit tests |
| */api | 70% | P2 | Controller integration tests |
| */infrastructure | 70% | P2 | Repository tests |

### Test Examples

```java
// modules/shared/src/test/java/com/oms/shared/domain/MoneyTest.java
package com.oms.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithValidValues() {
        var money = new Money(BigDecimal.valueOf(1000), Currency.KRW);

        assertThat(money.amount()).isEqualByComparingTo("1000");
        assertThat(money.currency()).isEqualTo(Currency.KRW);
    }

    @Test
    void shouldAddSameCurrency() {
        var m1 = new Money(BigDecimal.valueOf(1000), Currency.KRW);
        var m2 = new Money(BigDecimal.valueOf(500), Currency.KRW);

        var result = m1.add(m2);

        assertThat(result.amount()).isEqualByComparingTo("1500");
        assertThat(result.currency()).isEqualTo(Currency.KRW);
    }

    @Test
    void shouldThrowWhenAddingDifferentCurrencies() {
        var krw = new Money(BigDecimal.valueOf(1000), Currency.KRW);
        var usd = new Money(BigDecimal.valueOf(1), Currency.USD);

        assertThatThrownBy(() -> krw.add(usd))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot add different currencies");
    }

    @ParameterizedTest
    @CsvSource({
        "0, true",
        "100, false",
        "-100, false"
    })
    void shouldDetectZeroAmount(int amount, boolean expectedZero) {
        var money = new Money(BigDecimal.valueOf(amount), Currency.KRW);

        assertThat(money.isZero()).isEqualTo(expectedZero);
    }
}
```

```java
// modules/order/src/test/java/com/oms/order/domain/OrderTest.java
package com.oms.order.domain;

import com.oms.order.domain.event.OrderCreatedEvent;
import com.oms.order.domain.event.OrderCancelledEvent;
import com.oms.shared.domain.Money;
import com.oms.shared.domain.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Nested
    class Creation {

        @Test
        void shouldCreateOrderWithPendingStatus() {
            var order = Order.create(
                "COUPANG",
                new Customer("John Doe", "john@example.com"),
                List.of(new OrderItem("PROD-001", 2, new Money(BigDecimal.valueOf(10000), Currency.KRW)))
            );

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getDomainEvents()).hasSize(1);
            assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCreatedEvent.class);
        }

        @Test
        void shouldCalculateTotalAmount() {
            var order = Order.create(
                "COUPANG",
                new Customer("John Doe", "john@example.com"),
                List.of(
                    new OrderItem("PROD-001", 2, new Money(BigDecimal.valueOf(10000), Currency.KRW)),
                    new OrderItem("PROD-002", 1, new Money(BigDecimal.valueOf(5000), Currency.KRW))
                )
            );

            assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("25000");
        }
    }

    @Nested
    class StateTransitions {

        @Test
        void shouldConfirmPendingOrder() {
            var order = createPendingOrder();

            order.confirm();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        void shouldNotCancelShippedOrder() {
            var order = createPendingOrder();
            order.confirm();
            order.ship();

            assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel order in SHIPPED status");
        }

        @Test
        void shouldEmitCancelledEventOnCancellation() {
            var order = createPendingOrder();
            order.clearEvents(); // Clear creation event

            order.cancel();

            assertThat(order.getDomainEvents()).hasSize(1);
            assertThat(order.getDomainEvents().get(0)).isInstanceOf(OrderCancelledEvent.class);
        }

        private Order createPendingOrder() {
            return Order.create(
                "COUPANG",
                new Customer("John Doe", "john@example.com"),
                List.of(new OrderItem("PROD-001", 1, new Money(BigDecimal.valueOf(10000), Currency.KRW)))
            );
        }
    }
}
```

```java
// modules/order/src/test/java/com/oms/order/application/OrderServiceTest.java
package com.oms.order.application;

import com.oms.order.domain.Order;
import com.oms.order.domain.OrderRepository;
import com.oms.shared.event.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        var command = new CreateOrderCommand(
            "COUPANG",
            new CustomerDto("John Doe", "john@example.com"),
            List.of(new OrderItemDto("PROD-001", 1, BigDecimal.valueOf(10000)))
        );
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var orderId = orderService.createOrder(command);

        assertThat(orderId).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void shouldFindOrderById() {
        var order = mock(Order.class);
        when(order.getId()).thenReturn("ORD-001");
        when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));

        var result = orderService.getOrder("ORD-001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("ORD-001");
    }
}
```

### Integration Tests with Testcontainers

```java
// api/src/test/java/com/oms/api/AbstractIntegrationTest.java
package com.oms.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("oms_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## Phase 4: OpenAPI Pipeline (Day 20-21)

### Objective
Establish contract-first API development with automated TypeScript generation.

### Backend OpenAPI Configuration

```java
// api/src/main/java/com/oms/api/config/OpenApiConfig.java
package com.oms.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI omsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("OMS API")
                .description("Order Management System API")
                .version("1.0.0")
                .contact(new Contact()
                    .name("OMS Team")
                    .email("oms@example.com")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.oms.example.com").description("Production")
            ));
    }
}
```

### OpenAPI Gradle Task

```kotlin
// api/build.gradle.kts
plugins {
    id("org.springframework.boot")
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$buildDir/openapi"))
    outputFileName.set("openapi.yaml")
}

tasks.register("generateOpenApiSpec") {
    dependsOn("bootRun")
    doLast {
        exec {
            commandLine("curl", "-s", "http://localhost:8080/v3/api-docs.yaml", "-o", "$buildDir/openapi/openapi.yaml")
        }
    }
}
```

### CI Pipeline for OpenAPI

```yaml
# .github/workflows/openapi-pipeline.yml
name: OpenAPI Pipeline

on:
  push:
    branches: [main]
    paths:
      - 'backend/api/src/main/java/**'
      - 'backend/modules/*/src/main/java/**/api/**'

jobs:
  generate-openapi:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: oms_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: --health-cmd pg_isready --health-interval 10s --health-timeout 5s --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Start backend
        working-directory: ./backend
        run: |
          ./gradlew :api:bootRun &
          sleep 60
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/oms_test

      - name: Generate OpenAPI spec
        run: curl -s http://localhost:8080/v3/api-docs.yaml -o openapi.yaml

      - name: Upload OpenAPI artifact
        uses: actions/upload-artifact@v4
        with:
          name: openapi-spec
          path: openapi.yaml

  generate-typescript:
    needs: generate-openapi
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Download OpenAPI spec
        uses: actions/download-artifact@v4
        with:
          name: openapi-spec

      - name: Generate TypeScript client
        run: |
          npx @openapitools/openapi-generator-cli generate \
            -i openapi.yaml \
            -g typescript-axios \
            -o frontend/src/api/generated \
            --additional-properties=supportsES6=true,withInterfaces=true

      - name: Commit generated code
        run: |
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add frontend/src/api/generated
          git diff --staged --quiet || git commit -m "chore: Update generated API client [skip ci]"
          git push
```

---

## Phase 5: Frontend Testing & Feature Isolation (Day 22-27)

### Objective
Achieve 70% coverage and enforce feature isolation.

### ESLint Feature Isolation Rules

```javascript
// frontend/eslint.config.js
import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import importPlugin from 'eslint-plugin-import';

export default tseslint.config(
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    plugins: {
      react,
      'react-hooks': reactHooks,
      'import': importPlugin,
    },
    rules: {
      // Feature isolation: cross-feature imports forbidden
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@/features/order/*', '!@/features/order'],
              message: 'Cannot import from order feature internals. Use public exports only.',
            },
            {
              group: ['@/features/inventory/*', '!@/features/inventory'],
              message: 'Cannot import from inventory feature internals. Use public exports only.',
            },
            {
              group: ['@/features/payment/*', '!@/features/payment'],
              message: 'Cannot import from payment feature internals. Use public exports only.',
            },
          ],
        },
      ],
      // Cross-feature imports must go through shared
      'import/no-restricted-paths': [
        'error',
        {
          zones: [
            {
              target: './src/features/order',
              from: './src/features/inventory',
              message: 'Order cannot import from Inventory. Use shared or events.',
            },
            {
              target: './src/features/order',
              from: './src/features/payment',
              message: 'Order cannot import from Payment. Use shared or events.',
            },
            {
              target: './src/features/inventory',
              from: './src/features/order',
              message: 'Inventory cannot import from Order. Use shared or events.',
            },
            {
              target: './src/features/inventory',
              from: './src/features/payment',
              message: 'Inventory cannot import from Payment. Use shared or events.',
            },
            {
              target: './src/features/payment',
              from: './src/features/order',
              message: 'Payment cannot import from Order. Use shared or events.',
            },
            {
              target: './src/features/payment',
              from: './src/features/inventory',
              message: 'Payment cannot import from Inventory. Use shared or events.',
            },
          ],
        },
      ],
    },
  }
);
```

### Vitest Coverage Configuration

```typescript
// frontend/vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        'src/api/generated/',
        '**/*.d.ts',
        '**/*.config.*',
      ],
      thresholds: {
        lines: 70,
        functions: 70,
        branches: 70,
        statements: 70,
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

### MSW Setup

```typescript
// frontend/src/test/setup.ts
import '@testing-library/jest-dom';
import { beforeAll, afterEach, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { handlers } from './mocks/handlers';

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

```typescript
// frontend/src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/v1/orders', () => {
    return HttpResponse.json({
      content: [
        {
          id: 'ORD-001',
          channel: 'COUPANG',
          customerName: 'John Doe',
          totalAmount: 50000,
          currency: 'KRW',
          status: 'PENDING',
          items: [],
        },
      ],
      totalPages: 1,
      totalElements: 1,
    });
  }),

  http.get('/api/v1/orders/:id', ({ params }) => {
    return HttpResponse.json({
      id: params.id,
      channel: 'COUPANG',
      customerName: 'John Doe',
      totalAmount: 50000,
      currency: 'KRW',
      status: 'PENDING',
      items: [],
    });
  }),

  http.post('/api/v1/orders', async ({ request }) => {
    const body = await request.json();
    return HttpResponse.json(
      { id: 'ORD-NEW-001', ...body },
      { status: 201 }
    );
  }),
];
```

### Frontend Test Examples

```typescript
// frontend/src/features/orders/components/OrderList.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { OrderList } from './OrderList';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('OrderList', () => {
  it('should display orders when API returns data', async () => {
    render(<OrderList />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('John Doe')).toBeInTheDocument();
    });
  });

  it('should show loading state initially', () => {
    render(<OrderList />, { wrapper: createWrapper() });

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('should display order total amount formatted', async () => {
    render(<OrderList />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText('50,000 KRW')).toBeInTheDocument();
    });
  });
});
```

---

## Phase 6: CI/CD Enhancement (Day 28-29)

### Objective
Update CI pipelines for Java 21, quality tools, and independent triggers.

### Updated Backend CI

```yaml
# .github/workflows/backend-ci.yml
name: Backend CI

on:
  push:
    branches: [main]
    paths:
      - 'backend/**'
      - '.github/workflows/backend-ci.yml'
  pull_request:
    branches: [main]
    paths:
      - 'backend/**'
      - '.github/workflows/backend-ci.yml'

jobs:
  quality:
    name: Code Quality
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run Checkstyle
        working-directory: ./backend
        run: ./gradlew checkstyleMain checkstyleTest

      - name: Run SpotBugs
        working-directory: ./backend
        run: ./gradlew spotbugsMain

      - name: Upload quality reports
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: quality-reports
          path: |
            backend/**/build/reports/checkstyle
            backend/**/build/reports/spotbugs

  test:
    name: Test & Coverage
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: oms_test
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: --health-cmd pg_isready --health-interval 10s --health-timeout 5s --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run tests with coverage
        working-directory: ./backend
        run: ./gradlew test jacocoTestReport
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/oms_test
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: postgres

      - name: Verify coverage threshold (80%)
        working-directory: ./backend
        run: ./gradlew jacocoTestCoverageVerification

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: backend/**/build/reports/jacoco

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: backend/**/build/reports/jacoco/test/jacocoTestReport.xml

  archunit:
    name: Architecture Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run ArchUnit tests
        working-directory: ./backend
        run: ./gradlew :modules:shared:test --tests "*ArchitectureTest*" --tests "*ModuleDependencyTest*"

  build:
    name: Build
    needs: [quality, test, archunit]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build
        working-directory: ./backend
        run: ./gradlew build -x test

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-artifacts
          path: backend/**/build/libs/*.jar
```

### Updated Frontend CI

```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI

on:
  push:
    branches: [main]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-ci.yml'
  pull_request:
    branches: [main]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-ci.yml'

jobs:
  lint:
    name: Lint & Type Check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup pnpm
        uses: pnpm/action-setup@v4
        with:
          version: 9

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: ./frontend
        run: pnpm install --frozen-lockfile

      - name: Run ESLint
        working-directory: ./frontend
        run: pnpm lint

      - name: Run TypeScript check
        working-directory: ./frontend
        run: pnpm exec tsc --noEmit

  test:
    name: Test & Coverage
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup pnpm
        uses: pnpm/action-setup@v4
        with:
          version: 9

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: ./frontend
        run: pnpm install --frozen-lockfile

      - name: Download OpenAPI spec
        uses: actions/download-artifact@v4
        with:
          name: openapi-spec
          path: frontend/
        continue-on-error: true

      - name: Run tests with coverage
        working-directory: ./frontend
        run: pnpm test:coverage

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: frontend-coverage
          path: frontend/coverage

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: frontend/coverage/lcov.info

  build:
    name: Build
    needs: [lint, test]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup pnpm
        uses: pnpm/action-setup@v4
        with:
          version: 9

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: ./frontend
        run: pnpm install --frozen-lockfile

      - name: Build
        working-directory: ./frontend
        run: pnpm build

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist
```

---

## Phase 7: Documentation & Local Dev (Day 30)

### Objective
Finalize documentation and local development setup.

### Updated docker-compose.yml

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: oms-postgres
    restart: unless-stopped
    environment:
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-oms_password}
      POSTGRES_DB: ${POSTGRES_DB:-oms}
      POSTGRES_USER: ${POSTGRES_USER:-oms}
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgresql_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U oms"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - oms-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: oms-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-oms}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-oms}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-oms_password}
      SPRING_THREADS_VIRTUAL_ENABLED: "true"
      JAVA_TOOL_OPTIONS: "-XX:+UseZGC -XX:+ZGenerational"
    ports:
      - "${BACKEND_PORT:-8080}:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
    networks:
      - oms-network

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: oms-frontend
    restart: unless-stopped
    ports:
      - "${FRONTEND_PORT:-3000}:80"
    depends_on:
      - backend
    networks:
      - oms-network

networks:
  oms-network:
    driver: bridge

volumes:
  postgresql_data:
```

### Updated Backend Dockerfile

```dockerfile
# backend/Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY modules modules
COPY api api

RUN chmod +x ./gradlew
RUN ./gradlew :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S oms && adduser -S oms -G oms
USER oms

COPY --from=builder /app/api/build/libs/*.jar app.jar

# Enable Virtual Threads and ZGC
ENV JAVA_TOOL_OPTIONS="-XX:+UseZGC -XX:+ZGenerational"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### .env.example

```bash
# .env.example

# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=oms
POSTGRES_USER=oms
POSTGRES_PASSWORD=oms_password

# Backend
BACKEND_PORT=8080
SPRING_PROFILES_ACTIVE=local

# Frontend
FRONTEND_PORT=3000
VITE_API_BASE_URL=http://localhost:8080

# Java Options
JAVA_TOOL_OPTIONS=-XX:+UseZGC -XX:+ZGenerational
```

---

## Success Criteria Verification

| Criterion | Target | Verification Command |
|-----------|--------|---------------------|
| Java version | 21, no Kotlin | `find backend -name "*.kt" \| wc -l` returns 0 |
| Virtual Threads | Enabled | Check `application.yml` for `spring.threads.virtual.enabled=true` |
| Multi-module build | Success | `./gradlew build` |
| ArchUnit tests | Pass | `./gradlew :modules:shared:test --tests "*ArchitectureTest*"` |
| Module isolation | Enforced | Order/Inventory/Payment cannot import each other |
| Layered architecture | Enforced | Controller -> Service -> Repository |
| @Transactional | Service only | ArchUnit test passes |
| Frontend build | Success | `pnpm build` |
| OpenAPI types | Auto-generated | `frontend/src/api/generated/` exists |
| Feature isolation | Enforced | `pnpm lint` passes |
| Backend coverage | >= 80% | `./gradlew jacocoTestCoverageVerification` |
| Frontend coverage | >= 70% | `pnpm test:coverage` threshold check |
| CI independence | Verified | Backend change -> only backend-ci runs |
| Local dev | Works | `docker-compose up` starts all services |

---

## Risk Assessment

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Kotlin-to-Java migration bugs | HIGH | MEDIUM | Comprehensive test coverage before/after |
| Virtual Threads compatibility | MEDIUM | LOW | Spring Boot 3.2 has full support |
| ArchUnit test failures | LOW | MEDIUM | Fix architecture violations during migration |
| Test coverage deadline | MEDIUM | MEDIUM | Prioritize critical paths, defer low-risk areas |
| OpenAPI breaking changes | MEDIUM | LOW | Generated client with version pinning |

---

## Rollback Strategy

### Code Rollback

```bash
# Revert specific phase
git revert <phase-merge-commit>

# Revert to pre-migration state
git checkout <pre-migration-tag>
```

### Database Rollback

Flyway migrations are preserved. No schema changes in this plan.

### CI Rollback

All CI changes are in separate workflow files. Revert specific file changes.

---

## Timeline Summary

| Phase | Days | Key Deliverables |
|-------|------|------------------|
| Phase 0: Pre-Migration | 1-2 | Gradle setup, tooling, structure |
| Phase 1: Kotlin->Java | 3-10 | 158 files converted, Virtual Threads |
| Phase 2: ArchUnit | 11-12 | Architecture tests |
| Phase 3: Backend Tests | 13-19 | 80% coverage |
| Phase 4: OpenAPI | 20-21 | TypeScript codegen |
| Phase 5: Frontend | 22-27 | 70% coverage, feature isolation |
| Phase 6: CI/CD | 28-29 | Updated pipelines |
| Phase 7: Docs | 30 | Documentation, local dev |

**Total**: 30 days

---

## File Change Summary

### Files to Delete (158 Kotlin files)
- All `*.kt` files in `backend/`

### Files to Create

```
backend/
├── modules/
│   ├── shared/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/oms/shared/**/*.java
│   ├── order/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/oms/order/**/*.java
│   ├── inventory/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/oms/inventory/**/*.java
│   └── payment/
│       ├── build.gradle.kts
│       └── src/main/java/com/oms/payment/**/*.java
├── api/
│   ├── build.gradle.kts
│   └── src/main/java/com/oms/api/**/*.java
├── config/
│   └── checkstyle/
│       └── checkstyle.xml
├── build.gradle.kts (updated)
└── settings.gradle.kts (updated)

frontend/
├── src/
│   ├── api/generated/ (OpenAPI)
│   └── test/
│       ├── setup.ts (updated)
│       └── mocks/
│           ├── handlers.ts
│           └── factories.ts
├── eslint.config.js (updated)
└── vitest.config.ts (updated)

.github/workflows/
├── backend-ci.yml (updated)
├── frontend-ci.yml (updated)
└── openapi-pipeline.yml (new)
```

### Files to Modify

```
backend/
├── build.gradle.kts
├── settings.gradle.kts
└── api/src/main/resources/application.yml

frontend/
├── package.json
├── eslint.config.js
├── vitest.config.ts
└── vite.config.ts

docker-compose.yml
.env.example
```

---

## Next Steps

1. **Begin Phase 0**: Set up Java toolchain and target module structure
2. **Create migration checklist**: Track each Kotlin file conversion
3. **Set up CI for Java**: Ensure builds work with Java before migration
4. **Start Phase 1**: Convert modules in dependency order (shared first)

---

*Plan created: 2026-03-07*
*Plan status: Ready for Implementation*
