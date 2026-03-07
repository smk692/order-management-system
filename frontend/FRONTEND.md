# Frontend - Global OMS

This document provides detailed documentation for the Global OMS frontend.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [State Management](#state-management)
- [Development](#development)
- [Styling](#styling)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Build and Deployment](#build-and-deployment)

## Overview

The Global OMS frontend is a modern React application built with TypeScript, Vite, and feature-based architecture. It uses React Query for server state management and Zustand for UI state, providing a clear separation of concerns.

## Technology Stack

- **Language**: TypeScript 5+
- **Framework**: React 19
- **Build Tool**: Vite 6
- **Package Manager**: pnpm 9+
- **State Management**:
  - React Query (TanStack Query) - Server state
  - Zustand - UI state
- **Routing**: React Router 7
- **Styling**: CSS Modules / Tailwind CSS (to be configured)
- **Testing**: Vitest, Testing Library
- **Code Quality**: ESLint 9, Prettier
- **Internationalization**: i18next

## Architecture

### Feature-Based Architecture

The frontend follows a feature-based architecture where code is organized by business feature rather than technical layer.

```
src/
├── features/              # Feature modules
│   ├── orders/            # Order management feature
│   │   ├── components/    # Order-specific components
│   │   ├── hooks/         # Order-related hooks
│   │   ├── api/           # Order API calls (React Query)
│   │   ├── store/         # Order UI state (Zustand)
│   │   ├── types/         # Order TypeScript types
│   │   └── index.ts       # Public exports
│   ├── inventory/
│   ├── channels/
│   └── ...
├── components/            # Shared components
│   └── ui/                # Reusable UI components
├── lib/                   # Shared utilities
│   ├── api/               # API client setup
│   ├── query/             # React Query configuration
│   └── i18n/              # i18n configuration
├── hooks/                 # Shared hooks
├── types/                 # Shared TypeScript types
└── App.tsx                # Root component
```

### Component Hierarchy

```
App
├── Layout
│   ├── Header
│   ├── Sidebar
│   └── Content
│       ├── OrderListPage (Feature)
│       │   ├── OrderList (Feature Component)
│       │   │   ├── OrderItem
│       │   │   └── OrderFilters
│       │   └── OrderDetail (Feature Component)
│       └── InventoryPage (Feature)
```

## Project Structure

### Feature Module Structure

Each feature follows a consistent structure:

```
features/orders/
├── components/            # UI components
│   ├── OrderList.tsx
│   ├── OrderDetail.tsx
│   ├── OrderForm.tsx
│   └── index.ts
├── hooks/                 # Custom hooks
│   ├── useOrders.ts       # React Query hook
│   ├── useOrderForm.ts    # Form logic
│   └── index.ts
├── api/                   # API calls
│   ├── orderApi.ts        # React Query queries/mutations
│   └── index.ts
├── store/                 # UI state (Zustand)
│   ├── orderStore.ts
│   └── index.ts
├── types/                 # TypeScript types
│   ├── order.ts
│   └── index.ts
├── utils/                 # Feature utilities
│   ├── orderUtils.ts
│   └── index.ts
└── index.ts               # Public exports
```

### Shared Components

```
components/ui/
├── Button/
│   ├── Button.tsx
│   ├── Button.test.tsx
│   ├── Button.module.css
│   └── index.ts
├── Input/
├── Modal/
├── Table/
└── ...
```

## State Management

### React Query (Server State)

**Purpose**: Manage server-synchronized data (API responses).

**Features**:
- Automatic caching
- Background refetching
- Optimistic updates
- Request deduplication
- Pagination and infinite scroll

**Setup** (`lib/query/queryClient.ts`):
```typescript
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});
```

**Usage Example** (`features/orders/hooks/useOrders.ts`):
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderApi } from '../api/orderApi';

export function useOrders() {
  return useQuery({
    queryKey: ['orders'],
    queryFn: orderApi.getOrders,
  });
}

export function useCreateOrder() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: orderApi.createOrder,
    onSuccess: () => {
      // Invalidate and refetch orders
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
```

**Component Usage**:
```typescript
function OrderList() {
  const { data, isLoading, error } = useOrders();
  const createOrder = useCreateOrder();

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} />;

  return (
    <div>
      {data?.map(order => <OrderItem key={order.id} order={order} />)}
      <button onClick={() => createOrder.mutate(newOrder)}>
        Create Order
      </button>
    </div>
  );
}
```

### Zustand (UI State)

**Purpose**: Manage local UI state (modals, forms, selections).

**Features**:
- Simple and lightweight
- No boilerplate
- DevTools support
- TypeScript-friendly

**Setup** (`features/orders/store/orderStore.ts`):
```typescript
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

interface OrderStore {
  selectedOrderId: string | null;
  isModalOpen: boolean;
  setSelectedOrderId: (id: string | null) => void;
  openModal: () => void;
  closeModal: () => void;
}

export const useOrderStore = create<OrderStore>()(
  devtools(
    (set) => ({
      selectedOrderId: null,
      isModalOpen: false,
      setSelectedOrderId: (id) => set({ selectedOrderId: id }),
      openModal: () => set({ isModalOpen: true }),
      closeModal: () => set({ isModalOpen: false }),
    }),
    { name: 'OrderStore' }
  )
);
```

**Component Usage**:
```typescript
function OrderList() {
  const { selectedOrderId, setSelectedOrderId } = useOrderStore();

  return (
    <div>
      {orders.map(order => (
        <OrderItem
          key={order.id}
          order={order}
          isSelected={order.id === selectedOrderId}
          onClick={() => setSelectedOrderId(order.id)}
        />
      ))}
    </div>
  );
}
```

### State Management Decision Tree

```
┌─ Is this data from an API?
│  ├─ YES → Use React Query
│  └─ NO → Continue
│
└─ Does this state need to be shared across multiple components?
   ├─ YES → Use Zustand
   └─ NO → Use local component state (useState)
```

## Development

### Prerequisites

- Node.js 20+
- pnpm 9+

### Setup

1. **Install dependencies**:
   ```bash
   cd frontend
   pnpm install
   ```

2. **Start development server**:
   ```bash
   pnpm dev
   ```

3. **Access the app**:
   Open http://localhost:3000

### Package Scripts

```bash
# Development
pnpm dev               # Start dev server with HMR
pnpm dev:host          # Expose to network

# Building
pnpm build             # Production build
pnpm preview           # Preview production build

# Testing
pnpm test              # Run tests in watch mode
pnpm test:run          # Run tests once
pnpm test:coverage     # Generate coverage report
pnpm test:ui           # Open Vitest UI

# Code Quality
pnpm lint              # Lint with ESLint
pnpm lint:fix          # Auto-fix lint issues
pnpm format            # Format with Prettier
pnpm format:check      # Check formatting
pnpm type-check        # TypeScript type checking

# All Checks
pnpm check-all         # Run lint, format:check, type-check, test
```

### Environment Variables

Create `.env.local` for local overrides:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws
VITE_APP_TITLE=Global OMS
```

Access in code:
```typescript
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
```

## Styling

### CSS Modules (Current)

Each component has its own CSS module:

```typescript
// Button.tsx
import styles from './Button.module.css';

export function Button({ children }: ButtonProps) {
  return <button className={styles.button}>{children}</button>;
}
```

```css
/* Button.module.css */
.button {
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
}
```

### Tailwind CSS (Planned)

To be configured for utility-first CSS:

```typescript
export function Button({ children }: ButtonProps) {
  return (
    <button className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
      {children}
    </button>
  );
}
```

### Styling Best Practices

- Use semantic class names
- Keep styles scoped to components
- Avoid global styles except for resets/base styles
- Use CSS custom properties for theming

## Testing

### Testing Framework

- **Vitest**: Fast unit test runner
- **Testing Library**: React component testing
- **MSW** (Planned): Mock Service Worker for API mocking

### Unit Tests

Test component logic and hooks:

```typescript
// Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { Button } from './Button';

describe('Button', () => {
  it('renders children', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click me</Button>);

    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});
```

### Integration Tests

Test feature workflows:

```typescript
// OrderList.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { OrderList } from './OrderList';
import { queryClient } from '@/lib/query/queryClient';

describe('OrderList', () => {
  it('displays orders when loaded', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <OrderList />
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });
  });
});
```

### Hook Tests

Test custom hooks:

```typescript
// useOrders.test.ts
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { useOrders } from './useOrders';

describe('useOrders', () => {
  it('fetches orders successfully', async () => {
    const { result } = renderHook(() => useOrders(), {
      wrapper: QueryClientProvider,
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveLength(3);
  });
});
```

### Running Tests

```bash
# Watch mode (default)
pnpm test

# Run once
pnpm test:run

# Coverage report
pnpm test:coverage

# UI mode
pnpm test:ui
```

### Test Coverage Goals

- **Components**: >70% coverage
- **Hooks**: >80% coverage
- **Utilities**: >90% coverage

## Code Quality

### ESLint 9 (Flat Config)

Configuration: `eslint.config.js`

```javascript
import js from '@eslint/js';
import typescript from '@typescript-eslint/eslint-plugin';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';

export default [
  js.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    plugins: {
      '@typescript-eslint': typescript,
      react,
      'react-hooks': reactHooks,
    },
    rules: {
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      '@typescript-eslint/no-unused-vars': 'warn',
    },
  },
];
```

**Run**:
```bash
pnpm lint
pnpm lint:fix
```

### Prettier

Configuration: `.prettierrc`

```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "arrowParens": "avoid"
}
```

**Run**:
```bash
pnpm format
pnpm format:check
```

### TypeScript

Configuration: `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "react-jsx",
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

**Run**:
```bash
pnpm type-check
```

### Pre-commit Checks

Run all checks before committing:

```bash
pnpm check-all
# Runs: lint, format:check, type-check, test
```

## Build and Deployment

### Production Build

```bash
# Build for production
pnpm build

# Preview production build
pnpm preview
```

Build output: `dist/`

### Docker Build

```bash
# Build Docker image
docker build -t global-oms-frontend:latest -f docker/frontend/Dockerfile .

# Run with Docker Compose
docker compose up -d
```

### Environment-Specific Builds

```bash
# Development
pnpm build --mode development

# Staging
pnpm build --mode staging

# Production
pnpm build --mode production
```

### Bundle Analysis

```bash
# Analyze bundle size (to be configured)
pnpm build --analyze
```

### Deployment Checklist

- [ ] Run `pnpm check-all` (lint, format, type-check, test)
- [ ] Run `pnpm build` successfully
- [ ] Test production build with `pnpm preview`
- [ ] Verify environment variables are set
- [ ] Check bundle size is acceptable
- [ ] Ensure HTTPS is configured
- [ ] Configure CORS for API
- [ ] Set up error tracking (Sentry)
- [ ] Enable analytics (optional)

## Internationalization (i18n)

### Setup with i18next

Configuration: `lib/i18n/i18n.ts`

```typescript
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from './locales/en.json';
import ko from './locales/ko.json';

i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    ko: { translation: ko },
  },
  lng: 'en',
  fallbackLng: 'en',
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
```

### Usage in Components

```typescript
import { useTranslation } from 'react-i18next';

export function OrderList() {
  const { t } = useTranslation();

  return (
    <div>
      <h1>{t('orders.title')}</h1>
      <p>{t('orders.description')}</p>
    </div>
  );
}
```

### Translation Files

```json
// locales/en.json
{
  "orders": {
    "title": "Orders",
    "description": "Manage your orders"
  }
}

// locales/ko.json
{
  "orders": {
    "title": "주문",
    "description": "주문을 관리하세요"
  }
}
```

## Best Practices

### Component Design

- **Single Responsibility**: One component, one purpose
- **Composition**: Build complex UIs from small components
- **Props**: Keep props simple and typed
- **Hooks**: Extract logic into custom hooks

### State Management

- **Server State**: Always use React Query for API data
- **UI State**: Use Zustand for shared UI state
- **Local State**: Use `useState` for component-specific state

### Performance

- **Code Splitting**: Use `React.lazy()` for route-based splitting
- **Memoization**: Use `useMemo` and `useCallback` judiciously
- **React Query**: Leverage caching to reduce API calls
- **Avoid Re-renders**: Use `React.memo()` for expensive components

### Accessibility

- Use semantic HTML elements
- Add ARIA attributes when needed
- Ensure keyboard navigation works
- Test with screen readers

### Security

- Validate user input
- Sanitize HTML to prevent XSS
- Use HTTPS in production
- Don't store sensitive data in localStorage

## Troubleshooting

### Common Issues

**Development server not starting**:
```bash
# Clear node_modules and reinstall
rm -rf node_modules
pnpm install
```

**Type errors**:
```bash
# Re-run type checking
pnpm type-check
```

**Build fails**:
```bash
# Clean and rebuild
rm -rf dist
pnpm build
```

**Tests failing**:
```bash
# Run tests with verbose output
pnpm test:run --reporter=verbose
```

## Further Reading

- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [TanStack Query](https://tanstack.com/query/latest)
- [Zustand](https://zustand-demo.pmnd.rs/)
- [Vitest](https://vitest.dev/)
- [Testing Library](https://testing-library.com/)
