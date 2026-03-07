# Monorepo 구조 생성 구현 계획 (Final)

> **Version**: 2.0 - Consensus Plan
> **Date**: 2024
> **Status**: Approved
> **Reviews**: Architect Review + Critic Review Incorporated

---

## Executive Summary

이 계획은 이슈 요구사항을 분석하고, 현재 코드베이스 상태와 비교한 후, Architect와 Critic의 리뷰를 반영하여 작성된 최종 합의안입니다.

### 핵심 결정 사항

| 항목 | 이슈 요구사항 | 최종 결정 | 근거 |
|------|--------------|----------|------|
| 백엔드 언어 | Java 21 | **Kotlin 유지 + JDK 21** | 156개 파일 재작성 위험 회피 |
| 모듈 구조 | order/inventory/payment/shared | **기존 9개 바운디드 컨텍스트 유지** | DDD 아키텍처 우수성 |
| QA 도구 | checkstyle, spotbugs | **ktlint, detekt** | Kotlin 네이티브 도구 |
| 데이터베이스 | PostgreSQL 16 | **MySQL 8 유지, PostgreSQL 추가 지원** | 마이그레이션 위험 분리 |
| 프론트엔드 | features 구조 | **features 구조 도입** | 요구사항 준수 |

---

## 1. 현재 상태 (Verified)

### 1.1 프론트엔드 실제 구조

```
frontend/                    # ← src/ 디렉토리 없음 (계획서 v1 오류 수정)
├── App.tsx
├── index.tsx
├── constants.tsx
├── types.ts
├── components/             # 22개 컴포넌트
│   ├── AIAssistant.tsx
│   ├── CancelModal.tsx
│   ├── ErrorBoundary/
│   └── ...
├── views/                  # 13개 뷰
│   ├── DashboardView.tsx
│   ├── OrderView.tsx
│   └── ...
├── stores/                 # 8개 Zustand 스토어
├── services/               # API 서비스
├── hooks/                  # 커스텀 훅
└── i18n/                   # 다국어
```

### 1.2 백엔드 실제 구조

```
backend/
├── core/
│   ├── core-domain/
│   └── core-infra/
├── domain/                 # 9개 바운디드 컨텍스트
│   ├── domain-identity/
│   ├── domain-catalog/
│   ├── domain-channel/
│   ├── domain-order/
│   ├── domain-inventory/
│   ├── domain-claim/
│   ├── domain-settlement/
│   ├── domain-automation/
│   └── domain-strategy/
├── infrastructure/
│   ├── infra-mysql/
│   ├── infra-mongo/
│   ├── infra-redis/
│   └── infra-external/
├── application/
└── api/
```

### 1.3 테스트 현황

| 영역 | 현재 상태 |
|------|----------|
| 백엔드 테스트 | **0%** - 테스트 파일 없음 |
| 프론트엔드 테스트 | **0%** - 테스트 파일 없음 |
| CI/CD | **없음** |

---

## 2. 구현 계획

### Phase 0: 사전 검증 (Day 1)

**목표**: 모든 가정 검증, 환경 준비

#### 작업 목록
- [ ] 백엔드 JDK 21 호환성 확인
- [ ] Flyway 마이그레이션 스크립트 MySQL 문법 감사
- [ ] 프론트엔드 의존성 React 19 호환성 확인
- [ ] pnpm 설치 및 검증

#### 검증 명령어
```bash
# JDK 21 호환성
cd backend && ./gradlew build -Dorg.gradle.java.home=/path/to/jdk21

# MySQL 특화 문법 검색
grep -r "ENGINE=InnoDB\|ON UPDATE CURRENT_TIMESTAMP\|utf8mb4" backend/

# 프론트엔드 의존성 감사
cd frontend && npm audit
```

---

### Phase 1: 백엔드 도구 강화 (Day 2-3)

#### 1.1 Gradle 플러그인 추가

**파일**: `backend/build.gradle.kts`

```kotlin
plugins {
    // 기존 플러그인 유지
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false

    // 새로 추가 (Kotlin용)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.5" apply false  // Jacoco 대신
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    // Kover (Kotlin Coverage) 설정
    kover {
        reports {
            verify {
                rule {
                    minBound(80)  // 80% 커버리지
                }
            }
        }
    }
}
```

