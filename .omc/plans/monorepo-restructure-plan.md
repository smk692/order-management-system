# Monorepo 구조 재구성 구현 계획

> **문서 버전**: 2.0 (Consensus Plan - Planner + Architect + Critic)
> **생성일**: 2026-03-06
> **상태**: 검토 대기

---

## 목차

1. [Executive Summary](#1-executive-summary)
2. [현재 상태 분석](#2-현재-상태-분석)
3. [Gap 분석 및 아키텍처 결정](#3-gap-분석-및-아키텍처-결정)
4. [구현 계획](#4-구현-계획)
5. [변경 파일 목록](#5-변경-파일-목록)
6. [테스트 계획](#6-테스트-계획)
7. [리스크 및 대응](#7-리스크-및-대응)
8. [일정 및 마일스톤](#8-일정-및-마일스톤)

---

## 1. Executive Summary

### 핵심 결정 사항

| 항목 | 이슈 요구사항 | **컨센서스 결정** | 근거 |
|------|--------------|------------------|------|
| 백엔드 언어 | Java 21 | **Kotlin 유지 + JVM 21** | 90개 Kotlin 파일 재작성 비용 회피 |
| 백엔드 모듈 구조 | 4개 모듈 (order, inventory, payment, shared) | **현재 17개 모듈 구조 유지** | DDD 기반 성숙한 아키텍처 보존 |
| 데이터베이스 | PostgreSQL 16 | **MySQL 8.0 유지** | 마이그레이션 ROI 낮음, MongoDB가 JSONB 대안 |
| React 버전 | React 18 | **React 19 유지** | 다운그레이드 불필요, 19가 stable |
| 패키지 매니저 | pnpm | **pnpm 전환** | 이점 존재, 위험 낮음 |
| CI/CD | Path 분리 워크플로우 | **신규 구축** | 현재 완전 부재 |
| 품질 도구 | checkstyle, spotbugs, jacoco | **ktlint, detekt, jacoco** | Kotlin 프로젝트에 적합 |

### 접근 방식

**점진적 개선 (Incremental Improvement)** - 기존 아키텍처의 강점을 유지하면서 품질 도구와 CI/CD만 선별적으로 도입

### 예상 총 공수
- **점진적 접근 (채택)**: 5-6주
- ~~전면 재구성: 8-10주~~ (거부)

---

## 2. 현재 상태 분석

### 2.1 백엔드 아키텍처

```
backend/ (Kotlin 1.9.22 + Spring Boot 3.2.2 + JVM 17)
├── core/                    # 공유 도메인 (2개 모듈)
│   ├── core-domain/        # Money, Address, AuditableEntity
│   └── core-infra/         # 공통 인프라
├── domain/                  # 9개 Bounded Contexts
│   ├── domain-identity/    # 회사, 사용자, 권한
│   ├── domain-catalog/     # 상품 카탈로그
│   ├── domain-channel/     # 판매 채널
│   ├── domain-order/       # 주문 관리 (DDD Aggregate Root)
│   ├── domain-inventory/   # 재고 관리
│   ├── domain-claim/       # 반품/교환/환불
│   ├── domain-settlement/  # 정산
│   ├── domain-automation/  # 자동화 규칙
│   └── domain-strategy/    # 전략/글로벌 준비도
├── infrastructure/          # 데이터베이스 구현 (4개 모듈)
│   ├── infra-mysql/
│   ├── infra-mongo/
│   ├── infra-redis/
│   └── infra-external/
├── application/             # 애플리케이션 서비스
└── api/                     # REST Controllers
```

**통계:**
- Kotlin 파일: ~90개
- Gradle 모듈: 17개
- API 엔드포인트: 12개 주요 경로
- Flyway 마이그레이션: 활성화됨
- springdoc OpenAPI: 설정됨

### 2.2 프론트엔드 아키텍처

```
frontend/ (React 19 + TypeScript 5.8 + Vite 6)
├── components/              # 21개 재사용 컴포넌트
├── views/                   # 13개 페이지
├── stores/                  # 6개 Zustand 스토어
├── hooks/                   # 2개 커스텀 훅
├── services/                # API 통신
├── i18n/                    # 다국어 (en, ko)
└── types.ts                 # 타입 정의
```

**통계:**
- TypeScript 파일: 51개
- npm (package-lock.json 사용)

### 2.3 인프라 현황

| 서비스 | 버전 | 용도 |
|--------|------|------|
| MySQL | 8.0 | 관계형 데이터 |
| MongoDB | 7.0 | 문서형 데이터 (로그, 추적) |
| Redis | 7 | 캐시, 세션 |
| Docker Compose | 3.8 | 개발 환경 |

### 2.4 CI/CD 현황

**현재 상태: 완전 부재**
- `.github/workflows/` 디렉토리 없음
- 자동화된 테스트/빌드 없음
- OpenAPI 자동 생성 파이프라인 없음

---

## 3. Gap 분석 및 아키텍처 결정

### 3.1 백엔드 Gap 및 결정

| 항목 | 현재 | 이슈 요구 | **결정** | 근거 |
|------|------|----------|----------|------|
| 언어 | Kotlin | Java | **Kotlin 유지** | 90개 파일 재작성 비용 회피 |
| JVM | 17 | 21 | **21로 업그레이드** | Virtual Threads 이점 |
| 모듈 구조 | 17개 DDD 모듈 | 4개 간소화 모듈 | **현재 구조 유지** | 9개 BC 중 7개가 4모듈에 매핑 불가 |
| DB | MySQL 8.0 | PostgreSQL 16 | **MySQL 유지** | 마이그레이션 ROI 낮음 |
| Virtual Threads | 없음 | 있음 | **추가** | 성능 이점 |
| CORS | 부분 설정 | 명시적 설정 | **추가/보완** | localhost:5173 지원 |
| Flyway | 있음 | 있음 | **유지** | 이미 구현됨 |
| JSON 로깅 | 없음 | 있음 | **추가** | 운영 환경 필수 |
| 품질 도구 | 없음 | checkstyle, spotbugs, jacoco | **ktlint, detekt, jacoco** | Kotlin용 |

### 3.2 프론트엔드 Gap 및 결정

| 항목 | 현재 | 이슈 요구 | **결정** | 근거 |
|------|------|----------|----------|------|
| React | 19 | 18 | **19 유지** | 다운그레이드 불필요 |
| 패키지 매니저 | npm | pnpm | **pnpm 전환** | 이점 존재 |
| 디렉토리 구조 | views/, components/ | src/features/, src/shared/ | **구조 개편** | 모듈화 필요 |
| TanStack Query | 없음 | 있음 | **추가** | 서버 상태 관리 개선 |
| Zustand | 있음 | 있음 | **유지** | 클라이언트 상태용 |
| ESLint/Prettier | 없음 | 있음 | **추가** | 코드 일관성 |
| Vite proxy | localhost:3001 | localhost:8080 | **수정** | 백엔드 연동 |

### 3.3 인프라/CI Gap 및 결정

| 항목 | 현재 | 이슈 요구 | **결정** |
|------|------|----------|----------|
| GitHub Actions | 없음 | backend-ci.yml, frontend-ci.yml | **신규 생성** |
| OpenAPI 파이프라인 | 없음 | 자동 생성 | **추가** |
| 테스트 커버리지 | 미설정 | 80% (백엔드), 70% (프론트) | **jacoco 설정** (초기 70%, 점진적 80%) |

---

## 4. 구현 계획

### Phase 1: CI/CD + 품질 도구 (Week 1-2)

#### Step 1.1: GitHub Actions 백엔드 CI
**파일 생성:** `.github/workflows/backend-ci.yml`

```yaml
name: Backend CI
on:
  push:
    paths:
      - 'backend/**'
      - 'docker-compose.yml'
      - '.github/workflows/backend-ci.yml'
  pull_request:
    paths:
      - 'backend/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle.kts') }}
      - name: Build
        run: cd backend && ./gradlew build -x test
      - name: Test
        run: cd backend && ./gradlew test
      - name: Code Analysis
        run: cd backend && ./gradlew ktlintCheck detekt
      - name: Coverage
        run: cd backend && ./gradlew jacocoTestReport
      - name: Generate OpenAPI
        run: cd backend && ./gradlew generateOpenApiDocs
      - uses: actions/upload-artifact@v4
        with:
          name: openapi-spec
          path: backend/api/build/openapi.json
```

#### Step 1.2: GitHub Actions 프론트엔드 CI
**파일 생성:** `.github/workflows/frontend-ci.yml`

```yaml
name: Frontend CI
on:
  push:
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-ci.yml'
  pull_request:
    paths:
      - 'frontend/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup pnpm
        uses: pnpm/action-setup@v2
        with:
          version: 8
      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml
      - name: Install
        working-directory: frontend
        run: pnpm install
      - name: Lint
        working-directory: frontend
        run: pnpm lint
      - name: Type Check
        working-directory: frontend
        run: pnpm type-check
      - name: Build
        working-directory: frontend
        run: pnpm build
```

#### Step 1.3: 백엔드 품질 도구 설정

**파일 수정:** `backend/build.gradle.kts`
```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    id("jacoco")
}

ktlint {
    version.set("1.1.0")
    android.set(false)
}

detekt {
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal() // 초기 70%, 점진적으로 80%
            }
        }
    }
}
```

**파일 생성:** `backend/config/detekt/detekt.yml`
```yaml
build:
  maxIssues: 0
  excludeCorrectable: false

complexity:
  LongMethod:
    threshold: 60
  TooManyFunctions:
    threshold: 20

style:
  MaxLineLength:
    maxLineLength: 120
```

#### Step 1.4: 프론트엔드 ESLint/Prettier

**파일 생성:** `frontend/.eslintrc.cjs`
```javascript
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'prettier',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh', 'import'],
  rules: {
    'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
    'import/no-restricted-paths': [
      'error',
      {
        zones: [
          { target: './src/features/order', from: './src/features/inventory' },
          { target: './src/features/order', from: './src/features/claim' },
          { target: './src/features/inventory', from: './src/features/order' },
          { target: './src/features/inventory', from: './src/features/claim' },
          { target: './src/features/claim', from: './src/features/order' },
          { target: './src/features/claim', from: './src/features/inventory' },
        ],
      },
    ],
  },
};
```

**파일 생성:** `frontend/.prettierrc`
```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

---

### Phase 2: 프론트엔드 구조 개편 (Week 3)

#### Step 2.1: pnpm 전환
```bash
cd frontend
rm -rf node_modules package-lock.json
pnpm install
```

**package.json scripts 업데이트:**
```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .ts,.tsx",
    "lint:fix": "eslint . --ext .ts,.tsx --fix",
    "type-check": "tsc --noEmit",
    "format": "prettier --write \"src/**/*.{ts,tsx}\""
  }
}
```

#### Step 2.2: 디렉토리 구조 재편

**목표 구조:**
```
frontend/
├── src/
│   ├── features/
│   │   ├── order/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── pages/
│   │   │   │   └── OrderView.tsx (from views/OrderView.tsx)
│   │   │   └── store.ts
│   │   ├── inventory/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── pages/
│   │   │   │   └── InventoryView.tsx
│   │   │   └── store.ts
│   │   ├── claim/
│   │   │   ├── components/
│   │   │   └── pages/
│   │   │       └── ClaimsView.tsx
│   │   └── settlement/
│   │       ├── components/
│   │       └── pages/
│   │           └── SettlementView.tsx
│   ├── shared/
│   │   ├── api/
│   │   │   ├── queryClient.ts
│   │   │   └── types.ts (OpenAPI generated)
│   │   ├── components/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── ViewLoader.tsx
│   │   ├── hooks/
│   │   ├── stores/
│   │   │   └── useAppStore.ts
│   │   └── utils/
│   └── app/
│       ├── App.tsx
│       ├── index.tsx
│       └── providers/
│           └── QueryProvider.tsx
├── public/
└── index.html
```

#### Step 2.3: TanStack Query 도입

**의존성 추가:**
```bash
pnpm add @tanstack/react-query
```

**파일 생성:** `frontend/src/app/providers/QueryProvider.tsx`
```typescript
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5분
      retry: 1,
    },
  },
});

export function QueryProvider({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
}
```

**사용 예시:** `frontend/src/features/order/hooks/useOrders.ts`
```typescript
import { useQuery } from '@tanstack/react-query';
import { fetchOrders } from '@/shared/api/orders';

export const useOrders = () => {
  return useQuery({
    queryKey: ['orders'],
    queryFn: fetchOrders,
  });
};
```

#### Step 2.4: Vite 설정 업데이트

**파일 수정:** `frontend/vite.config.ts`
```typescript
import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  return {
    server: {
      port: 5173,
      host: '0.0.0.0',
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    plugins: [react()],
    define: {
      __APP_ENV__: JSON.stringify(env.VITE_APP_ENV || 'development'),
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@features': path.resolve(__dirname, './src/features'),
        '@shared': path.resolve(__dirname, './src/shared'),
      },
    },
  };
});
```

---

### Phase 3: 백엔드 업그레이드 (Week 4)

#### Step 3.1: Java 21 업그레이드

**파일 수정:** `backend/gradle.properties`
```properties
javaVersion=21
```

**파일 수정:** `backend/build.gradle.kts`
```kotlin
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
```

**파일 수정:** `backend/Dockerfile`
```dockerfile
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle :api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S oms && adduser -S oms -G oms
WORKDIR /app
COPY --from=build /app/api/build/libs/*.jar app.jar
USER oms
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Step 3.2: Virtual Threads 활성화

**파일 수정:** `backend/api/src/main/resources/application.yml`
```yaml
spring:
  application:
    name: oms-api
  threads:
    virtual:
      enabled: true
  # ... 기존 설정 유지
```

#### Step 3.3: CORS 설정 보완

**파일 생성/수정:** `backend/api/src/main/kotlin/com/oms/api/config/CorsConfig.kt`
```kotlin
package com.oms.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:5173",  // Vite dev
                "http://localhost:3000"   // Production
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
```

#### Step 3.4: JSON 로깅 설정

**파일 생성:** `backend/api/src/main/resources/logback-spring.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="prod">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>

    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
</configuration>
```

**의존성 추가:** `backend/api/build.gradle.kts`
```kotlin
dependencies {
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
}
```

---

### Phase 4: OpenAPI 파이프라인 (Week 5)

#### Step 4.1: OpenAPI 생성 설정

**파일 수정:** `backend/api/build.gradle.kts`
```kotlin
plugins {
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$buildDir/openapi"))
    outputFileName.set("openapi.json")
}
```

#### Step 4.2: OpenAPI → TypeScript 자동 생성

**파일 생성:** `.github/workflows/openapi-generate.yml`
```yaml
name: OpenAPI Generate
on:
  workflow_run:
    workflows: ["Backend CI"]
    types: [completed]
    branches: [main]

jobs:
  generate:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Download OpenAPI artifact
        uses: dawidd6/action-download-artifact@v3
        with:
          workflow: backend-ci.yml
          name: openapi-spec
          path: ./

      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Generate TypeScript types
        run: |
          npx openapi-typescript openapi.json -o frontend/src/shared/api/types.ts

      - name: Commit changes
        uses: stefanzweifel/git-auto-commit-action@v5
        with:
          commit_message: "chore: Update OpenAPI types"
          file_pattern: "frontend/src/shared/api/types.ts"
```

---

### Phase 5: Docker 환경 정비 (Week 5)

#### Step 5.1: .env.example 업데이트

```env
# Database (MySQL - 유지)
MYSQL_DATABASE=oms
MYSQL_USER=oms
MYSQL_PASSWORD=oms_password
MYSQL_ROOT_PASSWORD=root
MYSQL_PORT=3306

# MongoDB
MONGO_DATABASE=oms
MONGO_USER=oms
MONGO_PASSWORD=oms_password
MONGO_PORT=27017

# Redis
REDIS_PORT=6379

# Backend
SPRING_PROFILES_ACTIVE=docker
BACKEND_PORT=8080

# Frontend
FRONTEND_PORT=3000
VITE_API_BASE_URL=http://localhost:8080
```

---

## 5. 변경 파일 목록

### 5.1 신규 생성 파일 (15개)

| 경로 | 설명 |
|------|------|
| `.github/workflows/backend-ci.yml` | 백엔드 CI 워크플로우 |
| `.github/workflows/frontend-ci.yml` | 프론트엔드 CI 워크플로우 |
| `.github/workflows/openapi-generate.yml` | OpenAPI 타입 생성 |
| `backend/config/detekt/detekt.yml` | detekt 설정 |
| `backend/api/src/main/resources/logback-spring.xml` | JSON 로깅 |
| `backend/api/src/main/kotlin/com/oms/api/config/CorsConfig.kt` | CORS 설정 |
| `frontend/.eslintrc.cjs` | ESLint 설정 |
| `frontend/.prettierrc` | Prettier 설정 |
| `frontend/pnpm-lock.yaml` | pnpm 락파일 |
| `frontend/src/app/providers/QueryProvider.tsx` | TanStack Query 프로바이더 |
| `frontend/src/shared/api/queryClient.ts` | Query Client 설정 |
| `frontend/src/shared/api/types.ts` | OpenAPI 생성 타입 (자동 생성) |

### 5.2 수정 파일 (10개)

| 경로 | 변경 내용 |
|------|----------|
| `backend/build.gradle.kts` | ktlint, detekt, jacoco 플러그인 추가 |
| `backend/gradle.properties` | javaVersion=21 |
| `backend/api/build.gradle.kts` | logstash-encoder, springdoc-plugin 추가 |
| `backend/api/src/main/resources/application.yml` | Virtual Threads 활성화 |
| `backend/Dockerfile` | JDK 21 베이스 이미지 |
| `frontend/package.json` | scripts 추가, 의존성 추가 |
| `frontend/vite.config.ts` | proxy 타겟 수정, alias 업데이트 |
| `frontend/tsconfig.json` | path alias 설정 |
| `.env.example` | 환경변수 정리 |

### 5.3 마이그레이션 파일 (구조 재편)

| From | To |
|------|-----|
| `frontend/views/*.tsx` | `frontend/src/features/*/pages/*.tsx` |
| `frontend/components/*.tsx` | `frontend/src/shared/components/*.tsx` 또는 `frontend/src/features/*/components/*.tsx` |
| `frontend/stores/*.ts` | `frontend/src/shared/stores/*.ts` |
| `frontend/hooks/*.ts` | `frontend/src/shared/hooks/*.ts` |
| `frontend/App.tsx` | `frontend/src/app/App.tsx` |
| `frontend/index.tsx` | `frontend/src/app/index.tsx` |
| `frontend/package-lock.json` | 삭제 (npm → pnpm 전환) |

---

## 6. 테스트 계획

### 6.1 백엔드 테스트

| 테스트 유형 | 도구 | 목표 |
|------------|------|------|
| 단위 테스트 | JUnit 5 + MockK | 70% (초기), 80% (최종) |
| 통합 테스트 | Spring Boot Test | 주요 API 엔드포인트 |
| 정적 분석 | ktlint, detekt | 0 violations |
| 아키텍처 테스트 | ArchUnit | 도메인 간 의존성 검증 |

**테스트 우선순위:**
1. `domain-order` - Order Aggregate Root
2. `domain-inventory` - Stock 관리 로직
3. `application` - 주요 Use Cases

### 6.2 프론트엔드 테스트

| 테스트 유형 | 도구 | 목표 |
|------------|------|------|
| 린트 | ESLint | 0 errors, 0 warnings |
| 타입 체크 | TypeScript | 0 errors |
| 유닛 테스트 | Vitest (추후 도입) | 70% 커버리지 |
| E2E | Playwright (추후 도입) | Critical paths |

### 6.3 CI 검증

| 검증 항목 | 방법 |
|----------|------|
| 백엔드 CI 동작 | PR 생성 후 워크플로우 실행 확인 |
| 프론트엔드 CI 동작 | PR 생성 후 워크플로우 실행 확인 |
| Path 필터링 | 백엔드만 변경 시 프론트엔드 CI 미실행 확인 |
| OpenAPI 생성 | 백엔드 변경 후 타입 파일 자동 업데이트 확인 |

---

## 7. 리스크 및 대응

### 7.1 리스크 매트릭스

| 리스크 | 확률 | 영향 | 대응 |
|--------|------|------|------|
| **Java 21 라이브러리 호환성** | 중 | 고 | 업그레이드 전 의존성 호환성 검증, 문제 시 17 롤백 |
| **Virtual Threads ThreadLocal 이슈** | 중 | 중 | 프로파일별 활성화 (`prod`만), 모니터링 |
| **프론트엔드 구조 변경 시 import 에러** | 고 | 저 | IDE 자동 수정, ESLint auto-fix |
| **pnpm 호환성** | 저 | 저 | `.npmrc`에 `shamefully-hoist=true` |
| **CI 설정 오류** | 중 | 저 | 로컬 `act` 테스트, 단계별 검증 |

### 7.2 롤백 계획

| 단계 | 롤백 방법 |
|------|----------|
| Phase 1 (CI) | 워크플로우 파일 삭제 |
| Phase 2 (프론트엔드) | Git revert, npm으로 복귀 |
| Phase 3 (Java 21) | gradle.properties에서 javaVersion=17 |
| Phase 4 (OpenAPI) | 워크플로우 비활성화 |

---

## 8. 일정 및 마일스톤

### 타임라인

```
Week 1: ████████ Phase 1a - CI/CD 워크플로우 (backend-ci, frontend-ci)
Week 2: ████████ Phase 1b - 품질 도구 (ktlint, detekt, jacoco)
Week 3: ████████ Phase 2 - 프론트엔드 구조 개편 (pnpm, features/, TanStack Query)
Week 4: ████████ Phase 3 - 백엔드 업그레이드 (Java 21, Virtual Threads, CORS, 로깅)
Week 5: ████████ Phase 4 + 5 - OpenAPI 파이프라인 + Docker 정비
```

### 마일스톤

| 마일스톤 | 완료 조건 | 목표 주차 |
|----------|----------|----------|
| **M1: CI 동작** | 백엔드/프론트엔드 CI가 PR에서 자동 실행 | Week 2 |
| **M2: 프론트엔드 현대화** | pnpm + src/features/ 구조 + TanStack Query | Week 3 |
| **M3: 백엔드 현대화** | Java 21 + Virtual Threads 활성화 | Week 4 |
| **M4: 자동화 완성** | OpenAPI → TypeScript 파이프라인 동작 | Week 5 |

---

## 부록: 명시적 제외 항목

다음 항목은 이슈 요구사항에 포함되어 있으나 **구현하지 않습니다**:

| 항목 | 제외 사유 |
|------|----------|
| **Kotlin → Java 전환** | 기존 90개 Kotlin 파일 재작성 비용, 기존 투자 손실 |
| **17개 → 4개 모듈 축소** | DDD 경계 붕괴, 9개 BC 중 7개 매핑 불가 |
| **MySQL → PostgreSQL** | ROI 낮음, MongoDB가 이미 JSONB 대안 제공 |
| **React 19 → 18 다운그레이드** | 정당성 불충분, 19가 이미 stable |

---

**문서 작성자**: Claude (Consensus of Planner + Architect + Critic)
**검토 필요**: 프로젝트 관리자

---

*Plan Version: 2.0*
*Status: Ready for Review*
