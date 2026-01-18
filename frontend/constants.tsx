
import React from 'react';
import { 
  LayoutDashboard, Package, ShoppingCart, 
  Truck, ClipboardList, BarChart3, 
  Link2, Bell, Globe, Settings, 
  AlertCircle, CheckCircle2, Clock, 
  RotateCcw, XCircle, Zap, ShieldCheck, 
  Coins, FileText, Globe2, Scale, 
  ChevronRight, Landmark, BrainCircuit,
  GitMerge, Share2
} from 'lucide-react';
import { OrderStatus, StockStatus, Language, Translations } from './types';

export const COLORS = {
  primary: '#2563EB',
  secondary: '#1E40AF',
  success: '#10B981',
  warning: '#F59E0B',
  error: '#EF4444',
  info: '#06B6D4',
  background: '#F8FAFC',
};

export const TRANSLATIONS: Translations = {
  ko: {
    dashboard: '대시보드',
    orders: '주문 관리',
    products: '상품 마스터',
    inventory: '재고 관리',
    shipping: '배송 관리',
    claims: '클레임/반품',
    settlement: '정산 관리',
    automation: '자동화 엔진',
    interfaces: '인터페이스',
    i18n: '전략 센터',
    mapping: '채널-창고 매핑',
    product_mapping: '상품-채널 매핑',
    settings: '시스템 설정',
    operational_group: '운영 지휘소',
    strategy_group: '전략 및 지능',
    admin_group: '시스템 관리',
    customs_strategy: '글로벌 통관 전략',
    pricing_policy: '가격 및 마진 정책',
    operational_logic: '운영 로직 설정',
    save_changes: '전략 반영 및 저장',
    all_channels: '전체 채널',
    search_placeholder: '통합 검색 (Cmd+K)',
    invoice_naming: '인보이스 명칭 관리',
    legal_dg: '법적 규제 및 위험물',
    market_readiness: '마켓 진입 준비도',
    exchange_rate: '실시간 환율 현황'
  },
  en: {
    dashboard: 'Dashboard',
    orders: 'Orders',
    products: 'Product Master',
    inventory: 'Inventory',
    shipping: 'Shipping',
    claims: 'Claims',
    settlement: 'Settlement',
    automation: 'Automation',
    interfaces: 'Interfaces',
    i18n: 'Strategy Center',
    mapping: 'Channel Mapping',
    product_mapping: 'Product-Channel Map',
    settings: 'Settings',
    operational_group: 'Operations',
    strategy_group: 'Strategy & Intelligence',
    admin_group: 'Administration',
    customs_strategy: 'Global Customs',
    pricing_policy: 'Pricing Policy',
    operational_logic: 'Operational Logic',
    save_changes: 'Apply Strategy',
    all_channels: 'All Channels',
    search_placeholder: 'Global Search',
    invoice_naming: 'Invoice Naming',
    legal_dg: 'Legal & Compliance',
    market_readiness: 'Market Readiness',
    exchange_rate: 'Exchange Rates'
  }
};

export const MENU_GROUPS = [
  {
    id: 'group_op',
    labelKey: 'operational_group',
    items: [
      { id: 'dashboard', labelKey: 'dashboard', icon: <LayoutDashboard size={18} /> },
      { id: 'orders', labelKey: 'orders', icon: <ShoppingCart size={18} /> },
      { id: 'products', labelKey: 'products', icon: <Package size={18} /> },
      { id: 'inventory', labelKey: 'inventory', icon: <ClipboardList size={18} /> },
      { id: 'shipping', labelKey: 'shipping', icon: <Truck size={18} /> },
      { id: 'claims', labelKey: 'claims', icon: <RotateCcw size={18} /> },
    ]
  },
  {
    id: 'group_strategy',
    labelKey: 'strategy_group',
    items: [
      { id: 'i18n', labelKey: 'i18n', icon: <BrainCircuit size={18} /> },
      { id: 'product_mapping', labelKey: 'product_mapping', icon: <Share2 size={18} /> },
      { id: 'mapping', labelKey: 'mapping', icon: <GitMerge size={18} /> },
      { id: 'settlement', labelKey: 'settlement', icon: <BarChart3 size={18} /> },
    ]
  },
  {
    id: 'group_admin',
    labelKey: 'admin_group',
    items: [
      { id: 'automation', labelKey: 'automation', icon: <Zap size={18} /> },
      { id: 'interfaces', labelKey: 'interfaces', icon: <Link2 size={18} /> },
      { id: 'settings', labelKey: 'settings', icon: <Settings size={18} /> },
    ]
  }
];

export const ORDER_STATUS_MAP: Record<OrderStatus, { label: string; color: string; icon: React.ReactNode }> = {
  'NEW': { label: '신규주문', color: 'bg-blue-100 text-blue-700', icon: <Clock size={14} /> },
  'PAYMENT_PENDING': { label: '결제대기', color: 'bg-purple-100 text-purple-700', icon: <Clock size={14} /> },
  'PAID': { label: '결제완료', color: 'bg-indigo-100 text-indigo-700', icon: <CheckCircle2 size={14} /> },
  'PREPARING': { label: '상품준비중', color: 'bg-amber-100 text-amber-700', icon: <Package size={14} /> },
  'READY_TO_SHIP': { label: '출고대기', color: 'bg-cyan-100 text-cyan-700', icon: <Truck size={14} /> },
  'SHIPPED': { label: '출고완료', color: 'bg-cyan-100 text-cyan-700', icon: <CheckCircle2 size={14} /> },
  'IN_DELIVERY': { label: '배송중', color: 'bg-blue-100 text-blue-700', icon: <Truck size={14} /> },
  'DELIVERED': { label: '배송완료', color: 'bg-emerald-100 text-emerald-700', icon: <CheckCircle2 size={14} /> },
  'CANCELLED': { label: '주문취소', color: 'bg-rose-100 text-rose-700', icon: <XCircle size={14} /> },
  'EXCHANGE_REQUESTED': { label: '교환요청', color: 'bg-pink-100 text-pink-700', icon: <RotateCcw size={14} /> },
  'RETURN_REQUESTED': { label: '반품요청', color: 'bg-rose-100 text-rose-700', icon: <RotateCcw size={14} /> },
};

export const STOCK_STATUS_MAP: Record<StockStatus, { label: string; color: string }> = {
  'NORMAL': { label: '정상', color: 'text-emerald-500 bg-emerald-50 border-emerald-100' },
  'LOW': { label: '부족', color: 'text-amber-500 bg-amber-50 border-amber-100' },
  'OUT_OF_STOCK': { label: '품절', color: 'text-rose-500 bg-rose-50 border-rose-100' },
  'OVERSTOCK': { label: '과잉', color: 'text-cyan-500 bg-cyan-50 border-cyan-100' },
};
