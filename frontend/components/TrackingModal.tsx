
import React, { useState } from 'react';
import { X, Truck, Hash, Check } from 'lucide-react';

interface TrackingModalProps {
  isOpen: boolean;
  onClose: () => void;
  orderId: string | null;
  onConfirm: (orderId: string, carrier: string, trackingNo: string) => void;
}

const CARRIERS = [
  { name: 'CJ대한통운', color: 'bg-orange-500' },
  { name: '우체국택배', color: 'bg-red-600' },
  { name: '한진택배', color: 'bg-blue-800' },
  { name: '로젠택배', color: 'bg-yellow-500' },
  { name: '롯데택배', color: 'bg-red-500' }
];

const TrackingModal: React.FC<TrackingModalProps> = ({ isOpen, onClose, orderId, onConfirm }) => {
  const [carrier, setCarrier] = useState(CARRIERS[0].name);
  const [trackingNo, setTrackingNo] = useState('');

  if (!isOpen || !orderId) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!trackingNo.trim()) {
      alert("송장번호를 입력해주세요.");
      return;
    }
    onConfirm(orderId, carrier, trackingNo.trim());
    setTrackingNo('');
    onClose();
  };

  return (
    <div className="fixed inset-0 z-[160] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="tracking-modal-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>
      <form onSubmit={handleSubmit} className="relative bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-blue-500/20">
              <Truck size={20} />
            </div>
            <div>
              <h2 id="tracking-modal-title" className="text-lg font-bold text-slate-900">송장번호 입력</h2>
              <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">{orderId}</p>
            </div>
          </div>
          <button type="button" onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full text-slate-400">
            <X size={20} />
          </button>
        </div>
        <div className="p-8 space-y-6">
          <div className="space-y-3">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">택배사 선택</label>
            <div className="grid grid-cols-2 gap-2">
              {CARRIERS.map((c) => (
                <button
                  key={c.name}
                  type="button"
                  onClick={() => setCarrier(c.name)}
                  className={`flex items-center gap-2 p-3 rounded-xl border text-xs font-bold transition-all ${
                    carrier === c.name 
                      ? 'border-blue-600 bg-blue-50 text-blue-700 ring-2 ring-blue-100' 
                      : 'border-slate-100 bg-white text-slate-600 hover:border-slate-200'
                  }`}
                >
                  <div className={`w-2 h-2 rounded-full ${c.color}`}></div>
                  {c.name}
                  {carrier === c.name && <Check size={12} className="ml-auto" />}
                </button>
              ))}
            </div>
          </div>
          <div className="space-y-2">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">송장번호</label>
            <div className="relative group">
              <Hash className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={16} />
              <input 
                autoFocus
                type="text" 
                value={trackingNo}
                onChange={(e) => setTrackingNo(e.target.value)}
                placeholder="송장번호를 입력하세요"
                className="w-full pl-10 pr-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold outline-none focus:ring-4 focus:ring-blue-500/10 focus:border-blue-500 transition-all" 
              />
            </div>
          </div>
        </div>
        <div className="p-6 bg-slate-50 border-t border-slate-100 flex gap-3">
          <button type="button" onClick={onClose} className="flex-1 py-3 text-slate-500 font-bold text-sm hover:bg-slate-100 rounded-2xl">취소</button>
          <button type="submit" className="flex-1 py-3 bg-blue-600 text-white font-bold text-sm rounded-2xl shadow-lg hover:bg-blue-700 transition-all active:scale-95">
            배송 시작 처리
          </button>
        </div>
      </form>
    </div>
  );
};

export default TrackingModal;
