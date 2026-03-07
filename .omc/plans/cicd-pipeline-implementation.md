# CI/CD Pipeline Implementation Plan

> **Issue:** CI/CD 파이프라인 구축 (GitHub Actions + 코드 품질 검증)
> **Status:** Consensus Plan (Planner + Architect + Critic)
> **Date:** 2026-03-06

---

## Executive Summary

본 문서는 Order Management System (OMS)의 CI/CD 파이프라인 구축을 위한 구현 계획입니다.
Planner, Architect, Critic 세 관점에서 검토한 합의된 계획입니다.

### Key Decisions

| 항목 | 이슈 요구사항 | 실제 구현 | 사유 |
|------|-------------|----------|------|
| 데이터베이스 | PostgreSQL | **MySQL 8.0** | 프로젝트가 MySQL 사용 중 |
| 코드 스타일 | Checkstyle | **ktlint + detekt** | Kotlin 프로젝트에 적합 |
| JDK 버전 | 21 | **21** | 그대로 적용 |
| 버그 패턴 | SpotBugs | **detekt** | Kotlin 호환성 이슈 |

---

## 1. 변경 파일 목록

### 1.1 신규 생성 파일

| 파일 경로 | 설명 |
|----------|------|
| `.github/workflows/ci.yml` | GitHub Actions 메인 워크플로우 |
| `backend/config/detekt/detekt.yml` | Detekt 설정 파일 |
| `backend/api/src/test/kotlin/com/oms/api/OmsApplicationTest.kt` | 샘플 단위 테스트 |
| `backend/api/src/integrationTest/kotlin/com/oms/AbstractIntegrationTest.kt` | 통합 테스트 베이스 클래스 |
| `backend/api/src/integrationTest/kotlin/com/oms/api/health/HealthCheckIntegrationTest.kt` | 샘플 통합 테스트 |
| `backend/api/src/archTest/kotlin/com/oms/architecture/LayerDependencyTest.kt` | 레이어 의존성 테스트 |
| `backend/api/src/archTest/kotlin/com/oms/architecture/NamingConventionTest.kt` | 네이밍 컨벤션 테스트 |
| `backend/api/src/integrationTest/resources/application-integrationTest.yml` | 통합 테스트 설정 |

### 1.2 수정 파일

| 파일 경로 | 변경 내용 |
|----------|----------|
| `backend/build.gradle.kts` | ktlint, detekt, jacoco, kover 플러그인 추가 |
| `backend/settings.gradle.kts` | 플러그인 버전 추가 |
| `backend/gradle.properties` | javaVersion 17 → 21 |
| `backend/api/build.gradle.kts` | integrationTest, archTest 소스셋 및 의존성 추가 |
| `backend/Dockerfile` | JDK 21 베이스 이미지로 변경 |

---

## 2. 구현 단계

### Phase 1: Gradle 인프라 설정 (Day 1)

#### 1.1 gradle.properties 수정

```properties
# Java
javaVersion=21

# Quality Tools
ktlintVersion=12.1.0
detektVersion=1.23.5
koverVersion=0.7.6
archunitVersion=1.2.1
testcontainersVersion=1.19.3
```

#### 1.2 settings.gradle.kts 플러그인 추가

```kotlin
pluginManagement {
    // 기존 코드...

    plugins {
        // 기존 플러그인...
        id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
        id("io.gitlab.arturbosch.detekt") version "1.23.5"
        id("org.jetbrains.kotlinx.kover") version "0.7.6"
    }
}
```

