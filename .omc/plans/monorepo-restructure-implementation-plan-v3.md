# Monorepo 구조 생성 구현 계획

> **Version**: 3.0 (Consensus Plan)
> **Date**: 2024
> **Status**: Final - Ralplan Consensus Achieved
> **Reviews**: Explore + Architect + Critic Review Incorporated

---

## Executive Summary

이 계획은 GitHub 이슈 요구사항을 분석하고, 현재 코드베이스와 비교하여 작성된 구현 계획입니다.

### 핵심 결정 사항 (Decision Log)

| 항목 | 이슈 요구사항 | 현재 상태 | 결정 | 근거 |
|------|--------------|----------|------|------|
| 백엔드 언어 | Java 21 | Kotlin 1.9 | **DECISION NEEDED** | 156개 파일 재작성 vs 요구사항 준수 |
| 모듈 구조 | order/inventory/payment/shared | 9 DDD 모듈 | **DECISION NEEDED** | 모듈 통합 vs DDD 유지 |
| QA 도구 | Checkstyle, SpotBugs, JaCoCo | 없음 | Java 선택 시 적용 | 언어 의존 |
| 데이터베이스 | PostgreSQL 16 | MySQL 8.0 | **PostgreSQL 전환** | 명시적 요구사항 |
| 프론트엔드 | React 18 + pnpm | React 19 + npm | **React 19 유지 + pnpm** | 다운그레이드 불필요 |
| 상태관리 | TanStack Query + Zustand | Zustand only | **TanStack Query 추가** | 요구사항 준수 |
| CI/CD | Path-filtered Actions | 없음 | **신규 구축** | 요구사항 준수 |

---

## 1. 현재 상태 분석

### 1.1 백엔드 구조

```
backend/
├── api/                    # REST API 진입점
├── application/            # 애플리케이션 서비스
├── core/
│   ├── core-domain/        # 공유 도메인
│   └── core-infra/         # 공유 인프라
├── domain/                 # 9개 바운디드 컨텍스트
│   ├── domain-automation/
│   ├── domain-catalog/
│   ├── domain-channel/
│   ├── domain-claim/
│   ├── domain-identity/
│   ├── domain-inventory/
│   ├── domain-order/
│   ├── domain-settlement/
│   └── domain-strategy/
└── infrastructure/
    ├── infra-external/
    ├── infra-mongo/
    ├── infra-mysql/
    └── infra-redis/
```

**현재 기술 스택:**
- Kotlin 1.9.22
- Spring Boot 3.2.2
- JDK 17
- Gradle 8.5 (Kotlin DSL)
- MySQL 8.0 + MongoDB 7.0 + Redis 7

### 1.2 프론트엔드 구조

```
frontend/
├── App.tsx
├── index.tsx
├── components/     # 22개 컴포넌트
├── views/          # 13개 뷰
├── stores/         # 8개 Zustand 스토어
├── services/       # API 서비스
├── hooks/
└── i18n/           # 다국어 (en/ko)
```

**현재 기술 스택:**
- React 19.2.3
- TypeScript 5.8
- Vite 6.2
- Zustand 4.5.7
- npm (not pnpm)

### 1.3 테스트 현황

| 영역 | 현재 상태 |
|------|----------|
| 백엔드 테스트 | **0%** - 테스트 파일 없음 |
| 프론트엔드 테스트 | **0%** - 테스트 파일 없음 |
| CI/CD | **없음** |

---

## 2. 의사결정 필요 항목

### 2.1 백엔드 언어: Java vs Kotlin

#### Option A: Java 21 전환 (이슈 요구사항 준수)

**작업량:**
- 156개 Kotlin 파일 Java 변환
- 테스트 프레임워크 변경 (Kotest → JUnit 5)
- 빌드 설정 수정

**장점:**
- 요구사항 100% 준수
- Checkstyle, SpotBugs, JaCoCo 네이티브 지원
- 더 넓은 개발자 풀

**단점:**
- 4-6주 추가 작업
- 버그 유입 위험
- 기존 코드 품질 손실 가능

#### Option B: Kotlin 유지 + JDK 21 (실용적 대안)

**작업량:**
- gradle.properties에서 jvmTarget=21로 변경
- Virtual Threads 활성화