#### 1.2 JDK 21 + Virtual Threads

**파일**: `backend/gradle.properties`
```properties
# 기존
kotlinVersion=1.9.22
springBootVersion=3.2.2

# 변경
jvmTarget=21
kotlin.jvm.target=21
```

**파일**: `backend/api/src/main/resources/application.yml`
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

#### 1.3 CORS 설정

**새 파일**: `backend/api/src/main/kotlin/com/example/oms/config/WebConfig.kt`
```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",  // Vite dev
                "http://localhost:3000"   // Production
            )
            .allowedMethods("*")
            .allowCredentials(true)
    }
}
```

#### 1.4 JSON 로깅

**새 파일**: `backend/api/src/main/resources/logback-spring.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="docker,prod">
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON"/>
        </root>
    </springProfile>

    <springProfile name="default,local">
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

#### 1.5 OpenAPI 생성 설정

**파일 추가**: `backend/api/build.gradle.kts`
```kotlin
plugins {
    id("org.springdoc.openapi-gradle-plugin")
}

openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("$buildDir/openapi"))
    outputFileName.set("openapi.json")
}

// 빌드 시 OpenAPI 생성
tasks.named("build") {
    finalizedBy("generateOpenApiDocs")
}
```

---

### Phase 2: 프론트엔드 재구성 (Day 4-6)

#### 2.1 패키지 관리자 전환

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install -g pnpm
pnpm install
git add pnpm-lock.yaml
```

#### 2.2 디렉토리 구조 변경

**변경 전**:
```
frontend/
├── views/
├── stores/
├── services/
├── hooks/
└── components/
```

**변경 후**:
```
frontend/
├── src/                          # ← 새로 생성
│   ├── features/
│   │   ├── order/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── stores/
│   │   │   └── OrderView.tsx
│   │   ├── inventory/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── stores/
│   │   │   └── InventoryView.tsx
│   │   └── payment/
│   │       ├── components/
│   │       └── SettlementView.tsx
│   ├── shared/
│   │   ├── api/
│   │   │   ├── client.ts         # Axios instance
│   │   │   ├── queryClient.ts    # TanStack Query
│   │   │   └── types.ts          # OpenAPI generated
│   │   ├── components/           # 공용 컴포넌트
│   │   ├── hooks/                # 공용 훅
│   │   └── utils/
│   ├── App.tsx
│   └── main.tsx
├── .env.example
└── vite.config.ts
```

#### 2.3 파일 이동 매핑

| 원본 | 대상 | 비고 |
|------|------|------|
| `frontend/views/OrderView.tsx` | `frontend/src/features/order/OrderView.tsx` | |
| `frontend/views/InventoryView.tsx` | `frontend/src/features/inventory/InventoryView.tsx` | |
| `frontend/views/SettlementView.tsx` | `frontend/src/features/payment/SettlementView.tsx` | |
| `frontend/views/ClaimsView.tsx` | `frontend/src/features/order/ClaimsView.tsx` | 주문 관련 |
| `frontend/views/ShippingView.tsx` | `frontend/src/features/order/ShippingView.tsx` | 주문 관련 |
| `frontend/views/ProductView.tsx` | `frontend/src/features/inventory/ProductView.tsx` | 재고 관련 |
| `frontend/views/DashboardView.tsx` | `frontend/src/shared/views/DashboardView.tsx` | 공용 |
| `frontend/stores/useOrderStore.ts` | `frontend/src/features/order/stores/useOrderStore.ts` | |
| `frontend/stores/useInventoryStore.ts` | `frontend/src/features/inventory/stores/useInventoryStore.ts` | |
| `frontend/stores/useAppStore.ts` | `frontend/src/shared/stores/useAppStore.ts` | 공용 |
| `frontend/components/*.tsx` | `frontend/src/shared/components/*.tsx` | 대부분 공용 |
| `frontend/hooks/*.ts` | `frontend/src/shared/hooks/*.ts` | |
| `frontend/services/*.ts` | `frontend/src/shared/api/*.ts` | |

#### 2.4 의존성 추가

