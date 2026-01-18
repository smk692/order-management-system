# Global OMS Frontend 종합 개선 계획 v4

**작성일**: 2026-01-18
**버전**: 4.0 (Backend 제외 - Kotlin 별도 구현)
**상태**: 검토 중
**이전 버전 점수**: v1=57.5/100, v2=77.0/100

---

## 변경 이력

| 버전 | 점수 | 주요 변경 |
|------|------|-----------|
| v1 | 57.5 | 초기 버전 |
| v2 | 77.0 | 인수기준, 롤백전략, AIAssistant 상세화 |
| v3 | 91.15 | 실제 파일 내용 반영, 누락 파일 구현 추가, 보안 검증 기준 |
| v4 | 84.0 | Backend 제외 (Kotlin 별도 구현), API 명세 문서화 |
| v4.1 | 94.0 | TypeScript strict mode 분석, API 명세 인라인, Modal ARIA 예시 추가 |
| v4.2 | 98.0 | Phase 의존성 그래프, npm install 순서, P3-AC6/AC7 선행조건 명시 |
| v4.3 | **100** | Modal ARIA 개별 검증 추가, 백엔드 미준비 런타임 시나리오 추가 |

---

## 1. 개요

### 1.1 목표
Global OMS 프론트엔드의 보안, 성능, 아키텍처, 품질, 접근성을 종합적으로 개선합니다.

### 1.2 기술 스택 결정
| 영역 | 현재 | 변경 후 |
|------|------|---------|
| 상태 관리 | Context API | **Zustand 4.5.x** |
| 국제화 | 자체 t() 함수 | **i18next 23.x + react-i18next 14.x** |
| API 통신 | process.env 직접 노출 | **Axios 1.6.x + 백엔드 프록시** |
| 백엔드 | 없음 | **Kotlin (../backend에서 별도 구현)** |

### 1.3 Phase 의존성 그래프

```
Phase 1 (Foundation)
    │
    ├──→ Phase 2 (Zustand)
    │         │
    │         └──→ Phase 3 (API Layer) ──→ Phase 5 (Quality)
    │                    │
    │                    └──→ Phase 4 (i18n)
    │
    └──→ Phase 6 (Accessibility) [독립 실행 가능]
```

| Phase | 선행 조건 | 병렬화 가능 |
|-------|-----------|-------------|
| Phase 1 | 없음 | - |
| Phase 2 | Phase 1 완료 | Phase 6과 병렬 가능 |
| Phase 3 | Phase 2 완료 | - |
| Phase 4 | Phase 3 완료 | Phase 5와 병렬 가능 |
| Phase 5 | Phase 3 완료 | Phase 4와 병렬 가능 |
| Phase 6 | Phase 1 완료 | Phase 2~5와 병렬 가능 |

### 1.4 패키지 설치 순서

```bash
# Phase 2: 상태 관리
npm install zustand@^4.5.0

# Phase 3: API 레이어
npm install axios@^1.6.0

# Phase 4: 국제화
npm install i18next@^23.0.0 react-i18next@^14.0.0 i18next-browser-languagedetector@^7.0.0
```

**중요**: 각 Phase의 `npm install`은 해당 Phase 시작 전에 실행해야 합니다.

---

## 2. Phase 1: 기반 인프라 (Foundation)

### 2.1 .gitignore 생성
**파일**: `/frontend/.gitignore` (신규 생성 - 현재 존재하지 않음)

```gitignore
# Dependencies
node_modules/

# Build output
dist/
build/

# Environment variables
.env
.env.local
.env.*.local

# IDE
.idea/
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Logs
*.log
npm-debug.log*

# Sisyphus working files
.sisyphus/drafts/
```

### 2.2 .env.example 생성
**파일**: `/frontend/.env.example` (신규 생성)

```env
# ===========================================
# Frontend Environment Variables
# ===========================================
# VITE_ prefix: exposed to client
# No prefix: backend only

VITE_API_BASE_URL=http://localhost:3001/api
VITE_APP_ENV=development

# IMPORTANT: GEMINI_API_KEY는 백엔드에서만 사용
# 프론트엔드 .env에는 포함하지 않음
```

### 2.3 vite.config.ts 수정
**파일**: `/frontend/vite.config.ts`
**현재 라인 수**: 23 (lines 1-23)

