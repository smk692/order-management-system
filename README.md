# Global OMS - Enterprise Order Management System

글로벌 주문 관리 시스템 (Global Order Management System)

## Overview

Global OMS는 대규모 이커머스 운영을 위한 엔터프라이즈급 주문 관리 시스템입니다. 도메인 주도 설계(DDD)와 이벤트 기반 아키텍처를 기반으로 구축되어, 복잡한 비즈니스 요구사항을 유연하게 처리할 수 있습니다.

멀티 채널 판매, 글로벌 재고 관리, 자동화된 주문 처리, 클레임 관리 등 이커머스 운영에 필요한 핵심 기능을 통합 제공합니다.

## Key Features

### 📦 Order Management (주문 관리)
- 멀티 채널 주문 통합 처리
- 실시간 주문 상태 추적
- 주문 분할/병합 처리
- 배송 추적 연동

### 🏢 Channel Management (채널 관리)
- 다중 판매 채널 연동 (마켓플레이스, 자사몰)
- 채널별 정책 및 수수료 관리
- 창고 및 물류 센터 관리
- 채널 성과 모니터링

### 📊 Inventory Management (재고 관리)
- 실시간 재고 동기화
- 멀티 창고 재고 배분
- 안전 재고 알림
- 재고 이력 추적

### ⚡ Claim Processing (클레임 처리)
- 반품/교환/환불 처리 자동화
- 클레임 상태 워크플로우
- 고객 보상 정책 관리
- 클레임 통계 및 분석

### 💰 Settlement Management (정산 관리)
- 채널별 정산 자동화
- 수수료 계산 및 관리
- 정산 리포트 생성
- 정산 내역 조회

### 🤖 Automation & Strategy (자동화 및 전략)
- 규칙 기반 자동 처리
- 재고 배분 전략 수립
- 글로벌 운영 준비도 관리
- 조건부 액션 트리거

### 👥 Identity & Access (사용자 관리)
- 회사/조직 관리
- 사용자 권한 관리
- 역할 기반 접근 제어
- 멀티 테넌시 지원

## Tech Stack

### Backend
- **Language**: Kotlin 1.9
- **Framework**: Spring Boot 3.2
- **Database**: MySQL 8.0, MongoDB 7.0
- **Cache**: Redis 7
- **Build**: Gradle 8.5

### Frontend
- **Language**: TypeScript
- **Framework**: React 19
- **Build**: Vite 6
- **State**: Zustand
- **i18n**: i18next

## Quick Start with Docker

### Prerequisites
- Docker Desktop 4.0+
- Docker Compose 2.0+

### 1. Clone and Setup

```bash
git clone https://github.com/smk692/order-management-system.git
cd order-management-system

# Copy environment file
cp .env.example .env
```

### 2. Run Full Stack (Recommended)

```bash
# Start all services (Infrastructure + Backend + Frontend)
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f
```

**Access:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:3000/swagger-ui/

### 3. Run Infrastructure Only (Development)

For local backend/frontend development:

```bash
# Start only infrastructure (MySQL, MongoDB, Redis)
docker compose -f docker-compose.dev.yml up -d

# Additional tools available:
# - Adminer (MySQL): http://localhost:8081
# - Mongo Express: http://localhost:8082
```

Then run backend/frontend locally:

```bash
# Backend
cd backend
./gradlew :api:bootRun

# Frontend (in another terminal)
cd frontend
npm install
npm run dev
```

### 4. Stop Services

```bash
# Stop all
docker compose down

# Stop and remove volumes (clean reset)
docker compose down -v
```

## Local Development (Without Docker)

### Backend

```bash
cd backend

# Build
./gradlew build

# Run
./gradlew :api:bootRun

# Run tests
./gradlew test
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Development
npm run dev

# Build
npm run build
```

## Project Structure

```
order-management-system/
├── backend/
│   ├── api/                    # REST API (Spring Boot)
│   ├── application/            # Application Services
│   ├── domain/
│   │   ├── domain-channel/     # Channel Context
│   │   ├── domain-inventory/   # Inventory Context
│   │   ├── domain-claim/       # Claim Context
│   │   ├── domain-settlement/  # Settlement Context
│   │   ├── domain-automation/  # Automation Context
│   │   ├── domain-strategy/    # Strategy Context
│   │   ├── domain-order/       # Order Context
│   │   ├── domain-catalog/     # Catalog Context
│   │   └── domain-identity/    # Identity Context
│   ├── infrastructure/
│   │   ├── infra-mysql/        # JPA Repositories
│   │   ├── infra-mongo/        # MongoDB Repositories
│   │   └── infra-redis/        # Redis Cache
│   └── core/
│       └── core-domain/        # Shared Domain (Entity, Event)
├── frontend/
│   ├── components/             # React Components
│   ├── services/               # API Services
│   └── i18n/                   # Internationalization
├── docker/
│   └── mysql/init/             # MySQL Init Scripts
├── docker-compose.yml          # Full Stack
├── docker-compose.dev.yml      # Infrastructure Only
└── .env.example                # Environment Template
```

## API Endpoints

| Context | Endpoint | Description |
|---------|----------|-------------|
| Channel | `/api/v1/channels` | Channel management |
| Channel | `/api/v1/warehouses` | Warehouse management |
| Inventory | `/api/v1/stocks` | Stock management |
| Claim | `/api/v1/claims` | Claim processing |
| Settlement | `/api/v1/settlements` | Settlement management |
| Automation | `/api/v1/automation/rules` | Automation rules |
| Strategy | `/api/v1/strategies` | Operations strategy |
| Strategy | `/api/v1/readiness` | Global readiness |
| Order | `/api/v1/orders` | Order management |
| Catalog | `/api/v1/products` | Product catalog |
| Identity | `/api/v1/companies` | Company management |
| Identity | `/api/v1/users` | User management |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_DATABASE` | oms | MySQL database name |
| `MYSQL_USER` | oms | MySQL username |
| `MYSQL_PASSWORD` | oms_password | MySQL password |
| `MYSQL_PORT` | 3306 | MySQL port |
| `MONGO_DATABASE` | oms | MongoDB database name |
| `MONGO_USER` | oms | MongoDB username |
| `MONGO_PASSWORD` | oms_password | MongoDB password |
| `MONGO_PORT` | 27017 | MongoDB port |
| `REDIS_PORT` | 6379 | Redis port |
| `BACKEND_PORT` | 8080 | Backend API port |
| `FRONTEND_PORT` | 3000 | Frontend port |

## License

MIT License
