
import React, { useState } from 'react';
import { X, XCircle, AlertTriangle, Save, AlertCircle } from 'lucide-react';

interface CancelModalProps {
  isOpen: boolean;
  onClose: () => void;
  orderId: string | null;
  onConfirm: (orderId: string, reason: string) => void;
}

const CANCEL_REASONS = ['고객 변심', '품절로 인한 발송 불가', '가격 기재 오류', '배송 지연', '중복 주문', '기타 사유'];

const CancelModal: React.FC<CancelModalProps> = ({ isOpen, onClose, orderId, onConfirm }) => {
  const [reason, setReason] = useState(CANCEL_REASONS[0]);

  if (!isOpen || !orderId) return null;

  return (
    <div className="fixed inset-0 z-[160] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="cancel-modal-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>
      <div className="relative bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-rose-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-rose-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-rose-500/20">
              <XCircle size={20} />
            </div>
            <div>
              <h2 id="cancel-modal-title" className="text-lg font-bold text-slate-900">주문 취소 처리</h2>
              <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{orderId}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-rose-100 rounded-full text-rose-500 transition-colors">
            <X size={20} />
          </button>
        </div>
        <div className="p-8 space-y-6">
          <div className="p-5 bg-rose-50 rounded-2xl border border-rose-100 flex gap-3 items-start">
            <AlertTriangle className="text-rose-600 flex-shrink-0 mt-0.5" size={18} />
            <div className="space-y-1">
              <p className="text-xs text-rose-800 font-bold">주의사항</p>
              <p className="text-[11px] text-rose-700 font-medium leading-relaxed">
                취소 처리 시 할당된 재고가 즉시 복구되며, 해당 쇼핑몰 채널로 취소 데이터가 전송됩니다. 이 작업은 취소가 불가능합니다.
              </p>
            </div>
          </div>
          <div className="space-y-3">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">취소 사유 선택</label>
            <div className="space-y-2">
              {CANCEL_REASONS.map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setReason(r)}
                  className={`w-full p-4 text-left text-sm font-bold rounded-2xl border transition-all ${
                    reason === r 
                      ? 'border-rose-200 bg-rose-50 text-rose-700 ring-2 ring-rose-100' 
                      : 'border-slate-100 bg-white text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>
        </div>
        <div className="p-6 bg-slate-50 border-t border-slate-100 flex gap-3">
          <button onClick={onClose} className="flex-1 py-4 text-slate-500 font-bold text-sm hover:bg-slate-100 rounded-2xl transition-colors">돌아가기</button>
          <button 
            onClick={() => {
              onConfirm(orderId, reason);
              onClose();
            }}
            className="flex-1 py-4 bg-rose-600 text-white font-bold text-sm rounded-2xl shadow-xl shadow-rose-500/20 hover:bg-rose-700 transition-all active:scale-95"
          >
            취소 확정하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default CancelModal;