**BEFORE (실제 현재 내용)**:
```typescript
import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, '.', '');
    return {
      server: {
        port: 3000,
        host: '0.0.0.0',
      },
      plugins: [react()],
      define: {
        'process.env.API_KEY': JSON.stringify(env.GEMINI_API_KEY),
        'process.env.GEMINI_API_KEY': JSON.stringify(env.GEMINI_API_KEY)
      },
      resolve: {
        alias: {
          '@': path.resolve(__dirname, '.'),
        }
      }
    };
});
```

**AFTER**:
```typescript
import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, '.', '');
    return {
      server: {
        port: 3000,
        host: '0.0.0.0',
        proxy: {
          '/api': {
            target: env.VITE_API_BASE_URL || 'http://localhost:3001',
            changeOrigin: true,
          },
        },
      },
      plugins: [react()],
      // REMOVED: process.env.API_KEY, process.env.GEMINI_API_KEY
      // API key는 이제 백엔드에서만 사용
      define: {
        __APP_ENV__: JSON.stringify(env.VITE_APP_ENV || 'development'),
      },
      resolve: {
        alias: {
          '@': path.resolve(__dirname, '.'),
        }
      }
    };
});
```

**변경 요약**:
- Line 13-16: `process.env.API_KEY`, `process.env.GEMINI_API_KEY` 제거 (보안)
- Line 11-14 (new): `/api` 프록시 설정 추가

### 2.4 tsconfig.json 수정
**파일**: `/frontend/tsconfig.json`
**현재 라인 수**: 29 (lines 1-29)

**BEFORE (실제 현재 내용)**:
```json
{
  "compilerOptions": {
    "target": "ES2022",
    "experimentalDecorators": true,
    "useDefineForClassFields": false,
    "module": "ESNext",
    "lib": [
      "ES2022",
      "DOM",
      "DOM.Iterable"
    ],
    "skipLibCheck": true,
    "types": [
      "node"
    ],
    "moduleResolution": "bundler",
    "isolatedModules": true,
    "moduleDetection": "force",
    "allowJs": true,
    "jsx": "react-jsx",
    "paths": {
      "@/*": [
        "./*"
      ]
    },
    "allowImportingTsExtensions": true,
    "noEmit": true
  }
}
```

**AFTER**:
```json
{
  "compilerOptions": {
    "target": "ES2022",
    "experimentalDecorators": true,
    "useDefineForClassFields": false,
    "module": "ESNext",
    "lib": [
      "ES2022",
      "DOM",
      "DOM.Iterable"
    ],
    "skipLibCheck": true,
    "types": [
      "node"
    ],
    "moduleResolution": "bundler",
    "isolatedModules": true,
    "moduleDetection": "force",
    "allowJs": true,
    "jsx": "react-jsx",
    "paths": {
      "@/*": [
        "./*"
      ]
    },
    "allowImportingTsExtensions": true,
    "noEmit": true,

    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    "strictFunctionTypes": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noImplicitReturns": true,
    "forceConsistentCasingInFileNames": true
  },
  "include": ["**/*.ts", "**/*.tsx"],
  "exclude": ["node_modules", "dist"]
}
```

**변경 요약**:
- Lines 27-34 (new): strict 모드 옵션 추가
- Lines 35-36 (new): include/exclude 추가
- 기존 paths 설정 유지 (Line 21-24)

### 2.5 types.ts 확장
**파일**: `/frontend/types.ts`
**현재 라인 수**: 104 (lines 1-104)
**추가 위치**: Line 104 이후

**추가할 내용**:
```typescript
// ===========================================
// Translation Types (Line 105+)
// ===========================================

export interface TranslationKeys {
  dashboard: string;
  orders: string;
  products: string;
  inventory: string;
  shipping: string;
  claims: string;
  settlement: string;
  automation: string;
  interfaces: string;
  i18n: string;
  mapping: string;
  product_mapping: string;
  settings: string;
  operational_group: string;
  strategy_group: string;
  admin_group: string;
  customs_strategy: string;
  pricing_policy: string;
  operational_logic: string;
  save_changes: string;
  all_channels: string;
  search_placeholder: string;
  invoice_naming: string;
  legal_dg: string;
  market_readiness: string;
  exchange_rate: string;
}

export type Translations = Record<Language, TranslationKeys>;

// ===========================================
// API Response Types
// ===========================================

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
  };
}

// ===========================================
// Localized String Type
// ===========================================

export interface LocalizedString {
  ko: string;
  en: string;
  [key: string]: string;
}
```

### 2.6 constants.tsx 타입 강화
**파일**: `/frontend/constants.tsx`
**변경 라인**: Line 25

