
import React, { useState, useEffect } from 'react';
import { 
  X, Save, Package, Box, RefreshCw, AlertCircle, 
  Plus, Minus, Info, Globe, ShieldCheck, Zap, Loader2,
  AlertTriangle
} from 'lucide-react';
import { Inventory } from '../types';

interface StockAdjustmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  inventory: Inventory | null;
  onSave: (updated: Inventory) => void;
}

const REASONS = [
  '정기 재고 실사', '입고 확인', '불량/파손 처리', 
  '사은품 목적 차감', '채널별 물량 재배분', '기타 사유'
];

const StockAdjustmentModal: React.FC<StockAdjustmentModalProps> = ({ isOpen, onClose, inventory, onSave }) => {
  const [activeTab, setActiveTab] = useState<'warehouse' | 'channels'>('warehouse');
  const [tempTotal, setTempTotal] = useState(0);
  const [tempBreakdown, setTempBreakdown] = useState<Record<string, number>>({});
  const [reason, setReason] = useState(REASONS[0]);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (inventory) {
      setTempTotal(inventory.total);
      setTempBreakdown({ ...inventory.channelBreakdown });
    }
  }, [inventory]);

  if (!isOpen || !inventory) return null;

  // Fix: Explicitly type reduce to fix '+' operator error and arithmetic type error
  const totalAllocated = Object.values(tempBreakdown).reduce((a: number, b: number) => a + (b as number), 0);
  const availableForAllocation = tempTotal - inventory.reserved;
  const remainingStock = availableForAllocation - (totalAllocated as number);
  const isOverAllocated = remainingStock < 0;

  const handleSave = async () => {
    if (isOverAllocated) {
      alert("할당된 재고의 합이 가용 재고를 초과할 수 없습니다.");
      return;
    }
    
    setIsSaving(true);
    // API Sync Simulation
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    onSave({
      ...inventory,
      total: tempTotal,
      available: availableForAllocation,
      channelBreakdown: tempBreakdown
    });
    
    setIsSaving(false);
    alert("재고 수정 사항이 채널에 반영되었습니다.");
  };

  const adjustChannel = (ch: string, delta: number) => {
    setTempBreakdown(prev => ({
      ...prev,
      [ch]: Math.max(0, (prev[ch] || 0) + delta)
    }));
  };

  return (
    <div className="fixed inset-0 z-[160] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-md" onClick={onClose}></div>
      
      <div className="relative bg-white w-full max-w-4xl rounded-[3rem] shadow-2xl overflow-hidden flex flex-col max-h-[90vh] animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-white">
          <div className="flex items-center gap-5">
            <div className="w-14 h-14 bg-indigo-600 rounded-2xl flex items-center justify-center text-white shadow-xl shadow-indigo-500/20">
              <RefreshCw size={28} className={isSaving ? 'animate-spin' : ''} />
            </div>
            <div>
              <h2 className="text-2xl font-black text-slate-900 tracking-tight">재고 수정 및 채널 할당</h2>
              <p className="text-xs text-slate-400 font-bold tracking-widest uppercase mt-1">
                {inventory.productId} | {inventory.productName}
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-3 hover:bg-slate-100 rounded-2xl text-slate-400"><X size={28} /></button>
        </div>

        {/* Tab Switcher */}
        <div className="flex px-8 border-b border-slate-100 bg-slate-50/50">
          <button
            onClick={() => setActiveTab('warehouse')}
            className={`flex items-center gap-2 px-8 py-5 text-sm font-black transition-all relative ${
              activeTab === 'warehouse' ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
            }`}
          >
            <Box size={16} /> 창고 재고 조정
            {activeTab === 'warehouse' && <div className="absolute bottom-0 left-0 right-0 h-1 bg-indigo-600 rounded-t-full"></div>}
          </button>
          <button
            onClick={() => setActiveTab('channels')}
            className={`flex items-center gap-2 px-8 py-5 text-sm font-black transition-all relative ${
              activeTab === 'channels' ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'
            }`}
          >
            <Globe size={16} /> 채널별 판매 재고 할당
            {activeTab === 'channels' && <div className="absolute bottom-0 left-0 right-0 h-1 bg-indigo-600 rounded-t-full"></div>}
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-10 space-y-10">
          {activeTab === 'warehouse' && (
            <div className="space-y-10 animate-in fade-in slide-in-from-left-4">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
                <div className="space-y-6">
                  <div className="p-8 bg-slate-50 rounded-[2.5rem] border border-slate-100 space-y-6">
                    <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                      <Package size={14}/> 물리적 총 재고 수정
                    </label>
                    <div className="flex items-center gap-6">
                      <button onClick={() => setTempTotal(prev => Math.max(0, prev - 10))} className="w-14 h-14 rounded-2xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-rose-500 hover:border-rose-200 transition-all shadow-sm active:scale-95"><Minus size={24}/></button>
                      <input 
                        type="number" 
                        value={tempTotal}
                        onChange={(e) => setTempTotal(parseInt(e.target.value) || 0)}
                        className="flex-1 text-center text-4xl font-black text-slate-900 bg-transparent border-none outline-none focus:ring-0"
                      />
                      <button onClick={() => setTempTotal(prev => prev + 10)} className="w-14 h-14 rounded-2xl bg-white border border-slate-200 flex items-center justify-center text-slate-400 hover:text-emerald-500 hover:border-emerald-200 transition-all shadow-sm active:scale-95"><Plus size={24}/></button>
                    </div>
                    <div className="pt-6 border-t border-slate-200 flex justify-between text-xs font-bold">
                      <span className="text-slate-400">현재 창고 실물 수량</span>
                      <span className="text-slate-900">{inventory.total.toLocaleString()} EA</span>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest ml-4">조정 사유 필수 선택</label>
                    <div className="grid grid-cols-2 gap-2">
                      {REASONS.map(r => (
                        <button 
                          key={r}
                          onClick={() => setReason(r)}
                          className={`p-4 rounded-2xl border text-xs font-black transition-all ${reason === r ? 'bg-indigo-600 border-indigo-600 text-white shadow-lg' : 'bg-white border-slate-100 text-slate-500 hover:border-slate-300'}`}
                        >
                          {r}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="bg-slate-900 rounded-[2.5rem] p-8 text-white flex flex-col shadow-2xl">
                  <h3 className="text-[11px] font-black text-slate-500 uppercase tracking-widest mb-8 flex items-center gap-2">
                    <ShieldCheck size={16} className="text-indigo-400"/> Stock Verification
                  </h3>
                  <div className="space-y-6 flex-1">
                    <div className="flex justify-between items-center pb-4 border-b border-white/10">
                      <span className="text-sm font-bold text-slate-400">물리 총 재고</span>
                      <span className="text-xl font-black">{tempTotal.toLocaleString()} EA</span>
                    </div>
                    <div className="flex justify-between items-center pb-4 border-b border-white/10">
                      <span className="text-sm font-bold text-slate-400">주문 점유 재고 (Reserved)</span>
                      <span className="text-xl font-black text-rose-400">-{inventory.reserved} EA</span>
                    </div>
                    <div className="flex justify-between items-center pt-4">
                      <span className="text-sm font-black text-indigo-400">가용 재고 (Available)</span>
                      <div className="text-right">
                        <p className="text-3xl font-black text-white">{availableForAllocation.toLocaleString()} EA</p>
                        <p className="text-[10px] text-slate-500 font-bold uppercase tracking-tight mt-1">할당 가능한 최대치</p>
                      </div>
                    </div>
                  </div>
                  <div className="mt-10 p-4 bg-white/5 rounded-2xl border border-white/10 flex gap-4">
                    <Info className="text-indigo-400 shrink-0" size={18} />
                    <p className="text-[11px] text-slate-400 leading-relaxed">
                      재고 수량을 변경하면 연동된 모든 채널에 즉시 반영되지 않으며, '저장' 버튼 클릭 시 일괄 동기화됩니다.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'channels' && (
            <div className="space-y-10 animate-in fade-in slide-in-from-right-4">
              <div className="flex items-center justify-between px-8 py-6 bg-indigo-50 rounded-[2rem] border border-indigo-100">
                <div>
                  <h4 className="text-sm font-black text-indigo-900">가상 재고 할당 밸런스</h4>
                  <p className="text-xs text-indigo-600 mt-1 font-medium">채널별 할당량이 총 가용 재고 내에 있는지 확인하세요.</p>
                </div>
                <div className="text-right">
                  <p className={`text-2xl font-black ${isOverAllocated ? 'text-rose-600' : 'text-indigo-600'}`}>
                    {remainingStock > 0 ? '+' : ''}{remainingStock.toLocaleString()} EA
                  </p>
                  <p className="text-[10px] font-black uppercase tracking-widest text-indigo-400">
                    {isOverAllocated ? 'Over Allocated!' : 'Remaining to Allocate'}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {[
                  { id: 'mall', label: '자사몰 (MALL)', color: 'bg-blue-600' },
                  { id: 'naver', label: '네이버 스마트스토어', color: 'bg-emerald-500' },
                  { id: 'coupang', label: '쿠팡 (로켓/윙)', color: 'bg-orange-500' },
                  { id: 'amazon', label: '아마존 (FBA/FBM)', color: 'bg-slate-700' },
                ].map((channel) => (
                  <div key={channel.id} className="bg-white border border-slate-200 rounded-[2rem] p-6 hover:border-indigo-300 transition-all flex flex-col gap-6 group">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className={`w-3 h-3 rounded-full ${channel.color}`}></div>
                        <span className="text-sm font-black text-slate-800">{channel.label}</span>
                      </div>
                      <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Share: {Math.round((tempBreakdown[channel.id] || 0) / (availableForAllocation || 1) * 100)}%</span>
                    </div>
                    
                    <div className="flex items-center gap-4">
                      <button onClick={() => adjustChannel(channel.id, -10)} className="w-10 h-10 rounded-xl border border-slate-100 flex items-center justify-center text-slate-400 hover:bg-slate-50 transition-all"><Minus size={18}/></button>
                      <input 
                        type="number" 
                        value={tempBreakdown[channel.id] || 0}
                        onChange={(e) => setTempBreakdown({...tempBreakdown, [channel.id]: parseInt(e.target.value) || 0})}
                        className="flex-1 text-center font-black text-xl text-slate-900 border-b border-transparent focus:border-indigo-200 outline-none transition-all"
                      />
                      <button onClick={() => adjustChannel(channel.id, 10)} className="w-10 h-10 rounded-xl border border-slate-100 flex items-center justify-center text-slate-400 hover:bg-slate-50 transition-all"><Plus size={18}/></button>
                    </div>
                  </div>
                ))}
              </div>

              {isOverAllocated && (
                <div className="p-6 bg-rose-50 border border-rose-100 rounded-[2rem] flex items-center gap-4 animate-bounce">
                  <AlertTriangle className="text-rose-500 shrink-0" size={24} />
                  <div>
                    <p className="text-sm font-black text-rose-800">재고 할당 오류</p>
                    <p className="text-xs text-rose-600 font-medium">각 채널별 재고 합계가 전체 가용 재고 ({availableForAllocation} EA)를 초과했습니다. 수량을 조절해주세요.</p>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-8 bg-slate-50 border-t border-slate-100 flex justify-between items-center relative z-20">
          <div className="flex items-center gap-4 text-slate-400">
             <Zap size={16} className="text-amber-500 animate-pulse" />
             <span className="text-[10px] font-black uppercase tracking-[0.2em]">Ready to Sync Across 4 Channels</span>
          </div>
          <div className="flex gap-3">
            <button onClick={onClose} className="px-8 py-4 bg-white border border-slate-200 text-slate-500 text-sm font-black rounded-2xl hover:bg-slate-100 transition-all">취소</button>
            <button 
              onClick={handleSave}
              disabled={isSaving || isOverAllocated}
              className={`px-10 py-4 text-white text-sm font-black rounded-2xl transition-all shadow-xl flex items-center gap-2 active:scale-95 disabled:opacity-50 ${isSaving ? 'bg-indigo-400' : 'bg-indigo-600 hover:bg-indigo-700 shadow-indigo-500/20'}`}
            >
              {isSaving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
              저장 및 채널 전송
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StockAdjustmentModal;