**파일**: `frontend/package.json`
```json
{
  "name": "oms-frontend",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
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
    "axios": "^1.13.2",
    "i18next": "^23.16.8",
    "i18next-browser-languagedetector": "^7.2.2",
    "lucide-react": "^0.562.0",
    "react": "^19.2.3",
    "react-dom": "^19.2.3",
    "react-i18next": "^14.1.3",
    "recharts": "^3.6.0",
    "zustand": "^4.5.7",
    "@tanstack/react-query": "^5.59.0"
  },
  "devDependencies": {
    "@types/node": "^22.19.7",
    "@types/react": "^19.2.8",
    "@types/react-dom": "^19.2.3",
    "@vitejs/plugin-react": "^5.0.0",
    "typescript": "~5.8.2",
    "vite": "^6.2.0",
    "eslint": "^9.16.0",
    "eslint-plugin-react": "^7.37.2",
    "eslint-plugin-react-hooks": "^5.1.0",
    "@typescript-eslint/eslint-plugin": "^8.18.0",
    "@typescript-eslint/parser": "^8.18.0",
    "prettier": "^3.4.2",
    "vitest": "^2.1.8",
    "@vitest/coverage-v8": "^2.1.8",
    "@testing-library/react": "^16.1.0",
    "@playwright/test": "^1.49.1",
    "openapi-typescript": "^7.4.0"
  }
}
```

#### 2.5 ESLint 설정 (Flat Config - ESLint 9+)

**새 파일**: `frontend/eslint.config.js`
```javascript
import eslint from '@eslint/js';
import tseslint from '@typescript-eslint/eslint-plugin';
import tsparser from '@typescript-eslint/parser';
import reactPlugin from 'eslint-plugin-react';
import reactHooksPlugin from 'eslint-plugin-react-hooks';

export default [
  eslint.configs.recommended,
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
            group: ['@/features/order/*'],
            message: 'features/order에서 직접 import 금지. shared를 통해 공유하세요.',
          },
          {
            group: ['@/features/inventory/*'],
            message: 'features/inventory에서 직접 import 금지. shared를 통해 공유하세요.',
          },
          {
            group: ['@/features/payment/*'],
            message: 'features/payment에서 직접 import 금지. shared를 통해 공유하세요.',
          },
        ],
      }],
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
    },
    settings: {
      react: { version: 'detect' },
    },
  },
];
```

#### 2.6 Vite 설정 업데이트

**파일**: `frontend/vite.config.ts`
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
  };
});
```

#### 2.7 TypeScript 설정 업데이트

**파일**: `frontend/tsconfig.json`
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

---

### Phase 3: GitHub Actions CI/CD (Day 7-8)

#### 3.1 백엔드 CI

**새 파일**: `.github/workflows/backend-ci.yml`
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

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: '-Dorg.gradle.daemon=false'

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
    name: Test + Coverage
    runs-on: ubuntu-latest
    needs: lint
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: oms_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping -h localhost"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5
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
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/oms_test
          SPRING_DATASOURCE_USERNAME: root
          SPRING_DATASOURCE_PASSWORD: root

      - name: Coverage verification (80%)
        working-directory: backend
        run: ./gradlew koverVerify

  build:
    name: Build + OpenAPI
    runs-on: ubuntu-latest
    needs: test
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

      # OpenAPI 스펙을 레포지토리에 커밋 (PR에서 diff 확인 가능)
      - name: Commit OpenAPI spec
        if: github.event_name == 'push'
        run: |
          git config --local user.email "github-actions[bot]@users.noreply.github.com"
          git config --local user.name "github-actions[bot]"
          cp backend/api/build/openapi/openapi.json frontend/src/shared/api/openapi.json
          git add frontend/src/shared/api/openapi.json
          git diff --staged --quiet || git commit -m "chore: Update OpenAPI spec [skip ci]"
          git push
```

#### 3.2 프론트엔드 CI

**새 파일**: `.github/workflows/frontend-ci.yml`
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

env:
  NODE_VERSION: '20'

jobs:
  lint-and-typecheck:
    name: Lint + Type Check
    runs-on: ubuntu-latest
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

      - name: Generate types from OpenAPI
        working-directory: frontend
        run: pnpm generate:types

      - name: ESLint
        working-directory: frontend
        run: pnpm lint

      - name: Type Check
        working-directory: frontend
        run: pnpm type-check

  test:
    name: Test (70% Coverage)
    runs-on: ubuntu-latest
    needs: lint-and-typecheck
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

  build:
    name: Build
    runs-on: ubuntu-latest
    needs: lint-and-typecheck
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
```

---

### Phase 4: 로컬 개발 환경 (Day 9)

#### 4.1 Docker Compose 업데이트

**파일**: `docker-compose.yml` (수정)
```yaml
version: '3.8'

