
import React from 'react';
import { X, Truck, Package, MapPin, Activity, CheckCircle2, ChevronRight, Phone, Clock } from 'lucide-react';

interface ShippingDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  shipmentId: string | null;
}

const ShippingDetailModal: React.FC<ShippingDetailModalProps> = ({ isOpen, onClose, shipmentId }) => {
  if (!isOpen || !shipmentId) return null;

  return (
    <div className="fixed inset-0 z-[110] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>
      
      <div className="relative bg-white w-full max-w-4xl rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-blue-50/30">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center shadow-sm text-blue-600">
              <Truck size={24} />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-bold text-slate-900">{shipmentId}</h2>
                <span className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded text-[10px] font-bold uppercase tracking-wider">배송 중</span>
              </div>
              <p className="text-xs text-slate-500 font-medium">CJ대한통운 | 송장번호: 6542-1234-9001</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full transition-colors text-slate-400">
            <X size={24} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-8 flex flex-col lg:flex-row gap-8">
          <div className="flex-1 space-y-8">
            {/* Visual Tracking Map Simulation */}
            <div className="bg-slate-100 h-48 rounded-[2rem] relative overflow-hidden flex items-center justify-center">
              <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/carbon-fibre.png')] opacity-5"></div>
              <div className="relative z-10 w-full px-12">
                <div className="flex justify-between items-center relative">
                  <div className="absolute top-1/2 left-0 w-full h-1 bg-white/50 -translate-y-1/2 rounded-full"></div>
                  <div className="absolute top-1/2 left-0 w-[60%] h-1 bg-blue-500 -translate-y-1/2 rounded-full shadow-[0_0_8px_rgba(59,130,246,0.5)]"></div>
                  
                  {[
                    { label: '발송지', sub: '군포 허브', active: true },
                    { label: '경유지', sub: '송파 센터', active: true },
                    { label: '배송지', sub: '강남구 역삼동', active: false },
                  ].map((p, idx) => (
                    <div key={idx} className="flex flex-col items-center gap-2">
                      <div className={`w-4 h-4 rounded-full border-4 border-white z-10 ${p.active ? 'bg-blue-600' : 'bg-slate-300'}`}></div>
                      <div className="text-center">
                        <p className="text-[10px] font-bold text-slate-800">{p.label}</p>
                        <p className="text-[9px] text-slate-500">{p.sub}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Detailed Timeline */}
            <div>
              <h3 className="text-sm font-bold text-slate-800 mb-6 flex items-center gap-2">
                <Clock size={16} className="text-blue-600" />
                실시간 배송 타임라인
              </h3>
              <div className="space-y-6 relative ml-2">
                <div className="absolute left-[11px] top-2 bottom-2 w-px bg-slate-100"></div>
                {[
                  { time: '14:20', date: '01.18', status: '간선하차', location: '송파 센터', desc: '배송지로 출발 준비 중입니다.', current: true },
                  { time: '10:05', date: '01.18', status: '간선상차', location: '군포 HUB', desc: '허브에서 분류 완료되어 출발했습니다.', current: false },
                  { time: '18:40', date: '01.17', status: '집화처리', location: '의왕 대리점', desc: '택배 기사님이 상품을 수거했습니다.', current: false },
                ].map((log, idx) => (
                  <div key={idx} className="flex gap-6 relative">
                    <div className={`w-[22px] h-[22px] rounded-full border-4 border-white z-10 flex-shrink-0 ${log.current ? 'bg-blue-600 shadow-lg shadow-blue-200' : 'bg-slate-200'}`}></div>
                    <div className="flex-1 pb-2">
                      <div className="flex items-center gap-3 mb-1">
                        <span className={`text-sm font-bold ${log.current ? 'text-blue-600' : 'text-slate-800'}`}>{log.status}</span>
                        <span className="text-[10px] text-slate-400 font-bold tracking-tighter">{log.date} {log.time}</span>
                      </div>
                      <p className="text-xs text-slate-500 font-medium mb-1">{log.location}</p>
                      <p className="text-[11px] text-slate-400">{log.desc}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="w-full lg:w-72 space-y-6">
            {/* Courier API Status */}
            <div className="bg-slate-900 rounded-3xl p-6 text-white shadow-xl">
              <h3 className="text-[10px] font-bold text-slate-500 uppercase mb-4 flex items-center gap-2">
                <Activity size={14} /> Partner Heartbeat
              </h3>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-xs text-slate-400">CJ대한통운 API</span>
                  <div className="flex items-center gap-1.5">
                    <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></div>
                    <span className="text-[10px] font-bold text-emerald-400">NORMAL</span>
                  </div>
                </div>
                <div className="p-3 bg-white/5 rounded-xl border border-white/10">
                  <p className="text-[10px] text-slate-500 mb-1 font-bold">LATEST HEARTBEAT</p>
                  <p className="text-xs font-mono">2025-01-18 16:58:12</p>
                </div>
              </div>
            </div>

            {/* Receiver Info */}
            <div className="bg-white border border-slate-200 rounded-2xl p-6">
              <h3 className="text-xs font-bold text-slate-400 uppercase mb-4 flex items-center gap-2">
                <MapPin size={14} /> 수령인 정보
              </h3>
              <div className="space-y-4">
                <div>
                  <p className="text-sm font-bold text-slate-800">이몽룡</p>
                  <p className="text-xs text-slate-500 flex items-center gap-1.5 mt-1">
                    <Phone size={10} /> 010-1234-5678
                  </p>
                </div>
                <p className="text-xs text-slate-600 leading-relaxed bg-slate-50 p-3 rounded-xl">
                  서울특별시 강남구 테헤란로 123, 10층 (역삼동)
                </p>
              </div>
            </div>

            <button className="w-full py-3 bg-white border border-slate-200 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-50 flex items-center justify-center gap-2 transition-colors">
              <Phone size={14} /> 기사님께 연락하기
            </button>
          </div>
        </div>

        <div className="p-6 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
          <button className="px-6 py-2.5 bg-white border border-slate-200 text-slate-600 text-sm font-bold rounded-xl">오배송 신고</button>
          <button className="px-6 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl shadow-lg shadow-blue-500/20">송장 재발행</button>
        </div>
      </div>
    </div>
  );
};

export default ShippingDetailModal;
