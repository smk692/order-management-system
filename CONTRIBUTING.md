# Contributing to Global OMS

Thank you for your interest in contributing to Global OMS! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Standards](#code-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Code Review Process](#code-review-process)

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **JDK 21+**: Required for backend development
- **pnpm 9+**: Package manager for frontend
- **Docker Desktop 4.0+**: For running infrastructure services
- **Git**: Version control

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/order-management-system.git
   cd order-management-system
   ```
3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/smk692/order-management-system.git
   ```

## Development Setup

### Backend Setup

1. Start infrastructure services:
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. Run the backend:
   ```bash
   cd backend
   ./gradlew :api:bootRun
   ```

3. Access Swagger UI at http://localhost:8080/swagger-ui/

### Frontend Setup

1. Install dependencies:
   ```bash
   cd frontend
   pnpm install
   ```

2. Run development server:
   ```bash
   pnpm dev
   ```

3. Access the app at http://localhost:3000

### Verify Setup

- Backend health check: `curl http://localhost:8080/actuator/health`
- Frontend should open automatically in your browser

## Code Standards

### Backend (Kotlin)

#### Style Guide

Follow Kotlin coding conventions:
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Prefer `val` over `var` when possible
- Use data classes for DTOs

#### Code Quality Tools

Run before committing:
```bash
# Format code
./gradlew ktlintFormat

# Check style
./gradlew ktlintCheck

# Static analysis
./gradlew detekt

# All checks
./gradlew ktlintCheck detekt test
```

#### Package Structure

```
domain-{context}/
├── model/              # Domain entities, value objects
├── repository/         # Repository interfaces
├── service/            # Domain services
└── event/              # Domain events
```

#### Best Practices

- Keep domain logic in domain modules
- Use dependency injection (Spring)
- Write self-documenting code
- Add KDoc for public APIs
- Follow SOLID principles
- Use sealed classes for type hierarchies

### Frontend (TypeScript/React)

#### Style Guide

Follow Airbnb React/TypeScript conventions:
- Use 2 spaces for indentation
- Maximum line length: 100 characters
- Use functional components with hooks
- Prefer named exports for components
- Use TypeScript strict mode

#### Code Quality Tools

Run before committing:
```bash
# Format code
pnpm format

# Check formatting
pnpm format:check

# Lint
pnpm lint

# Type check
pnpm type-check

# All checks
pnpm lint && pnpm format:check && pnpm type-check && pnpm test
```

#### Component Structure

```tsx
// features/orders/components/OrderList.tsx
import { useState } from 'react';
import { useOrders } from '../hooks/useOrders';

export function OrderList() {
  // Hooks first
  const { data, isLoading } = useOrders();
  const [selected, setSelected] = useState<string | null>(null);

  // Early returns
  if (isLoading) return <LoadingSpinner />;

  // Main render
  return (
    <div>
      {data?.map(order => (
        <OrderItem key={order.id} order={order} />
      ))}
    </div>
  );
}
```

#### Best Practices

- Use React Query for server state
- Use Zustand for UI state
- Avoid prop drilling (use context or state management)
- Keep components small and focused
- Use TypeScript for type safety
- Write accessible HTML (ARIA attributes)

## Commit Guidelines

### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring (no feature or bug fix)
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, tooling
- `ci`: CI/CD configuration changes

#### Scope

Specify the affected module or feature:
- `order`: Order context
- `inventory`: Inventory context
- `channel`: Channel context
- `claim`: Claim context
- `settlement`: Settlement context
- `automation`: Automation context
- `strategy`: Strategy context
- `catalog`: Catalog context
- `identity`: Identity context
- `api`: API layer
- `frontend`: Frontend code
- `infra`: Infrastructure code

#### Examples

```bash
# Feature
feat(order): add order cancellation endpoint

# Bug fix
fix(inventory): correct stock calculation for split orders

# Documentation
docs(readme): update setup instructions for PostgreSQL

# Refactoring
refactor(claim): simplify claim status workflow

# Multiple scopes
feat(order,inventory): add inventory reservation on order creation
```

### Commit Best Practices

- Keep commits atomic (one logical change per commit)
- Write clear, descriptive commit messages
- Reference issue numbers: `feat(order): add search (#123)`
- Use imperative mood: "add" not "added" or "adds"

## Pull Request Process

### Before Creating a PR

1. **Update from upstream**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run all checks**:
   ```bash
   # Backend
   cd backend
   ./gradlew ktlintCheck detekt test

   # Frontend
   cd frontend
   pnpm lint && pnpm format:check && pnpm type-check && pnpm test
   ```

3. **Verify build**:
   ```bash
   # Backend
   ./gradlew build

   # Frontend
   pnpm build
   ```

### Creating a PR

1. Push your branch:
   ```bash
   git push origin feature/your-feature-name
   ```

2. Open a PR on GitHub

3. Fill out the PR template:
   - **Title**: Use conventional commit format
   - **Description**: Explain what and why
   - **Related Issues**: Link to issue(s)
   - **Screenshots**: For UI changes
   - **Testing**: How you tested the changes

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Closes #123

## Changes Made
- Change 1
- Change 2

## Testing
How the changes were tested

## Screenshots (if applicable)

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests added/updated
- [ ] All tests pass
- [ ] Build succeeds
```

### PR Review Criteria

Your PR will be reviewed for:
- **Functionality**: Does it work as intended?
- **Code Quality**: Is it clean, readable, maintainable?
- **Tests**: Are there adequate tests?
- **Documentation**: Is documentation updated?
- **Performance**: Are there performance concerns?
- **Security**: Are there security implications?

## Testing Requirements

### Backend Testing

#### Unit Tests

Write unit tests for:
- Domain logic (entities, value objects)
- Business rules and validations
- Application services

Example with JUnit 5 and MockK:
```kotlin
@Test
fun `should create order when inventory is available`() {
    // Given
    val order = Order(...)
    every { inventoryService.checkAvailability(...) } returns true

    // When
    val result = orderService.createOrder(order)

    // Then
    assertThat(result).isNotNull()
    verify { inventoryService.reserveStock(...) }
}
```

#### Integration Tests

Write integration tests for:
- API endpoints
- Database operations
- External service integrations

Use `@SpringBootTest` for integration tests.

#### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :domain:domain-order:test

# With coverage
./gradlew test jacocoTestReport
```

### Frontend Testing

#### Component Tests

Write tests for:
- Component rendering
- User interactions
- Edge cases and error states

Example with Vitest and Testing Library:
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { OrderList } from './OrderList';

describe('OrderList', () => {
  it('renders orders when data is loaded', () => {
    render(<OrderList />);
    expect(screen.getByText('Order #1')).toBeInTheDocument();
  });

  it('handles order selection', () => {
    render(<OrderList />);
    fireEvent.click(screen.getByText('Order #1'));
    expect(screen.getByText('Selected')).toBeInTheDocument();
  });
});
```

#### Hook Tests

Test custom hooks with `@testing-library/react-hooks`.

#### Running Tests

```bash
# All tests
pnpm test

# Watch mode
pnpm test:watch

# Coverage
pnpm test:coverage

# UI mode
pnpm test:ui
```

### Test Coverage Requirements

- **Backend**: Minimum 80% line coverage for new code
- **Frontend**: Minimum 70% line coverage for new code
- **Critical Paths**: 100% coverage for payment, inventory, settlement

## Code Review Process

### As a Contributor

- Respond to feedback promptly
- Be open to suggestions
- Ask questions if feedback is unclear
- Update the PR based on feedback
- Mark conversations as resolved when addressed

### As a Reviewer

- Be respectful and constructive
- Explain the "why" behind suggestions
- Distinguish between blocking and non-blocking feedback
- Use GitHub's suggestion feature for minor changes
- Approve when satisfied or request changes

### Review Checklist

- [ ] Code follows project conventions
- [ ] Logic is clear and well-structured
- [ ] Tests are comprehensive
- [ ] Documentation is updated
- [ ] No obvious bugs or security issues
- [ ] Performance is acceptable
- [ ] No unnecessary dependencies added

## Need Help?

- **Documentation**: Check README.md, ARCHITECTURE.md
- **Issues**: Browse existing issues or create a new one
- **Discussions**: Use GitHub Discussions for questions
- **Code of Conduct**: Be respectful and inclusive

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

Thank you for contributing to Global OMS!
