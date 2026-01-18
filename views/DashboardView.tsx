
import React from 'react';
import { 
  Activity, ShoppingCart, ArrowRight, Sparkles, Truck, 
  MessageCircle, Zap, AlertTriangle, TrendingUp,
  PackageCheck, Clock, ChevronRight, AlertCircle
} from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const chartData = [
  { time: '09:00', orders: 12 },
  { time: '10:00', orders: 25 },
  { time: '11:00', orders: 45 },
  { time: '12:00', orders: 38 },
  { time: '13:00', orders: 52 },
  { time: '14:00', orders: 65 },
  { time: '15:00', orders: 48 },
  { time: '16:00', orders: 55 },
];

const ActionCard = ({ title, count, color, icon: Icon, trend }: any) => (
  <button className="bg-white p-6 rounded-[2.5rem] border border-slate-200 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all text-left group flex flex-col justify-between h-[200px] relative overflow-hidden">
    <div className={`absolute -right-4 -top-4 w-24 h-24 rounded-full opacity-[0.03] group-hover:opacity-[0.08] transition-opacity ${color}`}></div>
    <div>
      <div className={`w-12 h-12 rounded-2xl flex items-center justify-center mb-4 transition-transform group-hover:scale-110 ${color.replace('bg-', 'bg-').replace('-500', '-50')} ${color.replace('bg-', 'text-')}`}>
        <Icon size={24} />
      </div>
      <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest break-keep">{title}</h3>
    </div>
    <div className="flex items-end justify-between">
      <div className="space-y-1">
        <span className="text-3xl font-black text-slate-900 tracking-tighter">{count}</span>
        {trend && <p className={`text-[10px] font-bold ${trend.includes('+') ? 'text-emerald-500' : 'text-rose-500'}`}>{trend} from yesterday</p>}
      </div>
      <div className="w-10 h-10 bg-slate-50 rounded-xl flex items-center justify-center text-slate-300 group-hover:bg-indigo-600 group-hover:text-white transition-all">
        <ArrowRight size={18} />
      </div>
    </div>
  </button>
);

const DashboardView: React.FC = () => {
  return (
    <div className="space-y-8 pb-32">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 px-4 py-2">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <h1 className="text-4xl font-black text-slate-900 tracking-tight italic uppercase">Control Tower</h1>
            <div className="flex px-4 py-1.5 bg-indigo-600 text-white rounded-xl text-[10px] font-black uppercase tracking-[0.2em] items-center gap-2 shadow-lg shadow-indigo-200">
               <Sparkles size={12} className="animate-pulse" /> Real-time Sync
            </div>
          </div>
          <p className="text-slate-500 font-medium">관리자님, 현재 시스템은 <span className="text-indigo-600 font-bold">정상 운영</span> 중이며 총 4개의 채널이 동기화되고 있습니다.</p>
        </div>
        <div className="flex gap-2">
           <button className="px-6 py-3 bg-white border border-slate-200 rounded-2xl text-xs font-black uppercase tracking-widest text-slate-600 hover:bg-slate-50 transition-all">Report Center</button>
           <button className="px-6 py-3 bg-slate-900 text-white rounded-2xl text-xs font-black uppercase tracking-widest shadow-xl active:scale-95 transition-all">Manual Order</button>
        </div>
      </div>

      {/* Primary KPI Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
        <ActionCard title="Pending Shipment" count="128" color="bg-indigo-500" icon={Truck} trend="+12%" />
        <ActionCard title="Stock Shortage" count="5" color="bg-rose-500" icon={AlertTriangle} trend="-2건" />
        <ActionCard title="Return Requested" count="24" color="bg-amber-500" icon={RotateCcwIcon} trend="주의" />
        <ActionCard title="Daily Revenue" count="₩14.2M" color="bg-emerald-500" icon={TrendingUp} trend="+8.4%" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Chart Section */}
        <div className="lg:col-span-2 bg-white p-10 rounded-[3rem] border border-slate-200 shadow-sm space-y-10 min-w-0">
          <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-black text-slate-900 italic uppercase">Real-time Order Flow</h3>
                <p className="text-xs text-slate-400 font-bold mt-1 tracking-widest uppercase">Last 24 Hours Performance</p>
              </div>
              <div className="flex gap-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-indigo-500 rounded-full"></div>
                  <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">Today</span>
                </div>
              </div>
          </div>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorOrders" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis 
                  dataKey="time" 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fontSize: 10, fontWeight: 700, fill: '#94a3b8'}}
                  dy={10}
                />
                <YAxis 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fontSize: 10, fontWeight: 700, fill: '#94a3b8'}}
                />
                <Tooltip 
                  contentStyle={{ borderRadius: '20px', border: 'none', boxShadow: '0 20px 50px rgba(0,0,0,0.1)', padding: '12px' }}
                  itemStyle={{ fontSize: '12px', fontWeight: '900', color: '#6366f1' }}
                />
                <Area type="monotone" dataKey="orders" stroke="#6366f1" strokeWidth={4} fillOpacity={1} fill="url(#colorOrders)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Actionable Alerts Sidebar */}
        <div className="space-y-6">
          <div className="bg-slate-900 rounded-[3rem] p-8 text-white shadow-2xl relative overflow-hidden group min-h-[450px] flex flex-col">
              <div className="absolute top-0 right-0 p-8 opacity-10 group-hover:rotate-12 transition-transform duration-700">
                <AlertCircle size={120} />
              </div>
              <div className="relative z-10 space-y-8 flex-1">
                <div className="flex items-center gap-3">
                   <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center">
                     <Zap size={20} />
                   </div>
                   <h4 className="text-lg font-black italic uppercase tracking-widest">Operational Alerts</h4>
                </div>
                <div className="space-y-4">
                   {[
                     { label: '지연 주문', msg: '3건의 주문이 48시간 이상 미출고 상태입니다.', color: 'text-amber-400' },
                     { label: 'API 장애', msg: '아마존 US 인터페이스에서 인증 오류가 감지되었습니다.', color: 'text-rose-400' },
                     { label: '재고 부족', msg: '프리미엄 자켓의 재고가 할당량 미만입니다.', color: 'text-indigo-400' },
                   ].map((alert, i) => (
                     <div key={i} className="p-5 bg-white/5 border border-white/10 rounded-2xl hover:bg-white/10 transition-colors cursor-pointer group/alert">
                        <div className="flex justify-between items-center mb-1">
                          <span className={`text-[10px] font-black uppercase tracking-widest ${alert.color}`}>{alert.label}</span>
                          <ChevronRight size={14} className="text-white/20 group-hover/alert:text-white transition-all" />
                        </div>
                        <p className="text-xs text-white/70 font-medium leading-relaxed">{alert.msg}</p>
                     </div>
                   ))}
                </div>
              </div>
              <button className="relative z-10 w-full py-4 bg-white text-slate-900 rounded-2xl text-[10px] font-black uppercase tracking-widest hover:bg-indigo-50 transition-all shadow-xl">전체 경보 확인</button>
          </div>
        </div>
      </div>
    </div>
  );
};

const RotateCcwIcon = ({ size, className }: any) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="M1 4v6h6" /><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
  </svg>
);

export default DashboardView;
