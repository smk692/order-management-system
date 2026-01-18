
import React, { useState, useEffect } from 'react';
import { Search, X, Package, ShoppingCart, User, Command, Zap, ArrowRight, History } from 'lucide-react';

interface GlobalSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const GlobalSearchModal: React.FC<GlobalSearchModalProps> = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!isOpen) return null;

  const results = query.length > 0 ? [
    { id: '1', type: 'ORDER', title: 'ORD-20250118-005', sub: '홍길동 | 결제완료', icon: <ShoppingCart size={16} /> },
    { id: '2', type: 'PRODUCT', title: '울트라 경량 퀼팅 자켓', sub: 'SKU-001 | 재고 156', icon: <Package size={16} /> },
    { id: '3', type: 'CUSTOMER', title: '김철수', sub: 'VIP 고객 | 010-****-5678', icon: <User size={16} /> },
  ] : [];

  return (
    <div className="fixed inset-0 z-[250] flex items-start justify-center pt-[10vh] px-4">
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-md" onClick={onClose}></div>
      
      <div className="relative bg-white w-full max-w-2xl rounded-[32px] shadow-2xl border border-white/20 overflow-hidden flex flex-col animate-in fade-in zoom-in-95 duration-200">
        {/* Search Input */}
        <div className="p-6 flex items-center gap-4 border-b border-slate-100">
          <Search size={24} className="text-slate-400" />
          <input 
            autoFocus
            type="text" 
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="주문번호, 상품명, 고객명을 입력하세요..." 
            className="flex-1 bg-transparent border-none outline-none text-lg font-medium text-slate-800 placeholder:text-slate-300"
          />
          <div className="flex items-center gap-1.5 px-2 py-1 bg-slate-100 rounded-lg text-[10px] font-bold text-slate-400">
            <Command size={10} />
            K
          </div>
          <button onClick={onClose} className="p-1 hover:bg-slate-100 rounded-full text-slate-400">
            <X size={20} />
          </button>
        </div>

        {/* Search Content */}
        <div className="flex-1 overflow-y-auto max-h-[60vh] p-4">
          {query.length === 0 ? (
            <div className="space-y-6">
              <div>
                <h4 className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">최근 검색어</h4>
                <div className="space-y-1">
                  {['ORD-20250117-002', '퀼팅 자켓', '아마존 US 연동'].map((item) => (
                    <button key={item} className="w-full flex items-center gap-3 px-4 py-3 rounded-2xl hover:bg-slate-50 text-sm text-slate-600 transition-colors group">
                      <History size={16} className="text-slate-300" />
                      {item}
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <h4 className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">추천 작업</h4>
                <div className="grid grid-cols-2 gap-2 px-2">
                  <button className="flex items-center gap-3 p-4 bg-indigo-50 rounded-2xl text-indigo-700 text-sm font-bold hover:bg-indigo-100 transition-colors">
                    <Zap size={16} /> 신규 주문 수동 등록
                  </button>
                  <button className="flex items-center gap-3 p-4 bg-emerald-50 rounded-2xl text-emerald-700 text-sm font-bold hover:bg-emerald-100 transition-colors">
                    <Package size={16} /> 재고 일괄 업데이트
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="space-y-1">
              <h4 className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">검색 결과 ({results.length})</h4>
              {results.map((res) => (
                <button key={res.id} className="w-full flex items-center gap-4 px-4 py-4 rounded-2xl hover:bg-blue-50 transition-all group">
                  <div className="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center text-slate-400 group-hover:bg-blue-100 group-hover:text-blue-600 transition-colors">
                    {res.icon}
                  </div>
                  <div className="flex-1 text-left">
                    <p className="text-sm font-bold text-slate-800">{res.title}</p>
                    <p className="text-xs text-slate-500">{res.sub}</p>
                  </div>
                  <ArrowRight size={16} className="text-slate-300 opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Footer Info */}
        <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between text-[10px] font-bold text-slate-400 uppercase">
          <div className="flex gap-4">
            <span className="flex items-center gap-1.5"><span className="p-0.5 bg-white border border-slate-200 rounded">Enter</span> 선택</span>
            <span className="flex items-center gap-1.5"><span className="p-0.5 bg-white border border-slate-200 rounded">↑↓</span> 이동</span>
          </div>
          <span className="text-indigo-600">Global OMS Search v1.0</span>
        </div>
      </div>
    </div>
  );
};

export default GlobalSearchModal;
