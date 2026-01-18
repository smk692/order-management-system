
import React, { useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { DollarSign, Percent, ArrowDownToLine, TrendingUp, Calendar, ArrowRight, X, ChevronRight, Download, Printer } from 'lucide-react';
import SettlementDetailModal from '../components/SettlementDetailModal';

const data = [
  { name: '자사몰', revenue: 4500, fee: 150, net: 4350 },
  { name: '네이버', revenue: 3200, fee: 200, net: 3000 },
  { name: '쿠팡', revenue: 5800, fee: 750, net: 5050 },
  { name: '아마존', revenue: 2100, fee: 320, net: 1780 },
];

const SettlementView: React.FC = () => {
  const [selectedChannelId, setSelectedChannelId] = useState<string | null>(null);
  const [showPeriodModal, setShowPeriodModal] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState('2025년 1월');

  const handlePeriodSelect = (period: string) => {
    setSelectedPeriod(period);
    setShowPeriodModal(false);
    alert(`${period} 정산 데이터로 갱신되었습니다.`);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-black text-slate-900 tracking-tight">정산 관리</h1>
          <p className="text-slate-500 text-sm font-medium mt-1">월별 매출 정산 및 채널별 수수료 내역을 분석합니다.</p>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={() => setShowPeriodModal(true)}
            className="flex items-center gap-2 px-5 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-black text-slate-600 hover:bg-slate-50 transition-all shadow-sm"
          >
            <Calendar size={18} className="text-blue-600" />
            {selectedPeriod}
          </button>
          <button onClick={() => alert("현재 정산 내역 리포트를 PDF로 출력합니다.")} className="p-3 bg-slate-900 text-white rounded-2xl hover:bg-slate-800 transition-all">
            <Printer size={18} />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          { label: '총 판매금액', value: '₩ 15,600,000', icon: <DollarSign size={20} />, color: 'bg-blue-50 text-blue-600' },
          { label: '채널 수수료', value: '- ₩ 1,420,000', icon: <Percent size={20} />, color: 'bg-rose-50 text-rose-600' },
          { label: '실 정산 예정액', value: '₩ 14,180,000', icon: <ArrowDownToLine size={20} />, color: 'bg-emerald-50 text-emerald-600' },
          { label: '지난달 대비', value: '+ 14.8%', icon: <TrendingUp size={20} />, color: 'bg-indigo-600 text-white shadow-xl shadow-indigo-200' },
        ].map((stat, i) => (
          <div key={i} className={`p-6 rounded-[2rem] border border-slate-200 shadow-sm ${stat.label === '지난달 대비' ? stat.color : 'bg-white'}`}>
            <div className="flex items-center gap-3 mb-4">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${stat.label === '지난달 대비' ? 'bg-white/20' : stat.color}`}>
                {stat.icon}
              </div>
              <p className={`text-[10px] font-black uppercase tracking-widest ${stat.label === '지난달 대비' ? 'text-indigo-100' : 'text-slate-400'}`}>{stat.label}</p>
            </div>
            <p className="text-2xl font-black">{stat.value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Added min-w-0 for ResponsiveContainer stability */}
        <div className="lg:col-span-2 bg-white p-10 rounded-[3rem] border border-slate-200 shadow-sm min-w-0">
          <div className="flex items-center justify-between mb-10">
            <h3 className="text-lg font-black text-slate-800">채널별 정산 구조 분석</h3>
            <div className="flex gap-2">
              <span className="flex items-center gap-1.5 text-[10px] font-black text-slate-400 uppercase"><div className="w-2 h-2 bg-blue-600 rounded-full"></div> 매출</span>
              <span className="flex items-center gap-1.5 text-[10px] font-black text-slate-400 uppercase"><div className="w-2 h-2 bg-rose-500 rounded-full"></div> 수수료</span>
            </div>
          </div>
          <div className="h-80 w-full min-h-[320px]">
            <ResponsiveContainer width="100%" height="100%" minHeight={320}>
              <BarChart data={data} onClick={(state) => {
                if (state?.activeLabel) setSelectedChannelId(state.activeLabel);
              }}>
                <CartesianGrid strokeDasharray="4 4" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#94a3b8', fontWeight: 700}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fontSize: 12, fill: '#94a3b8', fontWeight: 700}} />
                <Tooltip 
                  cursor={{fill: '#f8fafc'}} 
                  contentStyle={{borderRadius: '24px', border: 'none', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.15)', padding: '15px'}} 
                />
                <Bar dataKey="revenue" fill="#2563EB" radius={[6, 6, 0, 0]} name="총 매출" barSize={32} />
                <Bar dataKey="fee" fill="#EF4444" radius={[6, 6, 0, 0]} name="수수료" barSize={32} />
                <Bar dataKey="net" fill="#10B981" radius={[6, 6, 0, 0]} name="정산액" barSize={32} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white p-8 rounded-[3rem] border border-slate-200 shadow-sm flex flex-col h-full">
          <h3 className="text-lg font-black text-slate-800 mb-8 flex items-center justify-between">
            채널별 상세 명세
            <button className="text-[10px] font-black text-blue-600 hover:underline">Export CSV</button>
          </h3>
          <div className="flex-1 space-y-4 overflow-y-auto pr-2 scrollbar-hide">
            {data.map((item) => (
              <div 
                key={item.name} 
                onClick={() => setSelectedChannelId(item.name)}
                className="p-5 bg-slate-50 border border-slate-100 rounded-[2rem] flex items-center justify-between group cursor-pointer hover:bg-white hover:border-indigo-400 hover:shadow-xl hover:shadow-indigo-500/5 transition-all"
              >
                <div>
                  <p className="text-sm font-black text-slate-800">{item.name}</p>
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-tight mt-1">실정산: ₩ {(item.net * 1000).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-3">
                  <div className="text-right hidden group-hover:block animate-in fade-in slide-in-from-right-1">
                    <p className="text-[10px] font-black text-indigo-600">상세분석</p>
                  </div>
                  <ChevronRight size={18} className="text-slate-300 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
                </div>
              </div>
            ))}
          </div>
          <div className="mt-8 pt-8 border-t border-slate-100">
            <button className="w-full py-4 bg-slate-900 text-white rounded-2xl text-xs font-black shadow-xl active:scale-95 transition-all">전체 정산 확정하기</button>
          </div>
        </div>
      </div>

      {/* Period Selector Modal */}
      {showPeriodModal && (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={() => setShowPeriodModal(false)}></div>
          <div className="relative bg-white w-full max-w-sm rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <h3 className="text-lg font-bold text-slate-900">정산 기간 선택</h3>
              <button onClick={() => setShowPeriodModal(false)} className="p-2 hover:bg-slate-100 rounded-full text-slate-400"><X size={20} /></button>
            </div>
            <div className="p-6 grid grid-cols-2 gap-2">
              {['2025년 1월', '2024년 12월', '2024년 11월', '2024년 10월', '2024년 3분기', '2024년 상반기'].map(p => (
                <button 
                  key={p} 
                  onClick={() => handlePeriodSelect(p)}
                  className={`p-4 rounded-2xl border text-xs font-bold transition-all ${selectedPeriod === p ? 'bg-indigo-600 text-white border-indigo-600 shadow-lg shadow-indigo-200' : 'bg-white text-slate-600 border-slate-100 hover:bg-slate-50'}`}
                >
                  {p}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      <SettlementDetailModal 
        isOpen={!!selectedChannelId} 
        onClose={() => setSelectedChannelId(null)} 
        channelId={selectedChannelId} 
      />
    </div>
  );
};

export default SettlementView;