#### 1.3 root build.gradle.kts 수정

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jetbrains.kotlinx.kover")
}

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
        }
    }

    dependencies {
        // Kotlin
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")

        // Test
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("io.mockk:mockk:1.13.9")
        "testImplementation"("io.kotest:kotest-runner-junit5:5.8.0")
        "testImplementation"("io.kotest:kotest-assertions-core:5.8.0")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    // ktlint 설정
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.1.1")
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude { element -> element.file.path.contains("generated") }
        }
    }

    // detekt 설정
    detekt {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
    }

    // JaCoCo 설정
    jacoco {
        toolVersion = "0.8.11"
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

// Kover 전체 프로젝트 커버리지 집계
kover {
    reports {
        total {
            filters {
                excludes {
                    packages("*.dto", "*.config", "*.exception")
                    classes("*Application*", "*Config*", "*Request*", "*Response*")
                }
            }
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}

// Domain modules configuration
configure(subprojects.filter { it.path.startsWith(":domain:") }) {
    dependencies {
        "implementation"(project(":core:core-domain"))
    }
}

// Infrastructure modules configuration
configure(subprojects.filter { it.path.startsWith(":infrastructure:") }) {
    dependencies {
        "implementation"(project(":core:core-domain"))
        "implementation"(project(":core:core-infra"))
    }
}
```

#### 1.4 api/build.gradle.kts 수정 (integrationTest, archTest 소스셋)

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

// Integration Test 소스셋
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
    }

    create("archTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        kotlin.srcDir("src/archTest/kotlin")
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}
val archTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    // 기존 의존성...

    // Integration Test - Testcontainers
    integrationTestImplementation("org.testcontainers:testcontainers:1.19.3")
    integrationTestImplementation("org.testcontainers:junit-jupiter:1.19.3")
    integrationTestImplementation("org.testcontainers:mysql:1.19.3")
    integrationTestImplementation("org.testcontainers:mongodb:1.19.3")

    // ArchUnit
    archTestImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("archTest") {
    description = "Runs architecture tests."
    group = "verification"
    testClassesDirs = sourceSets["archTest"].output.classesDirs
    classpath = sourceSets["archTest"].runtimeClasspath
    useJUnitPlatform()
}

tasks.check {
    dependsOn(tasks.named("integrationTest"))
    dependsOn(tasks.named("archTest"))
}
```

---

### Phase 2: 코드 품질 설정 파일 (Day 1)

#### 2.1 config/detekt/detekt.yml

```yaml
build:
  maxIssues: 0
  weights:
    complexity: 2
    style: 1

config:
  validation: true
  warningsAsErrors: true

complexity:
  LongMethod:
    threshold: 30
  LongParameterList:
    functionThreshold: 6
    constructorThreshold: 8
  CyclomaticComplexMethod:
    threshold: 15
  TooManyFunctions:
    thresholdInFiles: 20
    thresholdInClasses: 15

formatting:
  MaximumLineLength:
    maxLineLength: 120
  Indentation:
    indentSize: 4

naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
  VariableNaming:
    variablePattern: '[a-z][a-zA-Z0-9]*'
  ClassNaming:
    classPattern: '[A-Z][a-zA-Z0-9]*'
  PackageNaming:
    packagePattern: '[a-z]+(\.[a-z][a-z0-9]*)*'

style:
  MagicNumber:
    active: true
    ignoreNumbers:
      - '-1'
      - '0'
      - '1'
      - '2'
  WildcardImport:
    active: true
  MaxLineLength:
    maxLineLength: 120
  ReturnCount:
    max: 3

potential-bugs:
  UnsafeCast:
    active: true
  LateinitUsage:
    active: false
```

---

### Phase 3: ArchUnit 테스트 (Day 2)

#### 3.1 LayerDependencyTest.kt

```kotlin
package com.oms.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LayerDependencyTest {

    private val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.oms")

    @Test
    fun `domain layer should not depend on infrastructure`() {
        noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infra..", "..mysql..", "..mongo..", "..redis..")
            .because("Domain layer must be independent of infrastructure")
            .check(importedClasses)
    }

    @Test
    fun `domain layer should not depend on application layer`() {
        noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInPackage("com.oms.application..")
            .because("Domain layer must not depend on application layer")
            .check(importedClasses)
    }

    @Test
    fun `application layer should not depend on API layer`() {
        noClasses()
            .that().resideInPackage("com.oms.application..")
            .should().dependOnClassesThat()
            .resideInPackage("com.oms.api..")
            .because("Application layer must not depend on API layer")
            .check(importedClasses)
    }

    @Test
    fun `infrastructure should not depend on API layer`() {
        noClasses()
            .that().resideInAnyPackage("..infra..", "..mysql..", "..mongo..", "..redis..")
            .should().dependOnClassesThat()
            .resideInPackage("com.oms.api..")
            .because("Infrastructure layer must not depend on API layer")
            .check(importedClasses)
    }
}
```

#### 3.2 NamingConventionTest.kt

```kotlin
package com.oms.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamingConventionTest {

    private val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.oms")

    @Test
    fun `controllers should be named with Controller suffix`() {
        classes()
            .that().areAnnotatedWith(RestController::class.java)
            .should().haveSimpleNameEndingWith("Controller")
            .check(importedClasses)
    }

    @Test
    fun `services should be named with Service suffix`() {
        classes()
            .that().areAnnotatedWith(Service::class.java)
            .should().haveSimpleNameEndingWith("Service")
            .check(importedClasses)
    }

    @Test
    fun `repositories should be named with Repository suffix`() {
        classes()
            .that().areAnnotatedWith(Repository::class.java)
            .should().haveSimpleNameEndingWith("Repository")
            .orShould().haveSimpleNameEndingWith("RepositoryImpl")
            .check(importedClasses)
    }

    @Test
    fun `exceptions should be named with Exception suffix`() {
        classes()
            .that().areAssignableTo(Exception::class.java)
            .should().haveSimpleNameEndingWith("Exception")
            .check(importedClasses)
    }
}
```

---

### Phase 4: 테스트 인프라 (Day 2-3)

#### 4.1 AbstractIntegrationTest.kt

```kotlin
package com.oms

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integrationTest")
abstract class AbstractIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val mysql = MySQLContainer("mysql:8.0").apply {
            withDatabaseName("oms_test")
            withUsername("test")
            withPassword("test")
        }

        @Container
        @JvmStatic
        val mongodb = MongoDBContainer("mongo:7.0")

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // MySQL
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }

            // MongoDB
            registry.add("spring.data.mongodb.uri") { mongodb.replicaSetUrl }

            // JPA
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }
}
```

#### 4.2 HealthCheckIntegrationTest.kt

```kotlin
package com.oms.api.health

import com.oms.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

class HealthCheckIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `health endpoint should return UP`() {
        val response = restTemplate.getForEntity("/actuator/health", String::class.java)
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.contains("UP") == true)
    }
}
```

#### 4.3 application-integrationTest.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false

logging:
  level:
    org.testcontainers: INFO
    com.oms: DEBUG
```

---

### Phase 5: GitHub Actions Workflow (Day 3)

#### 5.1 .github/workflows/ci.yml

```yaml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
    paths:
      - 'backend/**'
      - '.github/workflows/ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'backend/**'
      - '.github/workflows/ci.yml'

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m"

jobs:
  # ========================================
  # Build & Unit Tests
  # ========================================
  build:
    name: Build & Unit Tests
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: backend

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build project
        run: ./gradlew build -x test -x integrationTest -x archTest --no-daemon

      - name: Run unit tests
        run: ./gradlew test --no-daemon

      - name: Upload unit test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: unit-test-results
          path: backend/**/build/reports/tests/test/
          retention-days: 7

  # ========================================
  # Code Quality
  # ========================================
  code-quality:
    name: Code Quality
    runs-on: ubuntu-latest
    needs: build
    defaults:
      run:
        working-directory: backend

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run ktlint
        run: ./gradlew ktlintCheck --no-daemon

      - name: Run detekt
        run: ./gradlew detekt --no-daemon

      - name: Upload detekt report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: detekt-report
          path: backend/**/build/reports/detekt/
          retention-days: 7

  # ========================================
  # Integration Tests
  # ========================================
  integration-tests:
    name: Integration Tests
    runs-on: ubuntu-latest
    needs: build
    defaults:
      run:
        working-directory: backend

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: oms_test
          MYSQL_USER: test
          MYSQL_PASSWORD: test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

      mongodb:
        image: mongo:7.0
        ports:
          - 27017:27017
        options: >-
          --health-cmd="echo 'db.runCommand(\"ping\").ok' | mongosh localhost:27017/test --quiet"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run integration tests
        run: ./gradlew integrationTest --no-daemon
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/oms_test
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test
          SPRING_DATA_MONGODB_URI: mongodb://localhost:27017/oms_test
          SPRING_REDIS_HOST: localhost
          SPRING_REDIS_PORT: 6379

      - name: Upload integration test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: integration-test-results
          path: backend/**/build/reports/tests/integrationTest/
          retention-days: 7

  # ========================================
  # Architecture Tests
  # ========================================
  arch-tests:
    name: Architecture Tests
    runs-on: ubuntu-latest
    needs: build
    defaults:
      run:
        working-directory: backend

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run architecture tests
        run: ./gradlew archTest --no-daemon

      - name: Upload ArchUnit results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: archunit-results
          path: backend/**/build/reports/tests/archTest/
          retention-days: 7

  # ========================================
  # Coverage
  # ========================================
  coverage:
    name: Code Coverage
    runs-on: ubuntu-latest
    needs: [build, integration-tests]
    defaults:
      run:
        working-directory: backend

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Generate coverage report
        run: ./gradlew koverXmlReport --no-daemon

      - name: Verify coverage threshold (80%)
        run: ./gradlew koverVerify --no-daemon
        continue-on-error: true

      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: backend/build/reports/kover/
          retention-days: 30

      - name: Add coverage to PR
        uses: madrapps/jacoco-report@v1.6.1
        if: github.event_name == 'pull_request'
        with:
          paths: backend/build/reports/kover/report.xml
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 80
          min-coverage-changed-files: 80
          title: "Code Coverage Report"
          update-comment: true
```

---

## 3. 기술적 고려사항

### 3.1 PostgreSQL vs MySQL 결정

| 항목 | 이슈 요구사항 | 프로젝트 현황 | 결정 |
|------|-------------|--------------|------|
| 데이터베이스 | PostgreSQL | MySQL 8.0 | **MySQL 사용** |

**사유:**
- `infra-mysql` 모듈, Flyway 마이그레이션, Docker Compose 모두 MySQL 8.0 사용
- PostgreSQL로 변경 시 마이그레이션 스크립트, JPA dialect 전면 수정 필요
- 이슈 요구사항은 템플릿 또는 오류로 판단

### 3.2 Checkstyle → ktlint + detekt

| 도구 | 용도 | Kotlin 지원 |
|------|------|-------------|
| Checkstyle | Java 코드 스타일 | X (Java 전용) |
| ktlint | Kotlin 코드 포맷팅 | O (공식 규칙) |
| detekt | Kotlin 정적 분석 | O (버그 패턴 포함) |

**결정:** ktlint + detekt 조합 사용 (120자 제한, 4칸 들여쓰기 동일 적용)

### 3.3 SpotBugs 제외

SpotBugs는 Java 바이트코드 분석기로, Kotlin에서 다음 문제 발생:
- Kotlin null safety 이해 못함 → false positive
- Kotlin 관용구 미지원
- detekt이 버그 패턴 검출 기능 포함

**결정:** SpotBugs 제외, detekt의 `potential-bugs` 룰셋으로 대체

### 3.4 JaCoCo vs Kover

| 도구 | 특징 | 멀티모듈 지원 |
|------|------|--------------|
| JaCoCo | 바이트코드 분석 | jacoco-report-aggregation 필요 |
| Kover | Kotlin 네이티브 | 기본 지원 |

**결정:** Kover 사용 (JetBrains 제작, Kotlin 최적화, 멀티모듈 집계 용이)

### 3.5 테스트 소스셋 위치

| 소스셋 | 위치 | 사유 |
|--------|------|------|
| test | 각 모듈 | 단위 테스트는 해당 코드와 함께 |
| integrationTest | api 모듈만 | Spring Context 전체 필요 |
| archTest | api 모듈만 | 전체 클래스 로딩 필요 |

---

## 4. 예상 영향 범위

### 4.1 빌드 시간 영향

| 단계 | 예상 시간 |
|------|----------|
| Build (컴파일) | 2-3분 |
| Unit Tests | 1-2분 |
| Integration Tests | 3-5분 (컨테이너 시작 포함) |
| Architecture Tests | 30초 |
| Code Quality (ktlint + detekt) | 1분 |
| Coverage Report | 1분 |
| **전체 CI 파이프라인** | **8-12분** |

### 4.2 개발 워크플로우 변경

- PR 제출 시 자동 검증 실행
- ktlint 포맷 위반 시 빌드 실패 → `./gradlew ktlintFormat`으로 자동 수정
- detekt 위반 시 빌드 실패 → 코드 수정 필요
- 80% 커버리지 미달 시 경고 (초기에는 실패 아님)

### 4.3 기존 코드 영향

현재 코드에서 예상되는 위반:
- detekt: 일부 긴 함수, 복잡도 경고 가능
- ArchUnit: 레이어 의존성 위반 가능성
- 커버리지: 0% 상태 → 테스트 추가 필요

**완화 전략:**
1. detekt baseline 파일 생성으로 기존 이슈 허용
2. 커버리지 검증 초기 비활성화 (warning only)
3. 점진적으로 임계값 상향

---

## 5. 테스트 계획

### 5.1 단위 테스트 범위

| 모듈 | 테스트 대상 | 우선순위 |
|------|-----------|---------|
| domain-order | Order, OrderItem, OrderStatus | 높음 |
| domain-inventory | Inventory, Stock | 높음 |
| domain-claim | Claim, ClaimType | 중간 |
| application | OrderService, InventoryService | 높음 |
| core-domain | Money, Address, DomainEvent | 높음 |

### 5.2 통합 테스트 범위

| 테스트 | 설명 | 의존성 |
|--------|------|--------|
| HealthCheckIntegrationTest | Actuator 헬스 체크 | 전체 |
| OrderApiIntegrationTest | 주문 API 전체 흐름 | MySQL |
| InventoryApiIntegrationTest | 재고 API 전체 흐름 | MySQL, Redis |
| EventPublishIntegrationTest | 이벤트 발행/구독 | MongoDB |

### 5.3 아키텍처 테스트 규칙

1. **레이어 의존성:** Domain → Infrastructure 금지
2. **네이밍 규칙:** Controller, Service, Repository 접미사
3. **패키지 구조:** Bounded Context 간 순환 의존성 금지

---

## 6. 위험 평가 및 완화

### 6.1 높은 위험

| 위험 | 영향 | 완화 |
|------|------|------|
| 테스트 0% → 80% 커버리지 불가 | 빌드 실패 | 커버리지 검증 초기 비활성화 |
| detekt 기존 코드 위반 다수 | 빌드 실패 | baseline 파일 생성 |

### 6.2 중간 위험

| 위험 | 영향 | 완화 |
|------|------|------|
| CI 시간 증가 (8-12분) | 개발 속도 저하 | Gradle 캐싱 적극 활용 |
| Testcontainers CI 문제 | 통합 테스트 실패 | Service containers 병행 |

### 6.3 낮은 위험

| 위험 | 영향 | 완화 |
|------|------|------|
| JDK 21 호환성 | 빌드 실패 | Spring Boot 3.2.2 공식 지원 |
| Kover 설정 | 커버리지 미집계 | JaCoCo 대안 준비 |

---

## 7. 구현 로드맵

### Day 1: Gradle 인프라
- [ ] gradle.properties 수정 (JDK 21, 플러그인 버전)
- [ ] settings.gradle.kts 플러그인 추가
- [ ] root build.gradle.kts 수정 (ktlint, detekt, kover)
- [ ] api/build.gradle.kts 소스셋 추가 (integrationTest, archTest)
- [ ] config/detekt/detekt.yml 생성

### Day 2: 테스트 인프라
- [ ] AbstractIntegrationTest 베이스 클래스
- [ ] application-integrationTest.yml
- [ ] ArchUnit 테스트 클래스 (LayerDependencyTest, NamingConventionTest)
- [ ] 샘플 통합 테스트 (HealthCheckIntegrationTest)

### Day 3: GitHub Actions
- [ ] .github/workflows/ci.yml 생성
- [ ] 워크플로우 테스트 실행
- [ ] PR 코멘트 설정 확인
- [ ] Dockerfile JDK 21 업데이트

### Day 4-7: 테스트 작성 (선택적)
- [ ] 도메인 단위 테스트 작성
- [ ] 서비스 레이어 테스트 작성
- [ ] API 통합 테스트 작성
- [ ] 커버리지 80% 달성

---

## 8. 성공 기준

- [ ] PR마다 자동 검증 (빌드, 린트, 테스트, 아키텍처) 실행
- [ ] 테스트 결과 PR 코멘트 자동 게시
- [ ] 커버리지 리포트 업로드 (아티팩트)
- [ ] 모든 체크 통과 시에만 머지 가능 (Branch Protection Rules)
- [ ] ktlint, detekt 위반 시 빌드 실패
- [ ] ArchUnit 레이어 의존성 검증 통과

---

## Appendix: 파일 변경 상세

### A. gradle.properties (수정)

```diff
- javaVersion=17
+ javaVersion=21
+
+ # Quality Tools
+ ktlintVersion=12.1.0
+ detektVersion=1.23.5
+ koverVersion=0.7.6
+ archunitVersion=1.2.1
+ testcontainersVersion=1.19.3
```

### B. Dockerfile (수정)

```diff
- FROM gradle:8.5-jdk17 AS build
+ FROM gradle:8.5-jdk21 AS build
...
- FROM eclipse-temurin:17-jre-alpine
+ FROM eclipse-temurin:21-jre-alpine
```

---

**Plan Version:** 1.0
**Consensus:** Planner ✓ | Architect ✓ | Critic ✓ (with noted deviations)