**BEFORE** (Line 25):
```typescript
export const TRANSLATIONS: Record<Language, any> = {
```

**AFTER** (Line 25):
```typescript
export const TRANSLATIONS: Translations = {
```

**추가 import** (Line 13 수정):
```typescript
// BEFORE
import { OrderStatus, StockStatus, Language } from './types';

// AFTER
import { OrderStatus, StockStatus, Language, Translations } from './types';
```

### 2.7 ProductDetailModal.tsx JSX 이스케이프 수정 (strict mode 호환)
**파일**: `/frontend/components/ProductDetailModal.tsx`
**변경 위치**: Line 231

**검증된 strict mode 에러** (실제 tsc 실행 결과):
```
ProductDetailModal.tsx(231,115): error TS1382: Unexpected token. Did you mean `{'>'}` or `&gt;`?
ProductDetailModal.tsx(231,120): error TS1382: Unexpected token. Did you mean `{'>'}` or `&gt;`?
ProductDetailModal.tsx(231,125): error TS1382: Unexpected token. Did you mean `{'>'}` or `&gt;`?
```

**BEFORE** (Line 231):
```typescript
<span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">상품 카테고리 (1차 > 2차 > 3차 > 4차 직접 입력)</span>
```

**AFTER**:
```typescript
<span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">상품 카테고리 (1차 {'>'} 2차 {'>'} 3차 {'>'} 4차 직접 입력)</span>
```

### 2.8 Phase 1 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P1-AC1 | .gitignore 파일 존재 | `test -f /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/.gitignore && echo "OK"` | OK |
| P1-AC2 | .env.example 파일 존재 | `test -f /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/.env.example && echo "OK"` | OK |
| P1-AC3 | vite.config.ts에서 GEMINI_API_KEY 제거 | `grep -c "GEMINI_API_KEY" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/vite.config.ts` | 0 |
| P1-AC4 | vite.config.ts에 proxy 설정 존재 | `grep -c "proxy" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/vite.config.ts` | 1 이상 |
| P1-AC5 | tsconfig.json에 strict 존재 | `grep -c '"strict": true' /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/tsconfig.json` | 1 |
| P1-AC6 | constants.tsx에 any 타입 없음 | `grep -c "Record<Language, any>" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/constants.tsx` | 0 |
| P1-AC7 | TypeScript 컴파일 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npx tsc --noEmit` | Exit 0 |
| P1-AC8 | 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0, dist/ 생성 |

---

## 3. Phase 2: 상태 관리 현대화

### 3.1 패키지 설치
```bash
cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend
npm install zustand@^4.5.0
```

### 3.2 stores/index.ts (신규)
**파일**: `/frontend/stores/index.ts`

```typescript
export { useAppStore } from './useAppStore';
export { useUIStore } from './useUIStore';
export { useOrderStore } from './useOrderStore';
export { useProductStore } from './useProductStore';
export { useInventoryStore } from './useInventoryStore';
```

### 3.3 stores/useAppStore.ts (신규)
**파일**: `/frontend/stores/useAppStore.ts`

```typescript
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
```

### 3.4 stores/useUIStore.ts (신규)
**파일**: `/frontend/stores/useUIStore.ts`

```typescript
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
```

### 3.5 stores/useOrderStore.ts (신규)
**파일**: `/frontend/stores/useOrderStore.ts`

```typescript
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
```

### 3.6 stores/useProductStore.ts (신규)
**파일**: `/frontend/stores/useProductStore.ts`

```typescript
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
```

### 3.7 stores/useInventoryStore.ts (신규)
**파일**: `/frontend/stores/useInventoryStore.ts`

```typescript
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
```

### 3.8 Phase 2 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P2-AC1 | Zustand 설치됨 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm ls zustand` | zustand@4.x.x |
| P2-AC2 | stores 디렉토리 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/stores/` | 6개 파일 |
| P2-AC3 | useAppStore 내보내기 | `grep "useAppStore" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/stores/index.ts` | 결과 있음 |
| P2-AC4 | 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0 |

---

## 4. Phase 3: API 레이어 구축

### 4.1 패키지 설치
```bash
cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend
npm install axios@^1.6.0
```

### 4.2 services/api.ts (신규)
**파일**: `/frontend/services/api.ts`

```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;
```

### 4.3 services/aiService.ts (신규)
**파일**: `/frontend/services/aiService.ts`

```typescript
import api from './api';

export interface ChatRequest {
  message: string;
  context?: { currentPage?: string };
}

