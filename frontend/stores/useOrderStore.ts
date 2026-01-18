import { create } from 'zustand';
import { Order } from '../types';

// Data from OrderView.tsx lines 15-20
const INITIAL_ORDERS: Order[] = [
  { id: 'ORD-20250118-001', channel: 'MALL', orderDate: '2025-01-18 14:30', customerName: '홍길동', totalAmount: 39000, status: 'NEW', fulfillmentMethod: 'WMS', wmsNode: '김포 자동화 센터 (JD)', routingLogic: '재고 우선 배정', items: [] },
  { id: 'ORD-20250118-002', channel: 'NAVER', orderDate: '2025-01-18 13:15', customerName: '김철수', totalAmount: 128000, status: 'PREPARING', fulfillmentMethod: 'WMS', wmsNode: '용인 3PL (CJ)', routingLogic: '권역별 최단거리', items: [] },
  { id: 'ORD-20250118-010', channel: 'OFFLINE', orderDate: '2025-01-18 15:45', customerName: '이직송', totalAmount: 52000, status: 'NEW', fulfillmentMethod: 'DIRECT', routingLogic: '매장 직접 발송', items: [] },
  { id: 'ORD-20250117-089', channel: 'COUPANG', orderDate: '2025-01-17 18:40', customerName: '이영희', totalAmount: 69000, status: 'IN_DELIVERY', fulfillmentMethod: 'WMS', wmsNode: '쿠팡 밀크런 노드', routingLogic: '채널 전용 풀필먼트', items: [] },
];

interface OrderState {
  orders: Order[];
  selectedIds: string[];
  isLoading: boolean;
  setOrders: (orders: Order[]) => void;
  addOrder: (order: Order) => void;
  toggleSelect: (id: string) => void;
  selectAll: (ids: string[]) => void;
  clearSelection: () => void;
}

export const useOrderStore = create<OrderState>((set) => ({
  orders: INITIAL_ORDERS,
  selectedIds: [],
  isLoading: false,
  setOrders: (orders) => set({ orders }),
  addOrder: (order) => set((state) => ({ orders: [order, ...state.orders] })),
  toggleSelect: (id) => set((state) => ({
    selectedIds: state.selectedIds.includes(id)
      ? state.selectedIds.filter((sid) => sid !== id)
      : [...state.selectedIds, id],
  })),
  selectAll: (ids) => set({ selectedIds: ids }),
  clearSelection: () => set({ selectedIds: [] }),
}));
