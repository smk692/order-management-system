# Global OMS - System Architecture

## Overview

Global OMS is an enterprise-grade order management system built using Domain-Driven Design (DDD) principles and event-driven architecture. The system is designed to handle complex e-commerce operations at scale with support for multi-channel sales, global inventory management, and automated order processing.

## Architecture Principles

- **Domain-Driven Design (DDD)**: Business logic organized into bounded contexts
- **Event-Driven Architecture**: Asynchronous communication between contexts
- **Hexagonal Architecture**: Clean separation of business logic from infrastructure
- **Microservices Ready**: Modular design allows for future service extraction
- **API-First**: RESTful APIs with OpenAPI/Swagger documentation

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Frontend (React 19)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Features   │  │ React Query  │  │   Zustand    │          │
│  │   (Pages)    │  │ (Server)     │  │    (UI)      │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     API Layer (Spring Boot 3.2)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ REST         │  │ WebSocket    │  │   GraphQL    │          │
│  │ Controllers  │  │ (Future)     │  │  (Future)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Application Layer (Use Cases)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Application │  │    Event     │  │  Integration │          │
│  │   Services   │  │   Handlers   │  │   Services   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              Domain Layer (Bounded Contexts)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Order     │  │   Channel    │  │  Inventory   │          │
│  │   Context    │  │   Context    │  │   Context    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │    Claim     │  │  Settlement  │  │  Automation  │          │
│  │   Context    │  │   Context    │  │   Context    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Strategy   │  │   Catalog    │  │   Identity   │          │
│  │   Context    │  │   Context    │  │   Context    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  PostgreSQL  │  │   MongoDB    │  │    Redis     │          │
│  │   (RDBMS)    │  │  (Documents) │  │   (Cache)    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## Backend Architecture

### Technology Stack

- **JDK 21**: Latest LTS with Virtual Threads for improved concurrency
- **Kotlin 1.9**: Modern JVM language with concise syntax
- **Spring Boot 3.2**: Application framework
- **PostgreSQL 16**: Primary relational database
- **MongoDB 7.0**: Document store for flexible schemas
- **Redis 7**: Caching and session management

### Bounded Contexts (DDD Modules)

#### 1. Order Context (`domain-order`)
- Manages order lifecycle
- Order placement, modification, cancellation
- Order status tracking
- Order splitting and merging

#### 2. Channel Context (`domain-channel`)
- Multi-channel integration (marketplaces, own stores)
- Channel-specific policies and fees
- Warehouse and fulfillment center management
- Channel performance monitoring

#### 3. Inventory Context (`domain-inventory`)
- Real-time inventory synchronization
- Multi-warehouse inventory allocation
- Safety stock alerts
- Inventory history tracking

#### 4. Claim Context (`domain-claim`)
- Return, exchange, refund processing
- Claim workflow management
- Customer compensation policies
- Claim statistics and analytics

#### 5. Settlement Context (`domain-settlement`)
- Channel-specific settlement automation
- Fee calculation and management
- Settlement report generation
- Settlement history and queries

#### 6. Automation Context (`domain-automation`)
- Rule-based automated processing
- Conditional action triggers
- Workflow automation

#### 7. Strategy Context (`domain-strategy`)
- Inventory allocation strategies
- Global operations readiness
- Strategic decision support

#### 8. Catalog Context (`domain-catalog`)
- Product information management
- SKU management
- Product categorization

#### 9. Identity Context (`domain-identity`)
- Company/organization management
- User authentication and authorization
- Role-based access control (RBAC)
- Multi-tenancy support

### Module Structure

```
backend/
├── api/                        # REST API layer
├── application/                # Application services (use cases)
├── domain/
│   ├── domain-order/          # Order bounded context
│   ├── domain-channel/        # Channel bounded context
│   ├── domain-inventory/      # Inventory bounded context
│   ├── domain-claim/          # Claim bounded context
│   ├── domain-settlement/     # Settlement bounded context
│   ├── domain-automation/     # Automation bounded context
│   ├── domain-strategy/       # Strategy bounded context
│   ├── domain-catalog/        # Catalog bounded context
│   └── domain-identity/       # Identity bounded context
├── infrastructure/
│   ├── infra-postgres/        # JPA repositories (PostgreSQL)
│   ├── infra-mongo/           # MongoDB repositories
│   └── infra-redis/           # Redis cache
└── core/
    └── core-domain/           # Shared domain (base entities, events)
```

### JDK 21 Virtual Threads

The backend leverages JDK 21's Virtual Threads for improved concurrency:

- **Spring MVC**: Configured to use virtual threads for request handling
- **Async Operations**: Database queries and API calls benefit from lightweight threads
- **Scalability**: Handle thousands of concurrent connections with minimal resource overhead

Configuration in `application.yml`:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Database Strategy

- **PostgreSQL**: Transactional data (orders, inventory, settlements)
- **MongoDB**: Flexible schemas (events, logs, analytics)
- **Redis**: Session state, caching, real-time data

## Frontend Architecture

### Technology Stack

- **React 19**: Modern UI library with server components (future)
- **TypeScript**: Type-safe JavaScript
- **Vite 6**: Fast build tool and dev server
- **pnpm**: Efficient package management
- **React Query**: Server state management
- **Zustand**: UI state management
- **i18next**: Internationalization

### State Management Strategy

#### React Query (Server State)
- API data fetching and caching
- Automatic background refetching
- Optimistic updates
- Request deduplication

#### Zustand (UI State)
- Local UI state (modals, forms, selections)
- Theme preferences
- User session state

**Separation Rationale**: Clear boundary between server-synchronized data (React Query) and local UI concerns (Zustand).

### Feature-Based Architecture