**장점:**
- 최소 변경으로 안정성 유지
- 기존 DDD 아키텍처 보존
- 즉시 실행 가능

**단점:**
- 요구사항 불일치
- ktlint/detekt/Kover 사용 필요

**권장:** 이슈 작성자에게 확인 요청. Kotlin이 허용되면 Option B 선택.

---

### 2.2 모듈 구조: 4 vs 9

#### Option A: 4모듈로 통합 (이슈 요구사항 준수)

```
backend/modules/
├── order/       # domain-order + domain-claim + domain-settlement
├── inventory/   # domain-inventory + domain-channel + domain-automation
├── payment/     # NEW (신규 개발)
└── shared/      # core-domain + core-infra + domain-identity + domain-catalog + domain-strategy
```

**작업량:**
- 모든 패키지 경로 변경
- import 문 전체 수정
- 빌드 스크립트 재작성

#### Option B: 9모듈 유지 (DDD 보존)

```
backend/
├── modules/
│   ├── order/         # 기존 domain-order
│   ├── inventory/     # 기존 domain-inventory
│   ├── payment/       # 기존 domain-settlement 리네임
│   └── shared/        # 기존 core-domain + core-infra
├── extended-modules/  # 추가 컨텍스트
│   ├── catalog/
│   ├── channel/
│   ├── claim/
│   ├── identity/
│   ├── automation/
│   └── strategy/
└── infrastructure/
```

**권장:** Option B - 기존 구조 최대한 보존하면서 요구사항 형식 준수

---

## 3. 구현 계획

### Phase 0: 환경 준비 (Day 1)

#### 작업 목록
- [ ] JDK 21 설치 및 JAVA_HOME 설정
- [ ] pnpm 설치 (`npm install -g pnpm`)
- [ ] 기존 코드 PostgreSQL 호환성 검사

#### 검증 명령어
```bash
# JDK 21 확인
java -version

# pnpm 확인
pnpm --version

# MySQL 특화 문법 검색
grep -r "ENGINE=InnoDB\|ON UPDATE CURRENT_TIMESTAMP\|utf8mb4" backend/
```

---

### Phase 1: 데이터베이스 전환 (Day 2-3)

#### 1.1 docker-compose.yml 수정

**변경 사항:**
- MySQL 서비스 제거
- PostgreSQL 16 서비스 추가

```yaml
# 변경 전
services:
  mysql:
    image: mysql:8.0
    ...

# 변경 후
services:
  postgres:
    image: postgres:16
    container_name: oms-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-oms}
      POSTGRES_USER: ${POSTGRES_USER:-oms}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-oms_password}
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U oms -d oms"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - oms-network
```

#### 1.2 Flyway 마이그레이션 PostgreSQL 변환

**변경 대상 파일:**
- `V1__init_channel_context.sql`
- `V2__init_identity_context.sql`
- `V3__init_catalog_context.sql`
- `V4__init_inventory_context.sql`
- `V5__init_order_context.sql`
- `V6__init_settlement_context.sql`

**MySQL → PostgreSQL 변환 규칙:**

| MySQL | PostgreSQL |
|-------|------------|
| `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4` | 제거 (기본값) |
| `ON UPDATE CURRENT_TIMESTAMP` | 트리거로 대체 |
| `INT NOT NULL DEFAULT 0` | `INTEGER NOT NULL DEFAULT 0` |
| `TEXT` | `TEXT` (동일) |
| `VARCHAR(n)` | `VARCHAR(n)` (동일) |

**예시 변환 (V1__init_channel_context.sql):**

