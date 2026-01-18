import { create } from 'zustand';
import { Product } from '../types';

// Data from ProductView.tsx lines 12-37
const INITIAL_PRODUCTS: Product[] = [
  {
    id: 'OMS-FG0015687674',
    sku: 'OUT-QLT-001',
    name: { ko: '[유통기한임박] 스텔라앤츄이스 독 로우 코티드 키블 10kg', en: "Stella & Chewy's Dog Food 10kg" },
    brand: "Stella & Chewy's",
    category: '반려동물 > 강아지 사료',
    basePrice: 89000,
    totalStock: 156,
    status: 'ACTIVE',
    uom: 'PCS',
    hsCode: '6103.42.0000',
    countryOfOrigin: '미국 (US)',
    materialContent: 'Chicken 90%',
    netWeight: 10,
    grossWeight: 11,
    dimensions: { width: 62, length: 39, height: 8, unit: 'cm' },
    barcodes: [{ code: 'A594195', isMain: true }],
    logisticsInfo: {
      tempMgmt: 'Normal', shelfLifeMgmt: true, snMgmt: false,
      isDangerous: false, isFragile: false, isHighValue: false, isNonStandard: false
    },
    manufacturerDetails: { name: "Stella & Chewy's LLC", address: 'Oak Creek, WI' },
    customsStrategies: []
  }
];

interface ProductState {
  products: Product[];
  selectedIds: string[];
  setProducts: (products: Product[]) => void;
  addProduct: (product: Product) => void;
  toggleSelect: (id: string) => void;
  clearSelection: () => void;
}

export const useProductStore = create<ProductState>((set) => ({
  products: INITIAL_PRODUCTS,
  selectedIds: [],
  setProducts: (products) => set({ products }),
  addProduct: (product) => set((state) => ({ products: [product, ...state.products] })),
  toggleSelect: (id) => set((state) => ({
    selectedIds: state.selectedIds.includes(id)
      ? state.selectedIds.filter((sid) => sid !== id)
      : [...state.selectedIds, id],
  })),
  clearSelection: () => set({ selectedIds: [] }),
}));
