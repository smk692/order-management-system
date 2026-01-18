
import React, { useState } from 'react';
import { 
  RotateCcw, Search, Filter, 
  Download, RefreshCw, Zap,
  TrendingDown, Clock, ChevronRight,
  ShoppingBag, PackageCheck, AlertCircle
} from 'lucide-react';
import ClaimDetailModal from '../components/ClaimDetailModal';
import MemoModal from '../components/MemoModal';

const MOCK_CLAIMS = [
  { id: 'CLM-001', orderId: 'ORD-20250118-005', type: 'RETURN', channel: 'MALL', reason: '단순변심', customer: '김민지', date: '2025-01-18 16:20', status: 'REQUESTED', priority: 'HIGH' },
  { id: 'CLM-002', orderId: 'ORD-20250117-042', type: 'EXCHANGE', channel: 'NAVER', reason: '사이즈 오배송', customer: '이태민', date: '2025-01-18 14:10', status: 'PROCESSING', priority: 'NORMAL' },
  { id: 'CLM-003', orderId: 'ORD-20250116-012', type: 'CANCEL', channel: 'COUPANG', reason: '배송 지연', customer: '최유리', date: '2025-01-18 09:30', status: 'COMPLETED', priority: 'LOW' },
];

const ClaimsView: React.FC = () => {
  const [selectedClaimId, setSelectedClaimId] = useState<string | null>(null);
  const [memoClaimId, setMemoClaimId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-8 pb-32 relative">
      {/* Search Header Section */}
      <div className="bg-white p-8 rounded-[2.5rem] border border-slate-200 shadow-sm space-y-8 transition-all">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="space-y-2">
            <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3 italic uppercase">
              Claims Console
            </h1>
            <p className="text-slate-500 font-medium text-sm">취소, 반품, 교환 등 모든 사후 처리 프로세스를 관리합니다.</p>
          </div>
          <div className="flex items-center gap-2">
            <button className="flex items-center gap-2 px-5 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-bold text-slate-600 hover:bg-slate-50 transition-all">
              <Download size={18} /> 보고서 추출
            </button>
          </div>
        </div>

        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative group">
            <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors" size={20} />
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="클레임/주문번호, 고객명으로 검색하세요..." 
              className="w-full pl-14 pr-6 py-4 bg-slate-50 border border-transparent rounded-[1.5rem] text-sm font-bold outline-none focus:bg-white focus:ring-4 focus:ring-indigo-500/5 focus:border-indigo-200 transition-all shadow-inner" 
            />
          </div>
          <button className="px-6 py-4 bg-white border border-slate-200 rounded-[1.5rem] text-xs font-black text-slate-600 flex items-center justify-center gap-2 hover:bg-slate-50 transition-all">
            <Filter size={16} /> 상세 필터
          </button>
        </div>
      </div>

      {/* KPI Stats Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[
          { label: '평균 처리 시간', value: '4.2h', change: '-12%', icon: <Clock size={20} />, color: 'text-indigo-600 bg-indigo-50' },
          { label: '당일 반품 접수', value: '24건', change: '+2건', icon: <RotateCcw size={20} />, color: 'text-rose-600 bg-rose-50' },
          { label: '교환 요청률', value: '1.2%', change: '-0.1%', icon: <RefreshCw size={20} />, color: 'text-emerald-600 bg-emerald-50' },
          { label: '환불 총액 (오늘)', value: '₩ 842K', change: '+15%', icon: <TrendingDown size={20} />, color: 'text-slate-600 bg-slate-50' },
        ].map((stat, i) => (
          <div key={i} className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm group hover:border-indigo-400 transition-all">
            <div className="flex items-center justify-between mb-4">
              <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${stat.color} transition-transform group-hover:scale-110`}>
                {stat.icon}
              </div>
              <span className={`text-[10px] font-black px-2 py-1 rounded-lg bg-slate-50 text-slate-500`}>
                {stat.change}
              </span>
            </div>
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{stat.label}</p>
            <p className="text-2xl font-black text-slate-900 mt-1">{stat.value}</p>
          </div>
        ))}
      </div>

      {/* Main Content Area */}
      <div className="bg-white rounded-[2.5rem] border border-slate-200 shadow-sm overflow-hidden animate-in fade-in duration-500">
        <div className="flex items-center gap-2 p-6 border-b border-slate-100 overflow-x-auto no-scrollbar">
          {['ALL', 'RETURN', 'EXCHANGE', 'CANCEL'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-6 py-3 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all whitespace-nowrap ${
                activeTab === tab ? 'bg-slate-900 text-white shadow-lg' : 'bg-slate-50 text-slate-400 hover:text-slate-600 hover:bg-slate-100'
              }`}
            >
              {tab === 'ALL' ? '전체 클레임' : tab === 'RETURN' ? '반품' : tab === 'EXCHANGE' ? '교환' : '취소'}
            </button>
          ))}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left min-w-[1000px]">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50/50">
                <th className="py-6 px-8 text-[11px] font-black text-slate-400 uppercase tracking-widest">Claim Info</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Type</th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Channel</th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest">Reason / Memo</th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Status</th>
                <th className="py-6 px-8 w-20"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {MOCK_CLAIMS.filter(c => activeTab === 'ALL' || c.type === activeTab).map((claim) => (
                <tr key={claim.id} onClick={() => setSelectedClaimId(claim.id)} className="hover:bg-slate-50/80 transition-all group cursor-pointer">
                  <td className="py-6 px-8">
                    <div className="flex flex-col">
                      <span className="text-sm font-black text-slate-900 group-hover:text-indigo-600 transition-colors italic">{claim.id}</span>
                      <span className="text-[10px] font-bold text-slate-400 mt-1 uppercase tracking-tight">{claim.orderId} • {claim.customer}</span>
                    </div>
                  </td>
                  <td className="py-6 px-4 text-center">
                    <span className={`px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest border ${
                      claim.type === 'CANCEL' ? 'bg-slate-100 text-slate-600 border-slate-200' :
                      claim.type === 'RETURN' ? 'bg-rose-50 text-rose-600 border-rose-200' : 'bg-indigo-50 text-indigo-600 border-indigo-200'
                    }`}>
                      {claim.type === 'CANCEL' ? '취소' : claim.type === 'RETURN' ? '반품' : '교환'}
                    </span>
                  </td>
                  <td className="py-6 px-6 text-center">
                    <span className="text-[10px] font-black text-slate-400 border border-slate-200 px-3 py-1.5 rounded-xl uppercase tracking-widest">{claim.channel}</span>
                  </td>
                  <td className="py-6 px-6">
                    <p className="text-sm font-bold text-slate-800 truncate max-w-[200px]">{claim.reason}</p>
                    <div className="flex items-center gap-1.5 mt-1.5">
                      {claim.priority === 'URGENT' && <Zap size={12} className="text-rose-500 fill-rose-500" />}
                      <span className={`text-[9px] font-black uppercase tracking-widest ${
                        claim.priority === 'URGENT' ? 'text-rose-500' : 'text-slate-400'
                      }`}>PRIORITY: {claim.priority || 'NORMAL'}</span>
                    </div>
                  </td>
                  <td className="py-6 px-6 text-center">
                    <span className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest border ${
                      claim.status === 'REQUESTED' ? 'bg-amber-50 text-amber-600 border-amber-200' :
                      claim.status === 'PROCESSING' ? 'bg-indigo-50 text-indigo-600 border-indigo-200' : 'bg-emerald-50 text-emerald-600 border-emerald-200'
                    }`}>
                      {claim.status === 'REQUESTED' ? '접수대기' : claim.status === 'PROCESSING' ? '처리중' : '완료'}
                    </span>
                  </td>
                  <td className="py-6 px-8 text-right">
                    <ChevronRight size={18} className="text-slate-300 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <ClaimDetailModal isOpen={!!selectedClaimId} onClose={() => setSelectedClaimId(null)} claimId={selectedClaimId} />
    </div>
  );
};

export default ClaimsView;
