import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '@/core/api/api';
import { queryKeys } from '@/core/api/queryClient';
import { Inventory } from '@/shared/types/types';

/**
 * Fetch all inventory items from the API
 */
const fetchInventory = async (): Promise<Inventory[]> => {
  const response = await api.get<Inventory[]>('/inventory');
  return response.data;
};

/**
 * Fetch a single inventory item by ID
 */
const fetchInventoryItem = async (id: string): Promise<Inventory> => {
  const response = await api.get<Inventory>(`/inventory/${id}`);
  return response.data;
};

/**
 * Adjust stock for an inventory item
 */
interface StockAdjustment {
  productId: string;
  warehouse: string;
  quantity: number;
  reason: string;
  type: 'increase' | 'decrease';
}

const adjustStock = async (adjustment: StockAdjustment): Promise<Inventory> => {
  const response = await api.post<Inventory>('/inventory/adjust', adjustment);
  return response.data;
};

/**
 * Hook to fetch all inventory items
 *
 * @returns Query result with inventory data, loading, and error states
 */
export const useInventory = () => {
  return useQuery({
    queryKey: queryKeys.inventory.all,
    queryFn: fetchInventory,
  });
};

/**
 * Hook to fetch a single inventory item by ID
 *
 * @param id - Inventory item ID (product ID)
 * @param enabled - Whether the query should run (default: true when id exists)
 * @returns Query result with inventory item data, loading, and error states
 */
export const useInventoryItem = (id: string | null, enabled = true) => {
  return useQuery({
    queryKey: queryKeys.inventory.detail(id || ''),
    queryFn: () => fetchInventoryItem(id!),
    enabled: enabled && !!id,
  });
};

/**
 * Hook to adjust stock for an inventory item
 *
 * @returns Mutation object with mutate function and status
 */
export const useAdjustStock = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: adjustStock,
    onSuccess: (data) => {
      // Invalidate inventory list to refetch after adjustment
      queryClient.invalidateQueries({ queryKey: queryKeys.inventory.all });
      // Invalidate the specific inventory item
      queryClient.invalidateQueries({ queryKey: queryKeys.inventory.detail(data.productId) });
    },
  });
};