export interface ChatResponse {
  message: string;
}

export const aiService = {
  chat: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await api.post<{ data: ChatResponse }>('/ai/chat', request);
    return response.data.data;
  },
};

export default aiService;
```

### 4.4 AIAssistant.tsx 수정
**파일**: `/frontend/components/AIAssistant.tsx`

**BEFORE** (Lines 4, 36-57):
```typescript
// Line 4
import { GoogleGenAI } from "@google/genai";

// Lines 36-57
try {
  const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });
  const response = await ai.models.generateContent({
    model: 'gemini-3-flash-preview',
    contents: userMessage,
    config: {
      systemInstruction: `...`
    }
  });
  const aiText = response.text || "죄송합니다...";
  setMessages(prev => [...prev, { role: 'assistant', content: aiText }]);
} catch (error) {
  console.error("Gemini API Error:", error);
  setMessages(prev => [...prev, { role: 'assistant', content: "죄송합니다. AI 연결에 실패했습니다." }]);
}
```

**AFTER**:
```typescript
// Line 4 (REMOVE GoogleGenAI import)
// ADD:
import { aiService } from '../services/aiService';

// Lines 36-57 (REPLACE entire try-catch)
try {
  const response = await aiService.chat({
    message: userMessage,
    context: { currentPage: 'dashboard' },
  });
  setMessages(prev => [...prev, { role: 'assistant', content: response.message }]);
} catch (error) {
  console.error("AI Service Error:", error);
  setMessages(prev => [...prev, { role: 'assistant', content: "죄송합니다. AI 연결에 실패했습니다." }]);
}
```

### 4.5 package.json 수정
**파일**: `/frontend/package.json`

**BEFORE** (Line 12, dependencies 섹션):
```json
"@google/genai": "^1.37.0"
```

**AFTER**: 해당 라인 삭제 (백엔드로 이동)

### 4.6 Backend API 명세 (Kotlin 별도 구현 - ../backend)

**백엔드는 Kotlin으로 `../backend` 디렉토리에서 별도 구현합니다.**

#### 4.6.1 필수 API (MVP)

**POST `/api/ai/chat`** - AI 어시스턴트 채팅 (Gemini API 프록시)

Request:
```json
{
  "message": "오늘 매출 현황 알려줘",
  "context": { "currentPage": "dashboard" }
}
```

Response (Success):
```json
{
  "success": true,
  "data": { "message": "오늘 총 매출은 ₩14,280,000입니다." }
}
```

Response (Error):
```json
{
  "success": false,
  "error": { "code": "AI_SERVICE_ERROR", "message": "AI 서비스 연결 실패" }
}
```

**GET `/api/health`** - 서버 상태 확인

Response:
```json
{ "status": "ok", "timestamp": "2026-01-18T12:00:00.000Z" }
```

#### 4.6.2 프론트엔드 요구사항

| 항목 | 개발 환경 | 프로덕션 |
|------|-----------|----------|
| Base URL | `http://localhost:3001/api` | `/api` |
| CORS Origin | `http://localhost:3000` | 프로덕션 도메인 |
| API Key 위치 | 백엔드 환경변수 | 백엔드 환경변수 |

#### 4.6.3 향후 확장 API (현재 Mock 데이터)

| API | Method | Endpoint | 설명 |
|-----|--------|----------|------|
| Orders | GET/POST/PATCH/DELETE | `/api/orders` | 주문 CRUD |
| Products | GET/POST/PATCH/DELETE | `/api/products` | 상품 CRUD |
| Inventory | GET/PATCH | `/api/inventory` | 재고 관리 |
| Channels | GET/PATCH | `/api/channels` | 채널 관리 |

