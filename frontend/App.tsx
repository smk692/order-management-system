
import React, { useState, createContext, useContext } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import DashboardView from './views/DashboardView';
import OrderView from './views/OrderView';
import InventoryView from './views/InventoryView';
import ProductView from './views/ProductView';
import ShippingView from './views/ShippingView';
import ClaimsView from './views/ClaimsView';
import SettlementView from './views/SettlementView';
import I18nView from './views/I18nView';
import MappingView from './views/MappingView';
import ProductMappingView from './views/ProductMappingView';
import InterfaceView from './views/InterfaceView';
import SettingsView from './views/SettingsView';
import AutomationView from './views/AutomationView';
import AIAssistant from './components/AIAssistant';
import { Language, Channel } from './types';
import { TRANSLATIONS } from './constants';

interface Warehouse {
  id: string;
  name: string;
  region: string;
  type: string;
  capacity: number;
}

interface GlobalDataContextType {
  channels: Channel[];
  warehouses: Warehouse[];
  setChannels: React.Dispatch<React.SetStateAction<Channel[]>>;
  setWarehouses: React.Dispatch<React.SetStateAction<Warehouse[]>>;
  isSidebarOpen: boolean;
  setIsSidebarOpen: (open: boolean) => void;
}

interface LanguageContextType {
  lang: Language;
  setLang: (l: Language) => void;
  t: (key: string) => string;
}

export const LanguageContext = createContext<LanguageContextType | undefined>(undefined);
export const GlobalDataContext = createContext<GlobalDataContextType | undefined>(undefined);

export const useTranslation = () => {
  const context = useContext(LanguageContext);
  if (!context) throw new Error('useTranslation must be used within a LanguageProvider');
  return context;
};

export const useGlobalData = () => {
  const context = useContext(GlobalDataContext);
  if (!context) throw new Error('useGlobalData must be used within a GlobalDataProvider');
  return context;
};

const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [lang, setLang] = useState<Language>('ko');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const [channels, setChannels] = useState<Channel[]>([
    { id: 'CH-001', name: '자사몰', type: 'D2C', status: 'CONNECTED', lastSync: '방금 전', logo: '🛍️' },
    { id: 'CH-002', name: '네이버', type: 'Market', status: 'CONNECTED', lastSync: '5분 전', logo: '🟢' },
    { id: 'CH-003', name: '쿠팡', type: 'Market', status: 'CONNECTED', lastSync: '12분 전', logo: '🚀' },
    { id: 'CH-004', name: '아마존 US', type: 'Global', status: 'ERROR', lastSync: '2시간 전', logo: '📦' },
    { id: 'CH-005', name: '쇼피 SG', type: 'Global', status: 'CONNECTED', lastSync: '1시간 전', logo: '🧡' },
  ]);

  const [warehouses, setWarehouses] = useState<Warehouse[]>([
    { id: 'WH-001', name: '김포 자동화 센터', region: '수도권', type: 'AUTO', capacity: 85 },
    { id: 'WH-002', name: '용인 메가 허브', region: '중부권', type: 'MEGA', capacity: 42 },
    { id: 'WH-003', name: '칠곡 물류 센터', region: '영남권', type: 'HUB', capacity: 60 },
    { id: 'WH-004', name: '인천 항공 센터', region: 'Global', type: 'AIR', capacity: 30 },
  ]);

  const t = (key: string) => (TRANSLATIONS[lang] as unknown as Record<string, string>)[key] || key;

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard': return <DashboardView />;
      case 'orders': return <OrderView />;
      case 'products': return <ProductView setActiveTab={setActiveTab} />;
      case 'inventory': return <InventoryView />;
      case 'shipping': return <ShippingView />;
      case 'claims': return <ClaimsView />;
      case 'settlement': return <SettlementView />;
      case 'i18n': return <I18nView />;
      case 'mapping': return <MappingView />;
      case 'product_mapping': return <ProductMappingView />;
      case 'interfaces': return <InterfaceView />;
      case 'settings': return <SettingsView />;
      case 'automation': return <AutomationView />;
      default: return null;
    }
  };

  const handleTabChange = (tabId: string) => {
    setActiveTab(tabId);
    setIsSidebarOpen(false); // 모달에서 탭 이동 시 사이드바 닫기
  };

  return (
    <LanguageContext.Provider value={{ lang, setLang, t }}>
      <GlobalDataContext.Provider value={{ channels, warehouses, setChannels, setWarehouses, isSidebarOpen, setIsSidebarOpen }}>
        <div className="min-h-screen bg-[#FDFDFF] flex font-pretendard">
          <Sidebar activeTab={activeTab} setActiveTab={handleTabChange} />
          
          <div className="flex-1 flex flex-col min-w-0 lg:ml-64 transition-all duration-300">
            <Header />
            <main className="flex-1 p-4 md:p-6 lg:p-10 overflow-x-hidden">
              <div className="max-w-[1400px] mx-auto animate-in fade-in duration-500">
                {renderContent()}
              </div>
            </main>
            <footer className="p-6 md:p-8 border-t border-slate-100 text-center bg-white/30 backdrop-blur-md">
              <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">&copy; 2025 Global OMS - Enterprise Solution</p>
            </footer>
          </div>
          <AIAssistant />
        </div>
      </GlobalDataContext.Provider>
    </LanguageContext.Provider>
  );
};

export default App;