services:
  # MySQL 유지 (기존 호환성)
  mysql:
    image: mysql:8.0
    container_name: oms-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-oms}
      MYSQL_USER: ${MYSQL_USER:-oms}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-oms_password}
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - oms-network

  # PostgreSQL 추가 (옵션 - 요구사항 대응)
  postgres:
    image: postgres:16
    container_name: oms-postgres
    restart: unless-stopped
    profiles: ["postgres"]  # 선택적 활성화
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-oms}
      POSTGRES_USER: ${POSTGRES_USER:-oms}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-oms_password}
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U oms"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - oms-network

  # MongoDB, Redis 유지
  mongodb:
    image: mongo:7.0
    # ... (기존 설정 유지)

  redis:
    image: redis:7-alpine
    # ... (기존 설정 유지)

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    # ... (기존 설정 유지)

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    # ... (기존 설정 유지)

volumes:
  mysql_data:
  postgres_data:  # 추가
  mongodb_data:
  redis_data:
```

#### 4.2 .gitignore 강화

**파일**: `.gitignore` (수정)
```gitignore
# ===== Backend =====
backend/build/
backend/**/build/
backend/.gradle/
*.jar
*.war
*.class

# ===== Frontend =====
frontend/dist/
frontend/node_modules/
frontend/.pnpm-store/
frontend/coverage/
frontend/playwright-report/

# ===== IDE =====
.idea/
*.iml
.vscode/
*.swp
*.swo

# ===== Environment =====
.env
.env.local
.env.*.local
*.local

# ===== OS =====
.DS_Store
Thumbs.db

# ===== Logs =====
logs/
*.log
npm-debug.log*
pnpm-debug.log*

# ===== Test =====
test-results/
playwright/.cache/
```

#### 4.3 환경 변수 템플릿

**파일**: `backend/.env.example`
```bash
# Database
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=oms
MYSQL_USER=oms
MYSQL_PASSWORD=oms_password

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
JWT_SECRET=your-super-secret-key-change-in-production
JWT_EXPIRATION=86400000

# Spring
SPRING_PROFILES_ACTIVE=local
```

**파일**: `frontend/.env.example`
```bash
# API
VITE_API_URL=http://localhost:8080

# Environment
VITE_APP_ENV=development
```

---

### Phase 5: 문서화 (Day 10)

#### 5.1 README 업데이트

**파일**: `README.md` (루트)
```markdown
# Order Management System (OMS)

글로벌 커머스를 위한 통합 주문 관리 시스템

## 기술 스택

### Backend
- Kotlin 1.9 + Spring Boot 3.2
- JDK 21 (Virtual Threads)
- MySQL 8.0 / MongoDB 7.0 / Redis 7

### Frontend
- React 19 + TypeScript 5
- Vite 6 + pnpm
- TanStack Query + Zustand

## 로컬 개발 환경

### 요구사항
- JDK 21
- Node.js 20+
- pnpm 9+
- Docker + Docker Compose

### 실행 방법

```bash
# 1. 인프라 실행
docker compose up -d mysql mongodb redis

# 2. 백엔드 실행
cd backend
./gradlew :api:bootRun

# 3. 프론트엔드 실행
cd frontend
pnpm install
pnpm dev
```

### 접속 URL
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 프로젝트 구조

```
.
├── backend/           # Kotlin/Spring Boot 백엔드
├── frontend/          # React/TypeScript 프론트엔드
├── docker/            # Docker 초기화 스크립트
├── .github/workflows/ # CI/CD 파이프라인
└── docker-compose.yml # 로컬 개발 환경
```

