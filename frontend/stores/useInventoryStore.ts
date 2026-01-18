import { create } from 'zustand';
import { Inventory } from '../types';

// Data from InventoryView.tsx lines 16-39
const INITIAL_INVENTORY: Inventory[] = [
  {
    productId: 'SKU-0001',
    productName: '프리미엄 퀼팅 자켓',
    warehouse: '김포 자동화 센터',
    total: 1000,
    available: 850,
    reserved: 150,
    safetyStock: 200,
    status: 'NORMAL',
    channelBreakdown: { 'CH-001': 400, 'CH-002': 250, 'CH-003': 150 }
  },
  {
    productId: 'SKU-0002',
    productName: '베이직 코튼 팬츠',
    warehouse: '용인 메가 허브',
    total: 45,
    available: 32,
    reserved: 13,
    safetyStock: 50,
    status: 'LOW',
    channelBreakdown: { 'CH-001': 15, 'CH-002': 10 }
  },
];

interface InventoryState {
  inventory: Inventory[];
  setInventory: (inventory: Inventory[]) => void;
  updateInventory: (productId: string, updates: Partial<Inventory>) => void;
}

export const useInventoryStore = create<InventoryState>((set) => ({
  inventory: INITIAL_INVENTORY,
  setInventory: (inventory) => set({ inventory }),
  updateInventory: (productId, updates) => set((state) => ({
    inventory: state.inventory.map((item) =>
      item.productId === productId ? { ...item, ...updates } : item
    ),
  })),
}));
