# Global OMS - Enterprise Order Management System

글로벌 주문 관리 시스템 (Global Order Management System)

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
