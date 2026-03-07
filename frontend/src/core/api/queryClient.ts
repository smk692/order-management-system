import { QueryClient } from '@tanstack/react-query'

/**
 * Configured QueryClient instance for React Query
 *
 * Default options:
 * - staleTime: 5 minutes - data considered fresh for 5 mins
 * - cacheTime: 10 minutes - unused data kept in cache for 10 mins
 * - retry: 1 - failed queries retry once before erroring
 * - refetchOnWindowFocus: false - don't auto-refetch on window focus
 */
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
})

/**
 * Query keys for consistent cache key management
 *
 * Usage:
 * - queryKeys.orders.all - ['orders']
 * - queryKeys.orders.detail(id) - ['orders', id]
 * - queryKeys.inventory.all - ['inventory']
 */
export const queryKeys = {
  orders: {
    all: ['orders'] as const,
    detail: (id: string) => ['orders', id] as const,
  },
  inventory: {
    all: ['inventory'] as const,
    detail: (id: string) => ['inventory', id] as const,
  },
  products: {
    all: ['products'] as const,
    detail: (id: string) => ['products', id] as const,
  },
  shipping: {
    all: ['shipping'] as const,
    detail: (id: string) => ['shipping', id] as const,
  },
  claims: {
    all: ['claims'] as const,
    detail: (id: string) => ['claims', id] as const,
  },
}
