
import React from 'react';
import { X, DollarSign, PieChart, Info, ArrowDownRight, Tag, CreditCard, ShieldCheck } from 'lucide-react';
import { ResponsiveContainer, PieChart as RePieChart, Pie, Cell, Tooltip } from 'recharts';

interface SettlementDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  channelId: string | null;
}

const breakdownData = [
  { name: '실 정산액', value: 85, color: '#2563eb' },
  { name: '플랫폼 수수료', value: 8, color: '#f43f5e' },
  { name: '광고비', value: 4, color: '#f59e0b' },
  { name: '결제 수수료', value: 3, color: '#64748b' },
];

const SettlementDetailModal: React.FC<SettlementDetailModalProps> = ({ isOpen, onClose, channelId }) => {
  if (!isOpen || !channelId) return null;

  return (
    <div className="fixed inset-0 z-[110] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="settlement-detail-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>

      <div className="relative bg-white w-full max-w-4xl rounded-3xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center text-white">
              <DollarSign size={20} />
            </div>
            <div>
              <h2 id="settlement-detail-title" className="text-xl font-bold text-slate-900">{channelId} 상세 정산 리포트</h2>
              <p className="text-xs text-slate-500 font-medium">정산 기간: 2025.01.01 - 2025.01.18</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full text-slate-400">
            <X size={24} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-8 flex flex-col lg:flex-row gap-8">
          <div className="flex-1 space-y-8">
            {/* Overview Stats */}
            <div className="grid grid-cols-2 gap-4">
              <div className="p-6 bg-blue-50 border border-blue-100 rounded-3xl">
                <p className="text-[10px] font-bold text-blue-400 uppercase tracking-widest mb-1">총 주문금액</p>
                <p className="text-2xl font-bold text-blue-700">₩ 5,800,000</p>
              </div>
              <div className="p-6 bg-emerald-50 border border-emerald-100 rounded-3xl">
                <p className="text-[10px] font-bold text-emerald-400 uppercase tracking-widest mb-1">실 정산 예정액</p>
                <p className="text-2xl font-bold text-emerald-700">₩ 5,050,000</p>
              </div>
            </div>

            {/* Granular Breakdown */}
            <div>
              <h3 className="text-sm font-bold text-slate-800 mb-6 flex items-center gap-2">
                <Info size={16} className="text-indigo-600" />
                정산 항목별 명세
              </h3>
              <div className="space-y-3">
                {[
                  { label: '상품 판매 합계', val: '₩ 5,800,000', sub: '총 45건 주문', type: 'plus' },
                  { label: '쿠폰/할인 분담금', val: '₩ -120,000', sub: '자사몰 프로모션 5%', type: 'minus' },
                  { label: '플랫폼 기본 수수료', val: '₩ -464,000', sub: '판매가의 8% 적용', type: 'minus' },
                  { label: '배송비 정산', val: '₩ 134,000', sub: '편도 기준 합산', type: 'plus' },
                  { label: '광고비 차감', val: '₩ -300,000', sub: '키워드 광고(CPC) 집행', type: 'minus' },
                ].map((item, idx) => (
                  <div key={idx} className="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-100">
                    <div>
                      <p className="text-sm font-bold text-slate-800">{item.label}</p>
                      <p className="text-[10px] text-slate-400 font-medium">{item.sub}</p>
                    </div>
                    <div className="text-right">
                      <p className={`text-sm font-bold ${item.type === 'minus' ? 'text-rose-600' : 'text-slate-800'}`}>
                        {item.val}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="w-full lg:w-72 space-y-6">
            {/* Chart */}
            <div className="bg-white border border-slate-200 rounded-[2rem] p-6 shadow-sm min-w-0">
              <h3 className="text-xs font-bold text-slate-400 uppercase mb-4 flex items-center gap-2 tracking-widest">
                <PieChart size={14} /> 비용 구조 분석
              </h3>
              <div className="h-48 mb-4 min-h-[192px]">
                <ResponsiveContainer width="100%" height="100%" minHeight={192}>
                  <RePieChart>
                    <Pie data={breakdownData} innerRadius={50} outerRadius={70} paddingAngle={4} dataKey="value">
                      {breakdownData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </RePieChart>
                </ResponsiveContainer>
              </div>
              <div className="space-y-2">
                {breakdownData.map((item) => (
                  <div key={item.name} className="flex items-center justify-between text-[10px] font-bold">
                    <div className="flex items-center gap-2">
                      <div className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: item.color }}></div>
                      <span className="text-slate-500">{item.name}</span>
                    </div>
                    <span className="text-slate-800">{item.value}%</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Payout Status */}
            <div className="p-6 bg-slate-900 rounded-3xl text-white">
              <div className="flex items-center gap-3 mb-4 text-emerald-400">
                <ShieldCheck size={20} />
                <span className="text-xs font-bold uppercase tracking-widest">Payout Verified</span>
              </div>
              <p className="text-xs text-slate-400 mb-1">입금 예정일</p>
              <p className="text-lg font-bold mb-4">2025년 1월 25일</p>
              <div className="h-px bg-slate-800 w-full mb-4"></div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] text-slate-500">입금 계좌</span>
                <span className="text-[11px] font-mono">우리은행 1002-***-990</span>
              </div>
            </div>
          </div>
        </div>

        <div className="p-6 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
          <button className="px-6 py-2.5 bg-white border border-slate-200 text-slate-600 text-sm font-bold rounded-xl flex items-center gap-2">
            <Tag size={16} /> 인보이스 PDF
          </button>
          <button className="px-6 py-2.5 bg-indigo-600 text-white text-sm font-bold rounded-xl shadow-lg shadow-indigo-500/20">
            정산 확정 승인
          </button>
        </div>
      </div>
    </div>
  );
};

export default SettlementDetailModal;