```sql
-- PostgreSQL Version
CREATE TABLE channels (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    additional_config TEXT,
    api_endpoint VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_channel_company ON channels(company_id);
CREATE INDEX idx_channel_type ON channels(type);
CREATE INDEX idx_channel_status ON channels(status);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_channels_updated_at
    BEFORE UPDATE ON channels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

#### 1.3 application.yml 수정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/oms
    username: oms
    password: oms_password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

#### 1.4 build.gradle.kts 의존성 추가

```kotlin
dependencies {
    runtimeOnly("org.postgresql:postgresql")
    // MySQL 의존성 제거
    // runtimeOnly("com.mysql:mysql-connector-j")
}
```

---

### Phase 2: 백엔드 도구 강화 (Day 4-5)

#### 2.1 JDK 21 + Virtual Threads

**파일:** `backend/gradle.properties`
```properties
# 변경
javaVersion=21
kotlinVersion=1.9.22
springBootVersion=3.2.2
```

**파일:** `backend/build.gradle.kts`
```kotlin
subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"  // 17 → 21
        }
    }
}
```

**파일:** `backend/api/src/main/resources/application.yml`
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

#### 2.2 코드 품질 도구 (Kotlin 유지 시)

**파일:** `backend/build.gradle.kts`
```kotlin
plugins {
    // 기존 플러그인
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
    kotlin("plugin.jpa") version "1.9.22" apply false
    id("org.springframework.boot") version "3.2.2" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false

    // 추가 플러그인
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.5" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    kover {
        reports {
            verify {
                rule {
                    minBound(80)  // 80% 커버리지 요구
                }
            }
        }
    }
}
```

#### 2.3 코드 품질 도구 (Java 전환 시)

**파일:** `backend/build.gradle.kts`
```kotlin
plugins {
    java
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.7"
    id("jacoco")
}

subprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "jacoco")

    checkstyle {
        toolVersion = "10.12.4"
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    }

    spotbugs {
        toolVersion = "4.8.3"
        effort = com.github.spotbugs.snom.Effort.MAX
        reportLevel = com.github.spotbugs.snom.Confidence.LOW
    }

    jacoco {
        toolVersion = "0.8.11"
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()  // 80%
                }
            }
        }
    }
}
```

#### 2.4 CORS 설정

**새 파일:** `backend/api/src/main/kotlin/com/oms/api/config/WebConfig.kt`
```kotlin
package com.oms.api.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000"   // Production
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
```

#### 2.5 JSON 로깅 (Logback)

**새 파일:** `backend/api/src/main/resources/logback-spring.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- Console Appender for Development -->
    <springProfile name="local,default">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <!-- JSON Appender for Production/Docker -->
    <springProfile name="docker,prod">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>
</configuration>
```

#### 2.6 OpenAPI 생성

**파일:** `backend/api/build.gradle.kts` 추가
```kotlin
plugins {
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("${buildDir}/openapi"))
    outputFileName.set("openapi.json")
}
```

#### 2.7 .env.example

**새 파일:** `backend/.env.example`
```bash
# Database (PostgreSQL)
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

# JWT
JWT_SECRET=your-super-secret-key-change-in-production-min-256-bits
JWT_EXPIRATION=86400000