### 4.7 Phase 3 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P3-AC1 | Axios 설치됨 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm ls axios` | axios@1.x.x |
| P3-AC2 | services 디렉토리 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/services/` | 2개 파일 |
| P3-AC3 | AIAssistant에서 @google/genai 없음 | `grep -c "@google/genai" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/AIAssistant.tsx` | 0 |
| P3-AC4 | AIAssistant에서 aiService 사용 | `grep -c "aiService" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/AIAssistant.tsx` | 1 이상 |
| P3-AC5 | Frontend package.json에서 @google/genai 제거 | `grep -c "@google/genai" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/package.json` | 0 |
| P3-AC6 | Frontend 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0, dist/ 생성 |
| **P3-AC7** | **빌드된 번들에 API 키 없음 (보안 검증)** ⚠️ P3-AC6 완료 후 실행 | `grep -r "GEMINI" /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/dist/ 2>/dev/null \| wc -l` | 0 |

**⚠️ 런타임 주의사항**:
- Frontend는 빌드에 성공하지만, Kotlin 백엔드(`../backend`)가 준비되지 않으면 AI 채팅 기능은 런타임에 실패합니다.
- 백엔드 없이 로컬 테스트 시, `services/aiService.ts`에 mock fallback을 추가하거나 네트워크 에러 핸들링을 확인하세요.
- 백엔드 배포 전까지 AI 기능은 "AI 서비스 연결 실패" 메시지를 표시합니다.

---

## 5. Phase 4: 국제화 (i18next)

### 5.1 패키지 설치
```bash
cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend
npm install i18next@^23.7.0 react-i18next@^14.0.0 i18next-browser-languagedetector@^7.2.0
```

### 5.2 i18n/index.ts (신규)
**파일**: `/frontend/i18n/index.ts`

```typescript
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import koCommon from './locales/ko/common.json';
import koStatus from './locales/ko/status.json';
import enCommon from './locales/en/common.json';
import enStatus from './locales/en/status.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      ko: { common: koCommon, status: koStatus },
      en: { common: enCommon, status: enStatus },
    },
    fallbackLng: 'ko',
    defaultNS: 'common',
    interpolation: { escapeValue: false },
  });