```
frontend/src/
├── features/
│   ├── orders/
│   │   ├── components/        # Order-specific components
│   │   ├── hooks/             # Order-related hooks
│   │   ├── api/               # Order API calls (React Query)
│   │   └── store/             # Order UI state (Zustand)
│   ├── inventory/
│   ├── channels/
│   └── ...
├── components/
│   └── ui/                    # Shared UI components
├── lib/
│   ├── api/                   # API client setup
│   ├── query/                 # React Query config
│   └── i18n/                  # i18n setup
└── App.tsx
```

### Component Patterns

- **Atomic Design**: Atoms → Molecules → Organisms → Templates → Pages
- **Compound Components**: Complex components with flexible composition
- **Render Props & Hooks**: Reusable logic extraction

## Infrastructure

### Docker Compose Setup

Two deployment modes:

1. **Full Stack** (`docker-compose.yml`):
   - Backend (Spring Boot)
   - Frontend (Vite + React)
   - PostgreSQL
   - MongoDB
   - Redis

2. **Infrastructure Only** (`docker-compose.dev.yml`):
   - PostgreSQL (with Adminer)
   - MongoDB (with Mongo Express)
   - Redis
   - For local development of backend/frontend

### Database Initialization

- **PostgreSQL**: Schema initialization scripts in `docker/postgres/init/`
- **MongoDB**: Collections and indexes created on first run
- **Redis**: No initialization required

## CI/CD Pipeline

### GitHub Actions Workflows

#### Backend CI (`.github/workflows/backend-ci.yml`)
- Triggered on push/PR to `main` affecting `backend/**`
- Steps:
  1. Setup JDK 21
  2. Cache Gradle dependencies
  3. Run `./gradlew build` (compile + test)
  4. Run code quality checks (detekt, ktlint)

#### Frontend CI (`.github/workflows/frontend-ci.yml`)
- Triggered on push/PR to `main` affecting `frontend/**`
- Steps:
  1. Setup Node.js and pnpm
  2. Cache pnpm dependencies
  3. Run `pnpm install`
  4. Run `pnpm type-check` (TypeScript)
  5. Run `pnpm lint` (ESLint)
  6. Run `pnpm test` (Vitest)
  7. Run `pnpm build` (production build)

#### Docker Build (`.github/workflows/docker-build.yml`)
- Triggered on push/PR to `main` affecting Docker files
- Validates Docker Compose builds

### Future CI/CD Enhancements
- Automated deployments to staging/production
- Database migration checks
- E2E testing with Playwright
- Performance benchmarking
- Security scanning (Snyk, Trivy)

## Code Quality

### Backend
- **detekt**: Static code analysis for Kotlin
- **ktlint**: Kotlin code style enforcement
- Configuration: `detekt.yml`, `.editorconfig`

### Frontend
- **ESLint 9**: Linting with flat config system
- **Prettier**: Code formatting
- **TypeScript**: Type checking with strict mode
- Configuration: `eslint.config.js`, `.prettierrc`, `tsconfig.json`

## Testing Strategy

### Backend Testing (Planned)
- **Unit Tests**: JUnit 5, MockK for mocking
- **Integration Tests**: Testcontainers for database tests
- **Architecture Tests**: ArchUnit for enforcing architectural rules
- **API Tests**: Spring REST Docs

### Frontend Testing
- **Unit Tests**: Vitest for component logic
- **Component Tests**: Testing Library for React components
- **E2E Tests** (Planned): Playwright for user flows
- **Visual Regression** (Planned): Percy or Chromatic

### Test Coverage Goals
- Backend: >80% line coverage
- Frontend: >70% line coverage
- Critical paths: 100% coverage

## Security Considerations

### Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Multi-tenancy isolation

### Data Protection
- Encrypted passwords (bcrypt)
- HTTPS enforcement in production
- Environment variable secrets management

### API Security
- Rate limiting
- CORS configuration
- Input validation and sanitization
- SQL injection prevention (JPA/Hibernate)

## Scalability & Performance

### Backend
- **Virtual Threads**: Lightweight concurrency (JDK 21)
- **Connection Pooling**: HikariCP for database connections
- **Caching**: Redis for frequently accessed data
- **Async Processing**: Event-driven architecture for background tasks

### Frontend
- **Code Splitting**: Route-based lazy loading
- **React Query Caching**: Reduce API calls
- **Optimistic Updates**: Instant UI feedback
- **Vite**: Fast HMR and optimized builds

### Database
- **Indexing**: Optimized queries with proper indexes
- **Read Replicas** (Future): Scale read operations
- **Sharding** (Future): Horizontal scaling for PostgreSQL

## Monitoring & Observability (Planned)

- **Application Metrics**: Spring Boot Actuator + Micrometer
- **Logging**: Structured logging with Logback
- **Tracing**: Distributed tracing with OpenTelemetry
- **APM**: Application Performance Monitoring (e.g., New Relic, Datadog)
- **Error Tracking**: Sentry for frontend/backend errors

## Future Enhancements

### Architecture Evolution
- **Event Sourcing**: Audit trail for critical operations
- **CQRS**: Separate read/write models for complex queries
- **Microservices**: Extract bounded contexts into separate services
- **GraphQL**: Flexible API queries for frontend

### Technology Additions
- **Kafka**: Event streaming for inter-service communication
- **Elasticsearch**: Full-text search and analytics
- **gRPC**: High-performance inter-service communication
- **Kubernetes**: Container orchestration for production

## References

- [Domain-Driven Design (DDD)](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [JDK 21 Virtual Threads](https://openjdk.org/jeps/444)
- [React Query](https://tanstack.com/query/latest)
- [Zustand](https://zustand-demo.pmnd.rs/)