# Spring
SPRING_PROFILES_ACTIVE=local
```

---

### Phase 3: 프론트엔드 재구성 (Day 6-8)

#### 3.1 패키지 관리자 전환 (npm → pnpm)

```bash
cd frontend
rm -rf node_modules package-lock.json
pnpm install
```

#### 3.2 디렉토리 구조 변경

**목표 구조:**
```
frontend/
├── src/
│   ├── features/
│   │   ├── order/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── stores/
│   │   │   ├── OrderView.tsx
│   │   │   ├── ClaimsView.tsx
│   │   │   ├── ShippingView.tsx
│   │   │   └── index.ts
│   │   ├── inventory/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── stores/
│   │   │   ├── InventoryView.tsx
│   │   │   ├── ProductView.tsx
│   │   │   └── index.ts
│   │   └── payment/
│   │       ├── components/
│   │       ├── hooks/
│   │       ├── stores/
│   │       ├── SettlementView.tsx
│   │       └── index.ts
│   ├── shared/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── queryClient.ts
│   │   │   └── types.ts        # OpenAPI 생성
│   │   ├── components/
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── ErrorBoundary/
│   │   │   └── ...
│   │   ├── hooks/
│   │   ├── utils/
│   │   └── stores/
│   │       └── useAppStore.ts
│   ├── App.tsx
│   └── main.tsx
├── index.html
├── vite.config.ts
├── tsconfig.json
├── eslint.config.js
├── .prettierrc
├── .env.example
├── pnpm-lock.yaml
└── package.json
```

#### 3.3 파일 이동 매핑

| 원본 | 대상 | 분류 기준 |
|------|------|----------|
| `views/OrderView.tsx` | `src/features/order/OrderView.tsx` | 주문 도메인 |
| `views/ClaimsView.tsx` | `src/features/order/ClaimsView.tsx` | 주문 관련 |
| `views/ShippingView.tsx` | `src/features/order/ShippingView.tsx` | 주문 관련 |
| `views/InventoryView.tsx` | `src/features/inventory/InventoryView.tsx` | 재고 도메인 |
| `views/ProductView.tsx` | `src/features/inventory/ProductView.tsx` | 재고 관련 |
| `views/SettlementView.tsx` | `src/features/payment/SettlementView.tsx` | 결제 도메인 |
| `views/DashboardView.tsx` | `src/shared/views/DashboardView.tsx` | 공용 |
| `views/ChannelView.tsx` | `src/shared/views/ChannelView.tsx` | 공용 |
| `views/WarehouseView.tsx` | `src/shared/views/WarehouseView.tsx` | 공용 |
| `views/AutomationView.tsx` | `src/shared/views/AutomationView.tsx` | 공용 |
| `views/StrategyView.tsx` | `src/shared/views/StrategyView.tsx` | 공용 |
| `views/AnalyticsView.tsx` | `src/shared/views/AnalyticsView.tsx` | 공용 |
| `views/SettingsView.tsx` | `src/shared/views/SettingsView.tsx` | 공용 |
| `stores/useOrderStore.ts` | `src/features/order/stores/useOrderStore.ts` | |
| `stores/useInventoryStore.ts` | `src/features/inventory/stores/useInventoryStore.ts` | |
| `stores/useSettlementStore.ts` | `src/features/payment/stores/useSettlementStore.ts` | |
| `stores/useAppStore.ts` | `src/shared/stores/useAppStore.ts` | 공용 |
| `components/Header.tsx` | `src/shared/components/Header.tsx` | 공용 |
| `components/Sidebar.tsx` | `src/shared/components/Sidebar.tsx` | 공용 |
| `components/OrderDetailModal.tsx` | `src/features/order/components/OrderDetailModal.tsx` | |
| `components/NewOrderModal.tsx` | `src/features/order/components/NewOrderModal.tsx` | |
| `components/StockAdjustmentModal.tsx` | `src/features/inventory/components/StockAdjustmentModal.tsx` | |
| `components/SettlementDetailModal.tsx` | `src/features/payment/components/SettlementDetailModal.tsx` | |
| `services/api.ts` | `src/shared/api/client.ts` | |
| `hooks/*.ts` | `src/shared/hooks/*.ts` | |

#### 3.4 의존성 업데이트

**파일:** `frontend/package.json`
```json
{
  "name": "oms-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix",
    "format": "prettier --write src",
    "type-check": "tsc --noEmit",
    "test": "vitest",
    "test:coverage": "vitest run --coverage",
    "test:e2e": "playwright test",
    "generate:types": "openapi-typescript ../backend/api/build/openapi/openapi.json -o src/shared/api/types.ts"
  },
  "dependencies": {
    "@tanstack/react-query": "^5.59.0",
    "axios": "^1.13.2",
    "i18next": "^23.16.8",
    "i18next-browser-languagedetector": "^7.2.2",
    "lucide-react": "^0.562.0",
    "react": "^19.2.3",
    "react-dom": "^19.2.3",
    "react-i18next": "^14.1.3",
    "recharts": "^3.6.0",
    "zustand": "^4.5.7"
  },
  "devDependencies": {
    "@playwright/test": "^1.49.1",
    "@testing-library/react": "^16.1.0",
    "@types/node": "^22.19.7",
    "@types/react": "^19.2.8",
    "@types/react-dom": "^19.2.3",
    "@typescript-eslint/eslint-plugin": "^8.18.0",
    "@typescript-eslint/parser": "^8.18.0",
    "@vitejs/plugin-react": "^5.0.0",
    "@vitest/coverage-v8": "^2.1.8",
    "eslint": "^9.16.0",
    "eslint-plugin-react": "^7.37.2",
    "eslint-plugin-react-hooks": "^5.1.0",
    "openapi-typescript": "^7.4.0",
    "prettier": "^3.4.2",
    "typescript": "~5.8.2",
    "vite": "^6.2.0",
    "vitest": "^2.1.8"
  }
}
```

#### 3.5 ESLint 설정 (Flat Config)

**새 파일:** `frontend/eslint.config.js`
```javascript
import js from '@eslint/js';
import tseslint from '@typescript-eslint/eslint-plugin';
import tsparser from '@typescript-eslint/parser';
import reactPlugin from 'eslint-plugin-react';
import reactHooksPlugin from 'eslint-plugin-react-hooks';

export default [
  js.configs.recommended,
  {
    files: ['src/**/*.{ts,tsx}'],
    languageOptions: {
      parser: tsparser,
      parserOptions: {
        ecmaVersion: 2022,
        sourceType: 'module',
        ecmaFeatures: { jsx: true },
      },
    },
    plugins: {
      '@typescript-eslint': tseslint,
      'react': reactPlugin,
      'react-hooks': reactHooksPlugin,
    },
    rules: {
      // Features 간 직접 import 금지
      'no-restricted-imports': ['error', {
        patterns: [
          {
            group: ['@/features/order/*', '!@/features/order'],
            message: 'features/order 내부에서 직접 import 금지. 해당 feature의 index.ts를 통해 export하세요.',
          },
          {
            group: ['@/features/inventory/*', '!@/features/inventory'],
            message: 'features/inventory 내부에서 직접 import 금지. 해당 feature의 index.ts를 통해 export하세요.',
          },
          {
            group: ['@/features/payment/*', '!@/features/payment'],
            message: 'features/payment 내부에서 직접 import 금지. 해당 feature의 index.ts를 통해 export하세요.',
          },
        ],
      }],
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
    },
    settings: {
      react: { version: 'detect' },
    },
  },
];
```

#### 3.6 Vite 설정 업데이트

**파일:** `frontend/vite.config.ts`
```typescript
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: true,
    },
  };
});
```

#### 3.7 TypeScript 설정

**파일:** `frontend/tsconfig.json`
```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

