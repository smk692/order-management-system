
import React, { useState } from 'react';
import { 
  Search, TrendingUp, History, Edit3, Settings, 
  Package, AlertTriangle, Layers, Globe, Filter,
  ChevronRight, ArrowRight, Sparkles, BarChart3, Database,
  Activity
} from 'lucide-react';
import { Inventory } from '../types';
import { useGlobalData } from '../App';
import { STOCK_STATUS_MAP } from '../constants';
import AIInsightModal from '../components/AIInsightModal';
import StockHistoryModal from '../components/StockHistoryModal'; 
import StockAdjustmentModal from '../components/StockAdjustmentModal';

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

const InventoryView: React.FC = () => {
  const { channels, warehouses } = useGlobalData();
  const [inventory, setInventory] = useState<Inventory[]>(INITIAL_INVENTORY);
  const [selectedInventory, setSelectedInventory] = useState<Inventory | null>(null);
  const [historyProductName, setHistoryProductName] = useState<string | null>(null);
  const [showAIModal, setShowAIModal] = useState(false);
  const [activeChannelFilter, setActiveChannelFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-8 relative pb-32">
      {/* Search Header Section */}
      <div className="bg-white p-8 rounded-[2.5rem] border border-slate-200 shadow-sm space-y-8 transition-all">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="space-y-2">
            <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3 italic uppercase">
              Inventory Command
            </h1>
            <p className="text-slate-500 font-medium text-sm">다중 물류 거점의 물리 재고와 채널별 가상 재고를 통합 지휘합니다.</p>
          </div>
          <div className="flex items-center gap-2">
            <button 
              onClick={() => setShowAIModal(true)}
              className="flex items-center gap-3 px-6 py-3 bg-indigo-600 text-white rounded-2xl text-sm font-black hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-500/20 active:scale-95"
            >
              <TrendingUp size={20} /> AI 재고 최적화
            </button>
          </div>
        </div>

        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative group">
            <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors" size={20} />
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="상품명, SKU, 또는 창고 위치를 검색하세요..." 
              className="w-full pl-14 pr-6 py-4 bg-slate-50 border border-transparent rounded-[1.5rem] text-sm font-bold outline-none focus:bg-white focus:ring-4 focus:ring-indigo-500/5 focus:border-indigo-200 transition-all shadow-inner" 
            />
          </div>
          <div className="flex bg-slate-100 p-1.5 rounded-[1.5rem] border border-slate-200 overflow-x-auto no-scrollbar max-w-full">
             <button onClick={() => setActiveChannelFilter('ALL')} className={`px-5 py-2.5 rounded-2xl text-[10px] font-black transition-all whitespace-nowrap ${activeChannelFilter === 'ALL' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-400'}`}>전체 창고</button>
             {channels.map(ch => (
               <button 
                key={ch.id}
                onClick={() => setActiveChannelFilter(ch.id)}
                className={`px-5 py-2.5 rounded-2xl text-[10px] font-black transition-all whitespace-nowrap flex items-center gap-2 ${activeChannelFilter === ch.id ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-400'}`}
               >
                 <span>{ch.logo}</span> {ch.name}
               </button>
             ))}
          </div>
        </div>
      </div>

      {/* Primary Inventory Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 px-2">
        {[
          { label: '전체 물리 재고', value: '14,204 EA', icon: <Database size={20} />, color: 'text-indigo-600 bg-indigo-50' },
          { label: '품절 임박 SKU', value: '12건', icon: <AlertTriangle size={20} />, color: 'text-rose-600 bg-rose-50' },
          { label: '당일 재고 변동', value: '+342건', icon: <Activity size={20} />, color: 'text-emerald-600 bg-emerald-50' },
          { label: '창고 가동률', value: '68.4%', icon: <BarChart3 size={20} />, color: 'text-slate-600 bg-slate-50' },
        ].map((stat, i) => (
          <div key={i} className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm group hover:border-indigo-400 transition-all">
            <div className="flex items-center justify-between mb-4">
              <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${stat.color} transition-transform group-hover:scale-110`}>
                {stat.icon}
              </div>
            </div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{stat.label}</p>
            <p className="text-2xl font-black text-slate-900 mt-1">{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Inventory Main Content Area */}
      <div className="bg-white rounded-[2.5rem] border border-slate-200 shadow-sm overflow-hidden animate-in fade-in duration-500">
        <div className="overflow-x-auto scrollbar-hide">
          <table className="w-full text-left min-w-[1000px]">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50/50">
                <th className="py-6 px-8 text-[11px] font-black text-slate-400 uppercase tracking-widest whitespace-nowrap">Product Master Info</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center whitespace-nowrap">Warehouse Node</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest whitespace-nowrap text-center">Status</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest whitespace-nowrap">Channel Allocation Breakdown</th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest text-right whitespace-nowrap">Available</th>
                <th className="py-6 px-8 w-28"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {inventory.map((item) => {
                const statusInfo = STOCK_STATUS_MAP[item.status];
                return (
                  <tr key={item.productId} className="hover:bg-slate-50/80 transition-all group">
                    <td className="py-6 px-8">
                      <div className="flex flex-col">
                        <span className="text-[10px] font-black text-indigo-600 uppercase mb-0.5 italic">{item.productId}</span>
                        <span className="text-sm font-black text-slate-900 group-hover:text-indigo-600 cursor-pointer transition-colors" onClick={() => setSelectedInventory(item)}>
                          {item.productName}
                        </span>
                      </div>
                    </td>
                    <td className="py-6 px-4 text-center">
                      <span className="text-[10px] font-black text-slate-500 px-3 py-1.5 bg-slate-100 border border-slate-200 rounded-xl uppercase tracking-tighter whitespace-nowrap">
                        {item.warehouse}
                      </span>
                    </td>
                    <td className="py-6 px-4 text-center">
                      <span className={`px-3 py-1.5 rounded-xl text-[10px] font-black border uppercase tracking-widest ${statusInfo.color}`}>
                        {statusInfo.label}
                      </span>
                    </td>
                    <td className="py-6 px-4 min-w-[320px]">
                      <div className="space-y-3">
                        <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden flex shadow-inner">
                           {Object.entries(item.channelBreakdown).map(([chId, val], idx) => (
                             <div 
                              key={chId} 
                              className={`h-full opacity-90 ${idx % 2 === 0 ? 'bg-indigo-600' : 'bg-emerald-500'}`} 
                              style={{ width: `${((val as number) / item.total) * 100}%` }}
                             ></div>
                           ))}
                        </div>
                        <div className="flex flex-wrap items-center gap-3">
                          {Object.entries(item.channelBreakdown).map(([chId, val]) => {
                            const channel = channels.find(c => c.id === chId);
                            return (
                              <div key={chId} className="flex items-center gap-1.5">
                                <span className="text-xs font-black text-slate-800">{channel?.logo}</span>
                                <span className="text-[9px] font-bold text-slate-400 uppercase tracking-tighter">
                                  {channel?.name || chId}: <span className="text-slate-900 font-black">{val}</span>
                                </span>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    </td>
                    <td className="py-6 px-6 text-right">
                      <p className="text-sm font-black text-slate-900 italic tracking-tight">{item.available.toLocaleString()} EA</p>
                      <p className="text-[10px] font-bold text-slate-300 uppercase tracking-widest mt-0.5">Safety: {item.safetyStock}</p>
                    </td>
                    <td className="py-6 px-8 text-right">
                      <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <button onClick={() => setSelectedInventory(item)} className="p-2.5 text-slate-400 hover:text-indigo-600 rounded-xl bg-white border border-slate-100 shadow-sm transition-all"><Edit3 size={16} /></button>
                        <button onClick={() => setHistoryProductName(item.productName)} className="p-2.5 text-slate-400 hover:text-slate-900 rounded-xl bg-white border border-slate-100 shadow-sm transition-all"><History size={16} /></button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      <AIInsightModal isOpen={showAIModal} onClose={() => setShowAIModal(false)} />
      {historyProductName && <StockHistoryModal isOpen={!!historyProductName} onClose={() => setHistoryProductName(null)} productName={historyProductName} />}
      <StockAdjustmentModal 
        isOpen={!!selectedInventory} 
        onClose={() => setSelectedInventory(null)} 
        inventory={selectedInventory}
        onSave={(updated) => setInventory(prev => prev.map(i => i.productId === updated.productId ? updated : i))}
      />
    </div>
  );
};

export default InventoryView;
