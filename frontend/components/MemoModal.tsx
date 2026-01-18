
import React, { useState } from 'react';
import { X, MessageSquare, Save, AlertCircle } from 'lucide-react';

interface MemoModalProps {
  isOpen: boolean;
  onClose: () => void;
  orderId: string | null;
}

const MemoModal: React.FC<MemoModalProps> = ({ isOpen, onClose, orderId }) => {
  const [memo, setMemo] = useState('');
  const [isImportant, setIsImportant] = useState(false);

  if (!isOpen || !orderId) return null;

  const handleSave = () => {
    if (!memo.trim()) {
      alert("메모 내용을 입력해주세요.");
      return;
    }
    alert(`${orderId} 주문에 메모가 등록되었습니다.\n내용: ${memo}`);
    setMemo('');
    setIsImportant(false);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-[160] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="memo-modal-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>
      <div className="relative bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center text-white">
              <MessageSquare size={20} />
            </div>
            <div>
              <h2 id="memo-modal-title" className="text-lg font-bold text-slate-900">내부 메모 작성</h2>
              <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{orderId}</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full text-slate-400">
            <X size={20} />
          </button>
        </div>
        <div className="p-8 space-y-6">
          <div className="space-y-2">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">운영 메모</label>
            <textarea 
              autoFocus
              rows={4}
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              placeholder="운영팀원들이 확인할 수 있는 메모를 남기세요."
              className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium outline-none focus:ring-2 focus:ring-indigo-500/20"
            ></textarea>
          </div>
          <label className="flex items-center gap-3 cursor-pointer group">
            <input 
              type="checkbox" 
              checked={isImportant}
              onChange={(e) => setIsImportant(e.target.checked)}
              className="w-5 h-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" 
            />
            <span className="text-sm font-bold text-slate-600 group-hover:text-slate-900">중요 메모로 표시</span>
          </label>
        </div>
        <div className="p-6 bg-slate-50 border-t border-slate-100 flex gap-3">
          <button onClick={onClose} className="flex-1 py-3 text-slate-500 font-bold text-sm">취소</button>
          <button onClick={handleSave} className="flex-1 py-3 bg-slate-900 text-white font-bold text-sm rounded-2xl shadow-lg hover:bg-slate-800 transition-all">
            메모 저장
          </button>
        </div>
      </div>
    </div>
  );
};

export default MemoModal;