#### 3.8 TanStack Query 설정

**새 파일:** `frontend/src/shared/api/queryClient.ts`
```typescript
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      gcTime: 1000 * 60 * 30,   // 30 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
```

#### 3.9 .env.example

**새 파일:** `frontend/.env.example`
```bash
# API
VITE_API_URL=http://localhost:8080

# Environment
VITE_APP_ENV=development
```

---

### Phase 4: CI/CD 구축 (Day 9-10)

#### 4.1 Backend CI

**새 파일:** `.github/workflows/backend-ci.yml`
```yaml
name: Backend CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'backend/**'
      - 'docker-compose.yml'
      - '.github/workflows/backend-ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'backend/**'
      - 'docker-compose.yml'
      - '.github/workflows/backend-ci.yml'

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true'

jobs:
  lint:
    name: Lint (ktlint + detekt)
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Run ktlint
        working-directory: backend
        run: ./gradlew ktlintCheck

      - name: Run detekt
        working-directory: backend
        run: ./gradlew detekt

  test:
    name: Unit Test
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Run tests
        working-directory: backend
        run: ./gradlew test

  integration:
    name: Integration Test
    runs-on: ubuntu-latest
    needs: test
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: oms_test
          POSTGRES_USER: oms
          POSTGRES_PASSWORD: oms_password
        ports:
          - 5432:5432
        options: >-
          --health-cmd="pg_isready -U oms -d oms_test"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
      mongodb:
        image: mongo:7.0
        env:
          MONGO_INITDB_ROOT_USERNAME: oms
          MONGO_INITDB_ROOT_PASSWORD: oms_password
        ports:
          - 27017:27017
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Run integration tests
        working-directory: backend
        run: ./gradlew integrationTest
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/oms_test
          SPRING_DATASOURCE_USERNAME: oms
          SPRING_DATASOURCE_PASSWORD: oms_password

  arch:
    name: Architecture Test (ArchUnit)
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Run architecture tests
        working-directory: backend
        run: ./gradlew archTest

  coverage:
    name: Coverage Verification (80%)
    runs-on: ubuntu-latest
    needs: [test, integration]
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Verify coverage (80% minimum)
        working-directory: backend
        run: ./gradlew koverVerify

      - name: Upload coverage report
        uses: codecov/codecov-action@v4
        with:
          directory: backend/build/reports/kover
          fail_ci_if_error: true

  build:
    name: Build + OpenAPI
    runs-on: ubuntu-latest
    needs: coverage
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: 'gradle'

      - name: Build
        working-directory: backend
        run: ./gradlew build -x test

      - name: Generate OpenAPI spec
        working-directory: backend
        run: ./gradlew generateOpenApiDocs

      - name: Upload OpenAPI artifact
        uses: actions/upload-artifact@v4
        with:
          name: openapi-spec
          path: backend/api/build/openapi/openapi.json
          retention-days: 7
```

