import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/core/api/api';
import { queryKeys } from '@/core/api/queryClient';
import { Order } from '@/shared/types/types';

/**
 * Fetch all orders from the API
 */
const fetchOrders = async (): Promise<Order[]> => {
  const response = await api.get<Order[]>('/orders');
  return response.data;
};

/**
 * Fetch a single order by ID
 */
const fetchOrder = async (id: string): Promise<Order> => {
  const response = await api.get<Order>(`/orders/${id}`);
  return response.data;
};

/**
 * Create a new order
 */
const createOrder = async (orderData: Partial<Order>): Promise<Order> => {
  const response = await api.post<Order>('/orders', orderData);
  return response.data;
};

/**
 * Update an existing order
 */
const updateOrder = async ({ id, data }: { id: string; data: Partial<Order> }): Promise<Order> => {
  const response = await api.put<Order>(`/orders/${id}`, data);
  return response.data;
};

/**
 * Hook to fetch all orders
 *
 * @returns Query result with orders data, loading, and error states
 */
export const useOrders = () => {
  return useQuery({
    queryKey: queryKeys.orders.all,
    queryFn: fetchOrders,
  });
};

/**
 * Hook to fetch a single order by ID
 *
 * @param id - Order ID
 * @param enabled - Whether the query should run (default: true when id exists)
 * @returns Query result with order data, loading, and error states
 */
export const useOrder = (id: string | null, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.orders.detail(id || ''),
    queryFn: () => fetchOrder(id!),
    enabled: enabled && !!id,
  });
};

/**
 * Hook to create a new order
 *
 * @returns Mutation object with mutate function and status
 */
export const useCreateOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createOrder,
    onSuccess: () => {
      // Invalidate orders list to refetch after creation
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
    },
  });
};

/**
 * Hook to update an existing order
 *
 * @returns Mutation object with mutate function and status
 */
export const useUpdateOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateOrder,
    onSuccess: (data) => {
      // Invalidate both the list and the specific order
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.detail(data.id) });
    },
  });
};