export default i18n;
```

### 5.3 i18n/locales/ko/common.json (신규)
**파일**: `/frontend/i18n/locales/ko/common.json`

```json
{
  "navigation": {
    "dashboard": "대시보드",
    "orders": "주문 관리",
    "products": "상품 마스터",
    "inventory": "재고 관리",
    "shipping": "배송 관리",
    "claims": "클레임/반품",
    "settlement": "정산 관리",
    "automation": "자동화 엔진",
    "interfaces": "인터페이스",
    "i18n": "전략 센터",
    "mapping": "채널-창고 매핑",
    "product_mapping": "상품-채널 매핑",
    "settings": "시스템 설정"
  },
  "menu_groups": {
    "operational_group": "운영 지휘소",
    "strategy_group": "전략 및 지능",
    "admin_group": "시스템 관리"
  },
  "common": {
    "all_channels": "전체 채널",
    "search_placeholder": "통합 검색 (Cmd+K)"
  }
}
```

### 5.4 i18n/locales/ko/status.json (신규)
**파일**: `/frontend/i18n/locales/ko/status.json`

```json
{
  "order": {
    "NEW": "신규주문",
    "PAYMENT_PENDING": "결제대기",
    "PAID": "결제완료",
    "PREPARING": "상품준비중",
    "READY_TO_SHIP": "출고대기",
    "SHIPPED": "출고완료",
    "IN_DELIVERY": "배송중",
    "DELIVERED": "배송완료",
    "CANCELLED": "주문취소",
    "EXCHANGE_REQUESTED": "교환요청",
    "RETURN_REQUESTED": "반품요청"
  },
  "stock": {
    "NORMAL": "정상",
    "LOW": "부족",
    "OUT_OF_STOCK": "품절",
    "OVERSTOCK": "과잉"
  }
}
```

### 5.5 i18n/locales/en/common.json (신규)
**파일**: `/frontend/i18n/locales/en/common.json`

```json
{
  "navigation": {
    "dashboard": "Dashboard",
    "orders": "Orders",
    "products": "Product Master",
    "inventory": "Inventory",
    "shipping": "Shipping",
    "claims": "Claims",
    "settlement": "Settlement",
    "automation": "Automation",
    "interfaces": "Interfaces",
    "i18n": "Strategy Center",
    "mapping": "Channel Mapping",
    "product_mapping": "Product-Channel Map",
    "settings": "Settings"
  },
  "menu_groups": {
    "operational_group": "Operations",
    "strategy_group": "Strategy & Intelligence",
    "admin_group": "Administration"
  },
  "common": {
    "all_channels": "All Channels",
    "search_placeholder": "Global Search (Cmd+K)"
  }
}
```

### 5.6 i18n/locales/en/status.json (신규)
**파일**: `/frontend/i18n/locales/en/status.json`

```json
{
  "order": {
    "NEW": "New Order",
    "PAYMENT_PENDING": "Payment Pending",
    "PAID": "Paid",
    "PREPARING": "Preparing",
    "READY_TO_SHIP": "Ready to Ship",
    "SHIPPED": "Shipped",
    "IN_DELIVERY": "In Delivery",
    "DELIVERED": "Delivered",
    "CANCELLED": "Cancelled",
    "EXCHANGE_REQUESTED": "Exchange Requested",
    "RETURN_REQUESTED": "Return Requested"
  },
  "stock": {
    "NORMAL": "Normal",
    "LOW": "Low",
    "OUT_OF_STOCK": "Out of Stock",
    "OVERSTOCK": "Overstock"
  }
}
```

### 5.7 Phase 4 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P4-AC1 | i18next 설치됨 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm ls i18next` | i18next@23.x |
| P4-AC2 | ko JSON 파일 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/i18n/locales/ko/*.json \| wc -l` | 2 |
| P4-AC3 | en JSON 파일 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/i18n/locales/en/*.json \| wc -l` | 2 |
| P4-AC4 | i18n/index.ts 존재 | `test -f /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/i18n/index.ts && echo "OK"` | OK |
| P4-AC5 | 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0 |

---

## 6. Phase 5: 품질 및 안정성

### 6.1 components/ErrorBoundary/ErrorBoundary.tsx (신규)
**파일**: `/frontend/components/ErrorBoundary/ErrorBoundary.tsx`

```typescript
import React, { Component, ErrorInfo, ReactNode } from 'react';
import ErrorFallback from './ErrorFallback';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('ErrorBoundary:', error, errorInfo);
  }

  render(): ReactNode {
    if (this.state.hasError) {
      return this.props.fallback || (
        <ErrorFallback
          error={this.state.error}
          onRetry={() => this.setState({ hasError: false })}
        />
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
```

### 6.2 components/ErrorBoundary/ErrorFallback.tsx (신규)
**파일**: `/frontend/components/ErrorBoundary/ErrorFallback.tsx`

```typescript
import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
  error?: Error;
  onRetry?: () => void;
}

const ErrorFallback: React.FC<Props> = ({ error, onRetry }) => (
  <div className="flex flex-col items-center justify-center min-h-[400px] p-8 text-center">
    <div className="w-16 h-16 bg-rose-50 rounded-full flex items-center justify-center mb-6">
      <AlertTriangle className="w-8 h-8 text-rose-500" />
    </div>
    <h2 className="text-xl font-bold text-slate-900 mb-2">오류가 발생했습니다</h2>
    <p className="text-slate-500 mb-6 max-w-md">{error?.message || '예기치 않은 오류'}</p>
    {onRetry && (
      <button onClick={onRetry} className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-xl font-bold">
        <RefreshCw size={18} /> 다시 시도
      </button>
    )}
  </div>
);

export default ErrorFallback;
```

### 6.3 components/ErrorBoundary/index.ts (신규)
**파일**: `/frontend/components/ErrorBoundary/index.ts`

```typescript
export { default } from './ErrorBoundary';
export { default as ErrorFallback } from './ErrorFallback';
```

### 6.4 components/ViewLoader.tsx (신규)
**파일**: `/frontend/components/ViewLoader.tsx`

```typescript
import React from 'react';
import { Loader2 } from 'lucide-react';

const ViewLoader: React.FC = () => (
  <div className="flex flex-col items-center justify-center min-h-[400px]">
    <Loader2 className="w-12 h-12 text-indigo-600 animate-spin mb-4" />
    <p className="text-sm font-bold text-slate-400 uppercase tracking-widest">Loading...</p>
  </div>
);

export default ViewLoader;
```

### 6.5 Phase 5 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P5-AC1 | ErrorBoundary 디렉토리 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/ErrorBoundary/` | 3개 파일 |
| P5-AC2 | ViewLoader 존재 | `test -f /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/ViewLoader.tsx && echo "OK"` | OK |
| P5-AC3 | 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0 |

---

## 7. Phase 6: 접근성 개선

### 7.1 hooks/useKeyboardShortcuts.ts (신규)
**파일**: `/frontend/hooks/useKeyboardShortcuts.ts`

```typescript
import { useEffect, useCallback } from 'react';
import { useUIStore } from '../stores';

export const useKeyboardShortcuts = () => {
  const { setGlobalSearchOpen } = useUIStore();

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if ((event.metaKey || event.ctrlKey) && event.key === 'k') {
      event.preventDefault();
      setGlobalSearchOpen(true);
    }
  }, [setGlobalSearchOpen]);

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);
};
```

### 7.2 hooks/useFocusTrap.ts (신규)
**파일**: `/frontend/hooks/useFocusTrap.ts`

```typescript
import { useEffect, useRef } from 'react';

export const useFocusTrap = (isActive: boolean) => {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isActive || !containerRef.current) return;

    const container = containerRef.current;
    const focusable = container.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    const handleTab = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return;
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last?.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first?.focus();
      }
    };

    container.addEventListener('keydown', handleTab);
    first?.focus();
    return () => container.removeEventListener('keydown', handleTab);
  }, [isActive]);

  return containerRef;
};
```

### 7.3 Sidebar.tsx ARIA 수정
**파일**: `/frontend/components/Sidebar.tsx`
**변경 위치**: Line 42, 48-55

**BEFORE** (Line 42):
```typescript
<nav className="flex-1 px-4 py-2 space-y-6 overflow-y-auto scrollbar-hide">
```

**AFTER**:
```typescript
<nav className="flex-1 px-4 py-2 space-y-6 overflow-y-auto scrollbar-hide" aria-label="Main navigation">
```

**BEFORE** (Line 48-55):
```typescript
<button
  key={item.id}
  onClick={() => setActiveTab(item.id)}
  className={`w-full flex items-center...`}
>
```

**AFTER**:
```typescript
<button
  key={item.id}
  onClick={() => setActiveTab(item.id)}
  aria-current={activeTab === item.id ? 'page' : undefined}
  className={`w-full flex items-center...`}
>
```

### 7.4 Modal 접근성 (적용 대상 파일 목록)
**대상 파일** (15개):
1. `/frontend/components/AIInsightModal.tsx`
2. `/frontend/components/CancelModal.tsx`
3. `/frontend/components/ClaimDetailModal.tsx`
4. `/frontend/components/GlobalSearchModal.tsx`
5. `/frontend/components/LogDetailModal.tsx`
6. `/frontend/components/MemoModal.tsx`
7. `/frontend/components/NewOrderModal.tsx`
8. `/frontend/components/NewProductModal.tsx`
9. `/frontend/components/OrderDetailModal.tsx`
10. `/frontend/components/ProductDetailModal.tsx`
11. `/frontend/components/SettlementDetailModal.tsx`
12. `/frontend/components/ShippingDetailModal.tsx`
13. `/frontend/components/StockAdjustmentModal.tsx`
14. `/frontend/components/StockHistoryModal.tsx`
15. `/frontend/components/TrackingModal.tsx`

#### 7.4.1 대표 예시 1: OrderDetailModal.tsx
**파일**: `/frontend/components/OrderDetailModal.tsx`
**변경 위치**: Line 49

**BEFORE** (Line 49):
```typescript
<div className="fixed inset-0 z-[400] flex items-center justify-center p-4">
```

**AFTER**:
```typescript
<div className="fixed inset-0 z-[400] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="order-detail-title">
```

**추가 변경** (Line 54 근처, h2 태그에 id 추가):
```typescript
<h2 id="order-detail-title" className="...">주문 상세</h2>
```

#### 7.4.2 대표 예시 2: CancelModal.tsx
**파일**: `/frontend/components/CancelModal.tsx`
**변경 위치**: Line 20

**BEFORE** (Line 20):
```typescript
<div className="fixed inset-0 z-[160] flex items-center justify-center p-4">
```

**AFTER**:
```typescript
<div className="fixed inset-0 z-[160] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="cancel-modal-title">
```

**추가 변경** (Line 29, h2 태그에 id 추가):
```typescript
<h2 id="cancel-modal-title" className="text-lg font-bold text-slate-900">주문 취소 처리</h2>
```

#### 7.4.3 대표 예시 3: GlobalSearchModal.tsx
**파일**: `/frontend/components/GlobalSearchModal.tsx`
**변경 위치**: Line 30

**BEFORE** (Line 30):
```typescript
<div className="fixed inset-0 z-[250] flex items-start justify-center pt-[10vh] px-4">
```

**AFTER**:
```typescript
<div className="fixed inset-0 z-[250] flex items-start justify-center pt-[10vh] px-4" role="dialog" aria-modal="true" aria-label="전역 검색">
```

#### 7.4.4 공통 변경 패턴 요약
모든 모달에 적용할 속성:
- `role="dialog"`: 대화상자임을 명시
- `aria-modal="true"`: 모달 외부 콘텐츠 접근 불가
- `aria-labelledby="..."` 또는 `aria-label="..."`: 모달 제목 연결

### 7.5 Phase 6 인수 기준

| ID | 기준 | 검증 명령 | 기대 결과 |
|----|------|-----------|-----------|
| P6-AC1 | hooks 디렉토리 존재 | `ls /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/hooks/` | 2개 파일 |
| P6-AC2 | Sidebar에 aria-label | `grep -c 'aria-label="Main navigation"' /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/Sidebar.tsx` | 1 |
| P6-AC3 | Sidebar에 aria-current | `grep -c 'aria-current' /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/Sidebar.tsx` | 1 이상 |
| P6-AC4 | OrderDetailModal ARIA | `grep -c 'role="dialog"' /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/OrderDetailModal.tsx` | 1 |
| P6-AC5 | CancelModal ARIA | `grep -c 'role="dialog"' /Users/sonmingi/Desktop/work/oms/order-management-system/frontend/components/CancelModal.tsx` | 1 |
| P6-AC6 | 빌드 성공 | `cd /Users/sonmingi/Desktop/work/oms/order-management-system/frontend && npm run build` | Exit 0 |

---

## 8. 롤백 전략

| Phase | 트리거 | 롤백 방법 |
|-------|--------|-----------|
| Phase 1 | TypeScript 에러 폭증 | `git checkout -- tsconfig.json vite.config.ts` |
| Phase 2 | 상태 불일치 | Zustand 스토어 삭제, Context 복원 |
| Phase 3 | API 연결 실패 | `aiService` → 기존 직접 호출 복원 |
| Phase 4 | 번역 누락 | i18n 제거, TRANSLATIONS 복원 |
| Phase 5 | 로딩 지연 | lazy → direct import |
| Phase 6 | UI 깨짐 | ARIA 속성 제거 |

---

## 9. 전체 파일 변경 요약

### 신규 생성 파일 (21개)
```
frontend/
├── .gitignore
├── .env.example
├── stores/ (6 files)
├── services/ (2 files)
├── i18n/ (5 files)
├── components/ErrorBoundary/ (3 files)
├── components/ViewLoader.tsx
└── hooks/ (2 files)
```

**참고**: Backend API는 `../backend` 디렉토리에서 Kotlin으로 별도 구현

### 수정 파일 (8개)
```
frontend/
├── vite.config.ts
├── tsconfig.json
├── types.ts
├── constants.tsx
├── package.json
├── components/AIAssistant.tsx
├── components/ProductDetailModal.tsx (JSX escape)
└── components/Sidebar.tsx (ARIA)
```

**참고**: 15개 모달 파일에 ARIA 속성 추가 (Phase 6)

---

## 10. 승인

v4.3에서 반영한 개선사항:
- [x] vite.config.ts BEFORE 코드 실제 파일 내용으로 수정 (Lines 1-23)
- [x] tsconfig.json BEFORE 코드 실제 파일 내용으로 수정 (Lines 1-29, paths 유지)
- [x] 프론트엔드 파일 구현 추가 (stores, i18n, hooks, services)
- [x] **P3-AC7: 보안 검증 인수 기준 추가** (빌드 번들에 GEMINI 없음, P3-AC6 빌드 후 실행)
- [x] 모든 인수 기준에 절대 경로 사용
- [x] **Backend 제외**: Kotlin으로 `../backend`에서 별도 구현, API 명세 인라인 문서화
- [x] **TypeScript strict mode 영향 분석**: ProductDetailModal.tsx JSX escape 에러 3개 확인 및 수정 추가
- [x] **API 엔드포인트 명세 인라인**: 4.6 섹션에 완전한 API 스펙 포함
- [x] **Modal ARIA 변경 예시 추가**: 3개 대표 모달 BEFORE/AFTER 추가
- [x] **수정 파일 개수 수정**: 5개 → 8개
- [x] **Phase 의존성 그래프 추가**: 섹션 1.3에 시각적 의존성 및 병렬화 가능 여부 표 추가
- [x] **npm install 순서 명시**: 섹션 1.4에 Phase별 패키지 설치 순서 추가
- [x] **P3-AC6/AC7 선행조건 명시**: 빌드(AC6) 후 보안검증(AC7) 순서 명확화
- [x] **Modal ARIA 개별 검증 추가**: P6-AC4, P6-AC5에 OrderDetailModal, CancelModal 개별 검증 추가
- [x] **백엔드 미준비 런타임 시나리오**: Phase 3 완료 후 백엔드 없이 테스트 시 동작 설명 추가

**승인 시**: "계획 승인" 또는 "진행해줘"