#### 4.2 Frontend CI

**새 파일:** `.github/workflows/frontend-ci.yml`
```yaml
name: Frontend CI

on:
  push:
    branches: [main, develop]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-ci.yml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'frontend/**'
      - '.github/workflows/frontend-ci.yml'

env:
  NODE_VERSION: '20'

jobs:
  setup:
    name: Setup + Type Generation
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Download OpenAPI spec
        uses: actions/download-artifact@v4
        with:
          name: openapi-spec
          path: frontend/
        continue-on-error: true  # 첫 실행 시 artifact 없을 수 있음

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Generate types from OpenAPI
        working-directory: frontend
        run: |
          if [ -f openapi.json ]; then
            pnpm generate:types
          else
            echo "OpenAPI spec not found, skipping type generation"
          fi

      - name: Cache node_modules
        uses: actions/cache@v4
        with:
          path: frontend/node_modules
          key: ${{ runner.os }}-pnpm-${{ hashFiles('frontend/pnpm-lock.yaml') }}

  lint:
    name: ESLint
    runs-on: ubuntu-latest
    needs: setup
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Run ESLint
        working-directory: frontend
        run: pnpm lint

  type-check:
    name: Type Check
    runs-on: ubuntu-latest
    needs: setup
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Type Check
        working-directory: frontend
        run: pnpm type-check

  build:
    name: Build
    runs-on: ubuntu-latest
    needs: [lint, type-check]
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Build
        working-directory: frontend
        run: pnpm build

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist
          retention-days: 7

  test:
    name: Unit Test (70% Coverage)
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Run tests with coverage
        working-directory: frontend
        run: pnpm test:coverage

      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          directory: frontend/coverage
          fail_ci_if_error: true

  e2e:
    name: E2E Tests
    runs-on: ubuntu-latest
    needs: build
    steps:
      - uses: actions/checkout@v4

      - uses: pnpm/action-setup@v2
        with:
          version: 9

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'pnpm'
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Install Playwright browsers
        working-directory: frontend
        run: pnpm exec playwright install --with-deps

      - name: Download build
        uses: actions/download-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist

      - name: Run E2E tests
        working-directory: frontend
        run: pnpm test:e2e

      - name: Upload Playwright report
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: playwright-report
          path: frontend/playwright-report
          retention-days: 7
```

---

### Phase 5: 문서화 (Day 11)

#### 5.1 루트 README.md

**파일:** `README.md`
```markdown
# Order Management System (OMS)

글로벌 커머스를 위한 통합 주문 관리 시스템

## 기술 스택

### Backend
- Kotlin 1.9 + Spring Boot 3.2
- JDK 21 (Virtual Threads 활성화)
- PostgreSQL 16 + MongoDB 7.0 + Redis 7

### Frontend
- React 19 + TypeScript 5.8
- Vite 6 + pnpm
- TanStack Query (서버 상태) + Zustand (클라이언트 상태)

## 로컬 개발 환경

### 요구사항
- JDK 21
- Node.js 20+
- pnpm 9+
- Docker + Docker Compose

### 실행 방법

\`\`\`bash
# 1. 인프라 실행
docker compose up -d postgres mongodb redis

# 2. 백엔드 실행
cd backend
./gradlew :api:bootRun

# 3. 프론트엔드 실행
cd frontend
pnpm install
pnpm dev
\`\`\`

### 접속 URL
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 프로젝트 구조

\`\`\`
.
├── backend/           # Kotlin/Spring Boot 백엔드
├── frontend/          # React/TypeScript 프론트엔드
├── docker/            # Docker 초기화 스크립트
├── .github/workflows/ # CI/CD 파이프라인
└── docker-compose.yml # 로컬 개발 환경
\`\`\`

## CI/CD

- **Backend CI**: Lint → Test → Integration → Arch → Coverage (80%) → Build → OpenAPI
- **Frontend CI**: Setup → Lint → Type Check → Build → Test (70%) → E2E

상세 내용은 각 디렉토리의 README를 참조하세요.
```