상세 내용은 각 디렉토리의 README를 참조하세요.
```

---

## 3. 변경 파일 목록 (최종)

### 신규 생성 파일 (28개)

```
.github/workflows/backend-ci.yml
.github/workflows/frontend-ci.yml
backend/config/detekt/detekt.yml
backend/api/src/main/kotlin/.../config/WebConfig.kt
backend/api/src/main/resources/logback-spring.xml
backend/.env.example
frontend/src/main.tsx
frontend/src/App.tsx (이동)
frontend/src/features/order/index.ts
frontend/src/features/order/OrderView.tsx (이동)
frontend/src/features/inventory/index.ts
frontend/src/features/inventory/InventoryView.tsx (이동)
frontend/src/features/payment/index.ts
frontend/src/features/payment/SettlementView.tsx (이동)
frontend/src/shared/api/client.ts
frontend/src/shared/api/queryClient.ts
frontend/src/shared/api/types.ts (생성됨)
frontend/src/shared/api/openapi.json (백엔드에서 생성)
frontend/eslint.config.js
frontend/.prettierrc
frontend/vitest.config.ts
frontend/playwright.config.ts
frontend/.env.example
frontend/pnpm-lock.yaml
backend/README.md
frontend/README.md
```

### 수정 파일 (10개)

```
backend/build.gradle.kts
backend/gradle.properties
backend/api/build.gradle.kts
backend/api/src/main/resources/application.yml
frontend/package.json
frontend/vite.config.ts
frontend/tsconfig.json
docker-compose.yml
.gitignore
README.md
```

### 이동 파일 (40+개)

프론트엔드 views/, stores/, components/, hooks/, services/ 전체가 src/ 하위로 재구성

---

## 4. 위험도 평가 (수정됨)

| 영역 | 위험도 | 근거 |
|------|--------|------|
| 백엔드 빌드 설정 | LOW | 플러그인 추가만 |
| JDK 21 업그레이드 | MEDIUM | 호환성 검증 필요 |
| 프론트엔드 구조 변경 | **HIGH** | 40+ 파일 이동 |
| CI/CD 신규 | LOW | 신규 구축 |
| 테스트 커버리지 | **CRITICAL** | 0% → 80%/70% 불가능 |
| DB 마이그레이션 | **DEFERRED** | 별도 PR로 분리 |

---

## 5. 테스트 계획 (현실적)

### 5.1 Phase 1: 기반 구축 (커버리지 목표: 20%)

- [ ] 백엔드: 핵심 도메인 엔티티 단위 테스트
- [ ] 프론트엔드: 공용 컴포넌트 테스트

### 5.2 Phase 2: 점진적 확대 (커버리지 목표: 50%)

- [ ] 백엔드: 서비스 레이어 테스트
- [ ] 프론트엔드: Feature 컴포넌트 테스트

### 5.3 Phase 3: 완성 (커버리지 목표: 80%/70%)

- [ ] 백엔드: 통합 테스트 (Testcontainers)
- [ ] 프론트엔드: E2E 테스트 (Playwright)

---

## 6. 일정 (현실적)

| Phase | 기간 | 작업 |
|-------|------|------|
| Phase 0 | Day 1 | 사전 검증 |
| Phase 1 | Day 2-3 | 백엔드 도구 강화 |
| Phase 2 | Day 4-6 | 프론트엔드 재구성 |
| Phase 3 | Day 7-8 | CI/CD 구축 |
| Phase 4 | Day 9 | 로컬 개발 환경 |
| Phase 5 | Day 10 | 문서화 |
| **테스트** | Week 2-4 | 커버리지 구축 (별도 진행) |
| **DB 마이그레이션** | TBD | PostgreSQL 전환 (별도 PR) |

**총 예상 기간**: 2주 (핵심 구조) + 2주 (테스트 구축)

---

## 7. 의사결정 로그

| 결정 사항 | 선택 | 근거 |
|-----------|------|------|
| Kotlin vs Java | Kotlin 유지 | 156파일 재작성 위험 |
| 모듈 구조 | 기존 9개 유지 | DDD 아키텍처 우수성 |
| QA 도구 | ktlint + detekt | Kotlin 네이티브 |
| 커버리지 도구 | Kover | Kotlin 네이티브 (Jacoco 대신) |
| DB | MySQL 유지, PostgreSQL 추가 | 점진적 마이그레이션 |
| OpenAPI 공유 | 레포지토리 커밋 | 아티팩트 의존성 회피 |
| 테스트 일정 | 별도 분리 | 현실적 기대치 설정 |

---

*Plan Version: 2.0 (Final)*
*Architect Review: Approved with recommendations*
*Critic Review: Critical issues addressed*
*Status: Ready for Implementation*
