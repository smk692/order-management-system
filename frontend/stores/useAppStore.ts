import { create } from 'zustand';
import { Channel } from '../types';

interface Warehouse {
  id: string;
  name: string;
  region: string;
  type: string;
  capacity: number;
}

interface AppState {
  channels: Channel[];
  warehouses: Warehouse[];
  setChannels: (channels: Channel[]) => void;
  setWarehouses: (warehouses: Warehouse[]) => void;
  updateChannel: (id: string, updates: Partial<Channel>) => void;
}

// Data from App.tsx lines 65-78
const INITIAL_CHANNELS: Channel[] = [
  { id: 'CH-001', name: '자사몰', type: 'D2C', status: 'CONNECTED', lastSync: '방금 전', logo: '🛍️' },
  { id: 'CH-002', name: '네이버', type: 'Market', status: 'CONNECTED', lastSync: '5분 전', logo: '🟢' },
  { id: 'CH-003', name: '쿠팡', type: 'Market', status: 'CONNECTED', lastSync: '12분 전', logo: '🚀' },
  { id: 'CH-004', name: '아마존 US', type: 'Global', status: 'ERROR', lastSync: '2시간 전', logo: '📦' },
  { id: 'CH-005', name: '쇼피 SG', type: 'Global', status: 'CONNECTED', lastSync: '1시간 전', logo: '🧡' },
];

const INITIAL_WAREHOUSES: Warehouse[] = [
  { id: 'WH-001', name: '김포 자동화 센터', region: '수도권', type: 'AUTO', capacity: 85 },
  { id: 'WH-002', name: '용인 메가 허브', region: '중부권', type: 'MEGA', capacity: 42 },
  { id: 'WH-003', name: '칠곡 물류 센터', region: '영남권', type: 'HUB', capacity: 60 },
  { id: 'WH-004', name: '인천 항공 센터', region: 'Global', type: 'AIR', capacity: 30 },
];

export const useAppStore = create<AppState>((set) => ({
  channels: INITIAL_CHANNELS,
  warehouses: INITIAL_WAREHOUSES,
  setChannels: (channels) => set({ channels }),
  setWarehouses: (warehouses) => set({ warehouses }),
  updateChannel: (id, updates) => set((state) => ({
    channels: state.channels.map((ch) => ch.id === id ? { ...ch, ...updates } : ch),
  })),
}));