#### 5.2 Backend README.md

**새 파일:** `backend/README.md`
```markdown
# OMS Backend

## 아키텍처

DDD(Domain-Driven Design) 기반 멀티모듈 구조

### 모듈 구조

\`\`\`
backend/
├── api/              # REST API 엔드포인트
├── application/      # 애플리케이션 서비스
├── core/
│   ├── core-domain/  # 공유 도메인 (BaseEntity, Events)
│   └── core-infra/   # 공유 인프라 (Config, Utils)
├── domain/           # 바운디드 컨텍스트
│   ├── domain-order/      # 주문 관리
│   ├── domain-inventory/  # 재고 관리
│   ├── domain-settlement/ # 정산 (결제)
│   └── ...
└── infrastructure/   # 외부 시스템 연동
    ├── infra-mysql/  # PostgreSQL JPA
    ├── infra-mongo/  # MongoDB
    └── infra-redis/  # Redis Cache
\`\`\`

### 개발 명령어

\`\`\`bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 코드 품질 검사
./gradlew ktlintCheck detekt

# 커버리지 확인
./gradlew koverReport

# 애플리케이션 실행
./gradlew :api:bootRun
\`\`\`

### API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
```

#### 5.3 Frontend README.md

**새 파일:** `frontend/README.md`
```markdown
# OMS Frontend

## 아키텍처

Feature-based 모듈 구조

### 디렉토리 구조

\`\`\`
src/
├── features/           # 기능별 모듈 (상호 직접 import 금지)
│   ├── order/          # 주문 관리
│   ├── inventory/      # 재고 관리
│   └── payment/        # 결제/정산
├── shared/             # 공유 모듈
│   ├── api/            # API 클라이언트, OpenAPI 타입
│   ├── components/     # 공용 컴포넌트
│   ├── hooks/          # 공용 훅
│   └── utils/          # 유틸리티
└── App.tsx
\`\`\`

### 개발 명령어

\`\`\`bash
# 의존성 설치
pnpm install

# 개발 서버
pnpm dev

# 빌드
pnpm build

# 린트
pnpm lint

# 타입 체크
pnpm type-check

# 테스트
pnpm test

# E2E 테스트
pnpm test:e2e

# OpenAPI 타입 생성
pnpm generate:types
\`\`\`

### 상태 관리
- **서버 상태**: TanStack Query
- **클라이언트 상태**: Zustand

### Import 규칙
- `@/features/order/*` 등 feature 내부 파일 직접 import 금지
- 반드시 `@/features/order` (index.ts)를 통해 export
```

---

## 4. 변경 파일 목록 (Summary)

### 신규 생성 (35+ 파일)

```
.github/workflows/backend-ci.yml
.github/workflows/frontend-ci.yml
backend/.env.example
backend/README.md
backend/config/detekt/detekt.yml
backend/api/src/main/kotlin/com/oms/api/config/WebConfig.kt
backend/api/src/main/resources/logback-spring.xml
docker/postgres/init/.gitkeep
frontend/.env.example
frontend/README.md
frontend/eslint.config.js
frontend/.prettierrc
frontend/vitest.config.ts
frontend/playwright.config.ts
frontend/pnpm-lock.yaml
frontend/src/main.tsx
frontend/src/App.tsx
frontend/src/features/order/index.ts
frontend/src/features/order/OrderView.tsx
frontend/src/features/inventory/index.ts
frontend/src/features/inventory/InventoryView.tsx
frontend/src/features/payment/index.ts
frontend/src/features/payment/SettlementView.tsx
frontend/src/shared/api/client.ts
frontend/src/shared/api/queryClient.ts
frontend/src/shared/api/types.ts (generated)
frontend/src/shared/components/Header.tsx
frontend/src/shared/components/Sidebar.tsx
frontend/src/shared/stores/useAppStore.ts
... (컴포넌트 이동 파일들)
```

### 수정 (15+ 파일)

```
README.md
docker-compose.yml
.gitignore
backend/build.gradle.kts
backend/gradle.properties
backend/api/build.gradle.kts
backend/api/src/main/resources/application.yml
backend/api/src/main/resources/db/migration/V1__init_channel_context.sql
backend/api/src/main/resources/db/migration/V2__init_identity_context.sql
backend/api/src/main/resources/db/migration/V3__init_catalog_context.sql
backend/api/src/main/resources/db/migration/V4__init_inventory_context.sql
backend/api/src/main/resources/db/migration/V5__init_order_context.sql
backend/api/src/main/resources/db/migration/V6__init_settlement_context.sql
frontend/package.json
frontend/vite.config.ts
frontend/tsconfig.json
frontend/index.html
```

### 삭제 (정리)

```
frontend/package-lock.json (npm → pnpm)
docker/mysql/ (MySQL → PostgreSQL)
```

---

## 5. 테스트 계획

### 5.1 백엔드 테스트 전략

| 레벨 | 도구 | 대상 | 커버리지 목표 |
|------|------|------|--------------|
| Unit | Kotest + MockK | Domain, Service | 80% |
| Integration | Testcontainers | Repository, API | 포함 |
| Architecture | ArchUnit | 모듈 의존성 | 100% 규칙 통과 |

### 5.2 프론트엔드 테스트 전략

| 레벨 | 도구 | 대상 | 커버리지 목표 |
|------|------|------|--------------|
| Unit | Vitest + RTL | Components, Hooks | 70% |
| Integration | Vitest | Feature modules | 포함 |
| E2E | Playwright | 주요 사용자 흐름 | 핵심 시나리오 |

### 5.3 테스트 우선순위

**Phase 1 (필수):**
1. 공유 컴포넌트 단위 테스트
2. API 클라이언트 테스트
3. 주요 비즈니스 로직 테스트

**Phase 2 (권장):**
1. Feature 컴포넌트 테스트
2. E2E 테스트 시나리오

---

## 6. 위험도 평가

| 변경 영역 | 위험도 | 영향 범위 | 완화 전략 |
|----------|--------|----------|----------|
| MySQL → PostgreSQL | **HIGH** | 전체 데이터 레이어 | 마이그레이션 스크립트 검증, 단계적 전환 |
| Frontend 구조 변경 | **MEDIUM** | 40+ 파일 이동 | import 경로 자동 수정, 린트 검증 |
| JDK 17 → 21 | **LOW** | 런타임 환경 | Gradle 빌드 테스트 |
| CI/CD 구축 | **LOW** | 신규 추가 | 점진적 활성화 |
| 커버리지 0% → 80% | **CRITICAL** | 테스트 작성 필요 | 별도 PR로 분리, 단계적 향상 |

---

## 7. 일정 추정

| Phase | 기간 | 작업 |
|-------|------|------|
| Phase 0 | Day 1 | 환경 준비, 검증 |
| Phase 1 | Day 2-3 | PostgreSQL 전환 |
| Phase 2 | Day 4-5 | 백엔드 도구 강화 |
| Phase 3 | Day 6-8 | 프론트엔드 재구성 |
| Phase 4 | Day 9-10 | CI/CD 구축 |
| Phase 5 | Day 11 | 문서화 |
| **테스트** | Week 2-4 | 커버리지 구축 (별도 트랙) |

**총 예상 기간**: 2주 (핵심 인프라) + 2주 (테스트 구축)

---

## 8. 의사결정 대기 항목

다음 항목은 이슈 작성자 또는 팀 확인이 필요합니다:

1. **Java vs Kotlin**: 이슈에 "Java 21"로 명시되어 있으나, 현재 156개 Kotlin 파일이 존재합니다. Kotlin 유지가 허용되는지 확인 필요.

2. **모듈 구조**: 이슈에 4모듈(order/inventory/payment/shared) 요청이 있으나, 현재 9개 DDD 바운디드 컨텍스트가 있습니다. 통합 vs 유지 결정 필요.

3. **React 버전**: 이슈에 React 18 명시되어 있으나, 현재 React 19 사용 중. 다운그레이드 필요한지 확인.

---

*Plan Version: 3.0 (Ralplan Consensus)*
*Explore Analysis: Complete*
*Architect Review: Approved*
*Critic Review: Issues Addressed*
*Status: Ready for Implementation (Pending Decision Items)*
