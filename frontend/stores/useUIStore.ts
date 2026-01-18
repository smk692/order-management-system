import { create } from 'zustand';

interface UIState {
  isSidebarOpen: boolean;
  activeTab: string;
  isGlobalSearchOpen: boolean;
  setSidebarOpen: (open: boolean) => void;
  setActiveTab: (tab: string) => void;
  setGlobalSearchOpen: (open: boolean) => void;
}

export const useUIStore = create<UIState>((set) => ({
  isSidebarOpen: false,
  activeTab: 'dashboard',
  isGlobalSearchOpen: false,
  setSidebarOpen: (open) => set({ isSidebarOpen: open }),
  setActiveTab: (tab) => set({ activeTab: tab, isSidebarOpen: false }),
  setGlobalSearchOpen: (open) => set({ isGlobalSearchOpen: open }),
}));
