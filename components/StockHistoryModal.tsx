
import React from 'react';
import { X, History, ArrowUpRight, ArrowDownRight, Package, Calendar } from 'lucide-react';

interface StockHistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  productName: string | null;
}

const MOCK_HISTORY = [
  { id: 1, type: 'IN', amount: 120, reason: '신규 입고 (발주번호: PO-102)', date: '2025-01-18 10:20', user: '홍길동' },
  { id: 2, type: 'OUT', amount: 5, reason: '주문 출고 (ORD-20250118-005)', date: '2025-01-18 14:45', user: '시스템' },
  { id: 3, type: 'ADJUST', amount: -2, reason: '재고 실사 차이 조정', date: '2025-01-17 17:00', user: '김철수' },
  { id: 4, type: 'IN', amount: 45, reason: '반품 입고 (CLM-001)', date: '2025-01-16 09:30', user: '이영희' },
];

const StockHistoryModal: React.FC<StockHistoryModalProps> = ({ isOpen, onClose, productName }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>
      <div className="relative bg-white w-full max-w-2xl rounded-[2.5rem] shadow-2xl overflow-hidden flex flex-col animate-in zoom-in-95 duration-200 max-h-[80vh]">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-slate-900 rounded-xl flex items-center justify-center text-white">
              <History size={20} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900">{productName} - 재고 변동 이력</h2>
              <p className="text-xs text-slate-400 font-medium">최근 30일간의 모든 입출고 내역입니다.</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full text-slate-400">
            <X size={24} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {MOCK_HISTORY.map((log) => (
            <div key={log.id} className="flex items-center gap-4 p-4 bg-white border border-slate-100 rounded-2xl hover:border-slate-200 transition-all">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${
                log.type === 'IN' ? 'bg-emerald-50 text-emerald-600' : 
                log.type === 'OUT' ? 'bg-rose-50 text-rose-600' : 'bg-amber-50 text-amber-600'
              }`}>
                {log.type === 'IN' ? <ArrowUpRight size={20} /> : <ArrowDownRight size={20} />}
              </div>
              <div className="flex-1">
                <p className="text-sm font-bold text-slate-800">{log.reason}</p>
                <div className="flex items-center gap-3 mt-1">
                  <span className="text-[10px] font-bold text-slate-400 flex items-center gap-1">
                    <Calendar size={10} /> {log.date}
                  </span>
                  <span className="text-[10px] font-bold text-slate-400">| 담당자: {log.user}</span>
                </div>
              </div>
              <div className="text-right">
                <p className={`text-base font-bold ${
                  log.type === 'IN' ? 'text-emerald-600' : 
                  log.type === 'OUT' ? 'text-rose-600' : 'text-amber-600'
                }`}>
                  {log.type === 'IN' ? '+' : ''}{log.amount}
                </p>
                <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">EA</p>
              </div>
            </div>
          ))}
        </div>

        <div className="p-6 bg-slate-50 border-t border-slate-100 text-center">
          <button onClick={onClose} className="text-sm font-bold text-slate-500 hover:text-slate-800">닫기</button>
        </div>
      </div>
    </div>
  );
};

export default StockHistoryModal;
