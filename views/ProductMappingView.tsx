
import React, { useState } from 'react';
import { 
  Search, Check, Download, ListFilter, Database, 
  Loader2, CheckCircle2, AlertTriangle, CheckSquare, Square, X
} from 'lucide-react';
import { useGlobalData } from '../App';

interface MappingItem {
  id: string;
  sku: string;
  name: string;
  category: string;
  price: number;
  channels: string[];
}

const TOTAL_MOCK_COUNT = 24500;

const INITIAL_MAPPINGS: MappingItem[] = Array.from({ length: 15 }).map((_, i) => ({
  id: `${i + 1}`,
  sku: `ITEM-SKU-${1000 + i}`,
  name: i % 2 === 0 ? `프리미엄 퀼팅 자켓 ${i + 1}` : `베이직 코튼 슬림 팬츠 ${i + 1}`,
  category: i % 2 === 0 ? '의류 > 아우터' : '의류 > 하의',
  price: 29000 + (i * 1000),
  channels: i % 3 === 0 ? ['CH-001', 'CH-002'] : ['CH-003'],
}));

const ProductMappingView: React.FC = () => {
  const { channels } = useGlobalData();
  const [mappings] = useState(INITIAL_MAPPINGS);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [isAllInSearchSelected, setIsAllInSearchSelected] = useState(false);
  const [pendingAction, setPendingAction] = useState<{ channelId: string, action: string } | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [progress, setProgress] = useState(0);

  const togglePageSelect = () => {
    if (selectedIds.length === mappings.length) {
      setSelectedIds([]);
      setIsAllInSearchSelected(false);
    } else {
      setSelectedIds(mappings.map(m => m.id));
    }
  };

  const selectAllInDatabase = () => setIsAllInSearchSelected(true);

  const toggleSelect = (id: string) => {
    setIsAllInSearchSelected(false);
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
  };

  const executeBulkAction = () => {
    if (!pendingAction) return;
    setPendingAction(null);
    setIsProcessing(true);
    setProgress(0);
    const interval = setInterval(() => {
      setProgress(prev => {
        if (prev >= 100) {
          clearInterval(interval);
          setTimeout(() => setIsProcessing(false), 500);
          setSelectedIds([]);
          setIsAllInSearchSelected(false);
          return 100;
        }
        return prev + 10;
      });
    }, 100);
  };

  const targetCount = isAllInSearchSelected ? TOTAL_MOCK_COUNT : selectedIds.length;

  return (
    <div className="space-y-6 pb-48 relative">
      <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 px-2">
        <div className="min-w-0">
          <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3 break-keep">
            매핑 콘솔 <Database size={24} className="text-indigo-600 shrink-0" />
          </h1>
          <p className="text-slate-500 text-xs md:text-sm font-medium mt-1 break-keep">실시간 데이터 마스터 정보를 동기화합니다.</p>
        </div>
        <div className="flex gap-2">
           <button className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-[10px] font-bold text-slate-600 shadow-sm transition-all whitespace-nowrap">
             <Download size={14} /> 다운로드
           </button>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-center gap-4 bg-white p-3 md:p-4 rounded-[1.5rem] md:rounded-[2rem] border border-slate-200 shadow-sm">
        <div className="w-full sm:flex-1 relative group">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={18} />
          <input type="text" placeholder="상품 검색..." className="w-full pl-11 pr-4 py-3 bg-slate-50 border-none rounded-xl text-sm font-bold outline-none break-keep" />
        </div>
        <button className="w-full sm:w-auto px-6 py-3 bg-slate-100 text-slate-500 rounded-xl text-xs font-black flex items-center justify-center gap-2 hover:bg-slate-200 transition-all uppercase tracking-widest whitespace-nowrap">
          <ListFilter size={16}/> 필터
        </button>
      </div>

      {selectedIds.length > 0 && (
        <div className="bg-indigo-600 text-white px-5 md:px-8 py-4 md:py-5 rounded-[1.5rem] flex flex-col md:flex-row items-center justify-between gap-3 animate-in slide-in-from-top-2 shadow-xl shadow-indigo-500/20">
           <div className="flex items-center gap-3">
              <CheckCircle2 size={18} className="text-indigo-200 shrink-0" />
              <p className="text-sm font-bold break-keep">
                {isAllInSearchSelected ? `${TOTAL_MOCK_COUNT.toLocaleString()}개 상품이 모두 선택되었습니다.` : `${selectedIds.length}개의 상품이 선택되었습니다.`}
              </p>
           </div>
           {!isAllInSearchSelected && (
             <button onClick={selectAllInDatabase} className="text-xs font-black underline underline-offset-4 hover:text-indigo-100 whitespace-nowrap">
               결과 전체 {TOTAL_MOCK_COUNT.toLocaleString()}개 선택
             </button>
           )}
        </div>
      )}

      <div className="bg-white rounded-[1.5rem] md:rounded-[3rem] border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto scrollbar-hide">
          <table className="w-full text-left min-w-[850px] table-fixed">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100">
                <th className="w-16 py-6 px-4 text-center">
                  <button onClick={togglePageSelect} className="text-slate-400">
                    {selectedIds.length === mappings.length ? <CheckSquare size={20} className="text-indigo-600" /> : <Square size={20} />}
                  </button>
                </th>
                <th className="w-80 py-6 px-4 text-[10px] font-black text-slate-400 uppercase tracking-widest break-keep">상품 마스터 정보</th>
                {channels.map(ch => (
                  <th key={ch.id} className="w-32 py-6 px-2 text-center border-l border-slate-100/50">
                    <div className="flex flex-col items-center gap-1 overflow-hidden px-1">
                       <span className="text-lg md:text-xl shrink-0">{ch.logo}</span>
                       <span className="text-[8px] md:text-[10px] font-black text-slate-500 uppercase tracking-tighter truncate w-full break-keep whitespace-nowrap">{ch.name}</span>
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {mappings.map(prod => {
                const isSelected = selectedIds.includes(prod.id) || isAllInSearchSelected;
                return (
                  <tr key={prod.id} className={`hover:bg-slate-50 transition-colors ${isSelected ? 'bg-indigo-50/30' : ''}`}>
                    <td className="py-4 px-4 text-center">
                      <button onClick={() => toggleSelect(prod.id)} className={`transition-colors ${isSelected ? 'text-indigo-600' : 'text-slate-300'}`}>
                        {isSelected ? <CheckSquare size={20} /> : <Square size={20} />}
                      </button>
                    </td>
                    <td className="py-4 px-4 min-w-0">
                       <div className="flex flex-col gap-0.5 overflow-hidden">
                          <span className="text-[9px] font-black text-slate-400 uppercase leading-none truncate whitespace-nowrap">{prod.sku}</span>
                          <span className="text-xs md:text-sm font-black text-slate-800 tracking-tight truncate leading-tight italic break-keep">{prod.name}</span>
                          <span className="text-[9px] font-bold text-slate-400 truncate whitespace-nowrap">{prod.category}</span>
                       </div>
                    </td>
                    {channels.map(ch => {
                      const isActive = prod.channels.includes(ch.id);
                      return (
                        <td key={ch.id} className="py-4 px-2 text-center border-l border-slate-50/50">
                           <div className={`w-8 h-8 md:w-10 md:h-10 rounded-xl md:rounded-2xl flex items-center justify-center mx-auto transition-all ${
                              isActive ? `bg-indigo-600 text-white shadow-lg` : 'bg-slate-100 text-slate-200'
                            }`}>
                              {isActive ? <Check size={16} strokeWidth={3} /> : <div className="w-1 h-1 rounded-full bg-slate-300"></div>}
                           </div>
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Floating Bulk Bar Responsive */}
      {(selectedIds.length > 0 || isAllInSearchSelected) && (
        <div className="fixed bottom-0 left-0 lg:left-64 right-0 z-[200] p-4 lg:p-8 animate-in slide-in-from-bottom-10">
           {pendingAction && (
             <div className="mb-4 w-full max-w-sm mx-auto bg-white rounded-3xl shadow-[0_20px_50px_rgba(0,0,0,0.2)] p-6 border border-indigo-100 animate-in zoom-in-95 duration-200">
                <div className="flex flex-col items-center text-center gap-4">
                   <div className="w-12 h-12 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600 shadow-inner"><AlertTriangle size={24} /></div>
                   <h4 className="text-sm md:text-base font-black text-slate-900 leading-tight break-keep">
                     {targetCount.toLocaleString()}개 상품을<br/>일괄 연동하시겠습니까?
                   </h4>
                   <div className="grid grid-cols-2 gap-2 w-full mt-2">
                      <button onClick={() => setPendingAction(null)} className="py-3.5 bg-slate-50 text-slate-500 rounded-xl text-[10px] md:text-xs font-black uppercase whitespace-nowrap">취소</button>
                      <button onClick={executeBulkAction} className="py-3.5 bg-indigo-600 text-white rounded-xl text-[10px] md:text-xs font-black uppercase shadow-lg shadow-indigo-500/20 whitespace-nowrap">연동 시작</button>
                   </div>
                </div>
             </div>
           )}

           <div className="bg-slate-900 text-white p-4 md:p-6 rounded-[2rem] shadow-2xl border border-white/10 flex flex-col md:flex-row items-center gap-4 md:gap-8 max-w-4xl mx-auto">
              <div className="flex items-center gap-4 border-b md:border-b-0 md:border-r border-white/10 pb-3 md:pb-0 md:pr-8 w-full md:w-auto">
                 <div className="w-10 h-10 md:w-14 md:h-14 bg-indigo-600 rounded-xl md:rounded-[1.5rem] flex items-center justify-center font-black text-sm md:text-xl shadow-2xl shrink-0">
                   {isAllInSearchSelected ? 'ALL' : selectedIds.length}
                 </div>
                 <div className="whitespace-nowrap overflow-hidden">
                   <p className="text-xs md:text-sm font-black italic truncate leading-none whitespace-nowrap">Items Selected</p>
                   <p className="text-[8px] md:text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-1 whitespace-nowrap">전체 처리 프로세스</p>
                 </div>
              </div>

              <div className="flex-1 flex items-center justify-center gap-3 overflow-x-auto no-scrollbar py-1">
                 {channels.map(ch => (
                   <button 
                    key={ch.id}
                    onClick={() => setPendingAction({ channelId: ch.id, action: 'CONNECT' })}
                    className={`w-10 h-10 md:w-12 md:h-12 bg-white/10 rounded-xl md:rounded-2xl flex items-center justify-center text-lg md:text-xl transition-all active:scale-90 shrink-0 ${pendingAction?.channelId === ch.id ? 'ring-2 ring-indigo-500 bg-white/20' : 'hover:bg-white/20'}`}
                   >
                     {ch.logo}
                   </button>
                 ))}
              </div>

              <div className="flex gap-2 w-full md:w-auto border-t md:border-t-0 border-white/5 pt-3 md:pt-0">
                 <button 
                   onClick={() => { setSelectedIds([]); setIsAllInSearchSelected(false); setPendingAction(null); }}
                   className="flex-1 md:flex-none px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-[9px] font-black uppercase whitespace-nowrap"
                 >
                   선택 해제
                 </button>
                 <button 
                   onClick={() => setPendingAction({ channelId: 'ALL', action: 'DISCONNECT' })}
                   className="flex-1 md:flex-none px-4 py-3 bg-rose-600 rounded-xl text-[9px] font-black uppercase shadow-xl whitespace-nowrap"
                 >
                   매핑 초기화
                 </button>
              </div>
           </div>
        </div>
      )}

      {/* Progress Modal Responsive */}
      {isProcessing && (
        <div className="fixed inset-0 z-[500] bg-slate-900/70 backdrop-blur-xl flex items-center justify-center p-6">
           <div className="bg-white w-full max-w-[320px] md:max-w-md p-8 md:p-12 rounded-[2.5rem] md:rounded-[4rem] shadow-2xl flex flex-col items-center gap-6 md:gap-8 animate-in zoom-in-95">
              <div className="relative w-24 h-24 md:w-32 md:h-32 shrink-0">
                 <svg className="w-full h-full transform -rotate-90">
                    <circle className="text-slate-100" strokeWidth="6" stroke="currentColor" fill="transparent" r="45" cx="48" cy="48" />
                    <circle 
                      className="text-indigo-600 transition-all duration-300" 
                      strokeWidth="6" strokeDasharray={282} strokeDashoffset={282 - (282 * progress) / 100} 
                      strokeLinecap="round" stroke="currentColor" fill="transparent" r="45" cx="48" cy="48" 
                    />
                 </svg>
                 <div className="absolute inset-0 flex items-center justify-center">
                    <span className="text-xl md:text-2xl font-black text-slate-900 italic">{progress}%</span>
                 </div>
              </div>
              <div className="text-center w-full space-y-2">
                 <h3 className="text-lg md:text-2xl font-black text-slate-900 break-keep leading-tight">API 데이터 전송 중</h3>
                 <p className="text-xs md:text-sm text-slate-400 font-medium leading-relaxed break-keep">쇼핑몰 서버로 정보를 안전하게 전송하고 있습니다.</p>
              </div>
              <button onClick={() => setIsProcessing(false)} className="px-6 py-3.5 bg-slate-50 text-slate-400 text-[9px] md:text-[10px] font-black uppercase rounded-xl hover:text-rose-500 whitespace-nowrap">백그라운드 실행</button>
           </div>
        </div>
      )}
    </div>
  );
};

export default ProductMappingView;
