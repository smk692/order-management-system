
import React, { useState } from 'react';
import { 
  Search, Filter, Download, MoreHorizontal, Plus, 
  Truck, XCircle, FileText, ChevronRight, 
  Printer, CheckSquare, Square, SearchX,
  Server, MapPin, PackageCheck, ZapOff, ShoppingBag, 
  Clock, Activity, AlertCircle, X, Tag, Trash2, Send
} from 'lucide-react';
import { ORDER_STATUS_MAP } from '../constants';
import { Order, OrderStatus } from '../types';
import OrderDetailModal from '../components/OrderDetailModal';
import NewOrderModal from '../components/NewOrderModal';

const INITIAL_ORDERS: Order[] = [
  { id: 'ORD-20250118-001', channel: 'MALL', orderDate: '2025-01-18 14:30', customerName: '홍길동', totalAmount: 39000, status: 'NEW', fulfillmentMethod: 'WMS', wmsNode: '김포 자동화 센터 (JD)', routingLogic: '재고 우선 배정', items: [] },
  { id: 'ORD-20250118-002', channel: 'NAVER', orderDate: '2025-01-18 13:15', customerName: '김철수', totalAmount: 128000, status: 'PREPARING', fulfillmentMethod: 'WMS', wmsNode: '용인 3PL (CJ)', routingLogic: '권역별 최단거리', items: [] },
  { id: 'ORD-20250118-010', channel: 'OFFLINE', orderDate: '2025-01-18 15:45', customerName: '이직송', totalAmount: 52000, status: 'NEW', fulfillmentMethod: 'DIRECT', routingLogic: '매장 직접 발송', items: [] },
  { id: 'ORD-20250117-089', channel: 'COUPANG', orderDate: '2025-01-17 18:40', customerName: '이영희', totalAmount: 69000, status: 'IN_DELIVERY', fulfillmentMethod: 'WMS', wmsNode: '쿠팡 밀크런 노드', routingLogic: '채널 전용 풀필먼트', items: [] },
];

const OrderView: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>(INITIAL_ORDERS);
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [detailOrderId, setDetailOrderId] = useState<string | null>(null);
  const [isNewOrderModalOpen, setIsNewOrderModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [fulfillmentFilter, setFulfillmentFilter] = useState<'ALL' | 'WMS' | 'DIRECT'>('ALL');

  const filteredOrders = orders.filter(o => {
    const statusMatch = filterStatus === 'ALL' || o.status === filterStatus;
    const fulfillmentMatch = fulfillmentFilter === 'ALL' || o.fulfillmentMethod === fulfillmentFilter;
    const searchMatch = o.id.toLowerCase().includes(searchQuery.toLowerCase()) || 
                      o.customerName.toLowerCase().includes(searchQuery.toLowerCase());
    return statusMatch && fulfillmentMatch && searchMatch;
  });

  const toggleSelect = (id: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredOrders.length && filteredOrders.length > 0) setSelectedIds([]);
    else setSelectedIds(filteredOrders.map(o => o.id));
  };

  return (
    <div className="space-y-8 relative pb-32">
      {/* Search Header Section */}
      <div className="bg-white p-8 rounded-[2.5rem] border border-slate-200 shadow-sm space-y-8 transition-all">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="space-y-2">
            <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3 italic uppercase">
              Orders Console
            </h1>
            <p className="text-slate-500 font-medium text-sm">WMS 자동 라우팅 및 수동 직접 배송 주문을 통합 관리합니다.</p>
          </div>
          <div className="flex items-center gap-2">
            <button className="flex items-center gap-2 px-5 py-3 bg-white border border-slate-200 rounded-2xl text-sm font-bold text-slate-600 hover:bg-slate-50 transition-all">
              <Download size={18} />
            </button>
            <button 
              onClick={() => setIsNewOrderModalOpen(true)}
              className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-2xl text-sm font-black hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-500/20 active:scale-95"
            >
              <Plus size={20} /> 신규 주문 등록
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
              placeholder="주문번호, 고객명, 배정 정보를 검색하세요..." 
              className="w-full pl-14 pr-6 py-4 bg-slate-50 border border-transparent rounded-[1.5rem] text-sm font-bold outline-none focus:bg-white focus:ring-4 focus:ring-indigo-500/5 focus:border-indigo-200 transition-all shadow-inner" 
            />
          </div>
          <div className="flex bg-slate-100 p-1.5 rounded-[1.5rem] border border-slate-200">
             <button onClick={() => setFulfillmentFilter('ALL')} className={`px-5 py-2.5 rounded-2xl text-[10px] font-black transition-all ${fulfillmentFilter === 'ALL' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-400'}`}>ALL</button>
             <button onClick={() => setFulfillmentFilter('WMS')} className={`px-5 py-2.5 rounded-2xl text-[10px] font-black transition-all ${fulfillmentFilter === 'WMS' ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-400'}`}>WMS</button>
             <button onClick={() => setFulfillmentFilter('DIRECT')} className={`px-5 py-2.5 rounded-2xl text-[10px] font-black transition-all ${fulfillmentFilter === 'DIRECT' ? 'bg-amber-500 text-white shadow-sm' : 'text-slate-400'}`}>DIRECT</button>
          </div>
        </div>
      </div>

      {/* KPI Stats Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 px-2">
        {[
          { label: '당일 주문 총액', value: '₩ 14.2M', change: '+12%', icon: <ShoppingBag size={20} />, color: 'text-indigo-600 bg-indigo-50' },
          { label: '출고 대기 중', value: '45건', change: '+5건', icon: <PackageCheck size={20} />, color: 'text-amber-600 bg-amber-50' },
          { label: '평균 배송 시간', value: '1.8일', change: '-0.2일', icon: <Clock size={20} />, color: 'text-emerald-600 bg-emerald-50' },
          { label: '운영 이슈 건수', value: '3건', change: '주의', icon: <AlertCircle size={20} />, color: 'text-rose-600 bg-rose-50' },
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
        <div className="flex items-center gap-2 p-6 border-b border-slate-100 overflow-x-auto no-scrollbar bg-slate-50/30">
          {['ALL', 'NEW', 'PAID', 'PREPARING', 'SHIPPED', 'CANCELLED'].map((s) => (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={`px-6 py-3 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all whitespace-nowrap ${
                filterStatus === s ? 'bg-slate-900 text-white shadow-lg' : 'bg-white text-slate-400 border border-slate-100 hover:text-slate-600 hover:bg-slate-50'
              }`}
            >
              {s === 'ALL' ? '전체 주문' : ORDER_STATUS_MAP[s as OrderStatus]?.label || s}
            </button>
          ))}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left min-w-[1000px]">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50/50">
                <th className="w-16 py-6 px-6 text-center">
                  <button onClick={toggleSelectAll} className="text-slate-400 transition-colors hover:text-indigo-600">
                    {selectedIds.length === filteredOrders.length && filteredOrders.length > 0 ? <CheckSquare size={20} className="text-indigo-600" /> : <Square size={20} />}
                  </button>
                </th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest">Order Details</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Fulfillment Path</th>
                <th className="py-6 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest">배정 노드 / 처리 주체</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-right">Amount</th>
                <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Status</th>
                <th className="py-6 px-8 w-20"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {filteredOrders.length > 0 ? filteredOrders.map((order) => {
                const statusInfo = ORDER_STATUS_MAP[order.status];
                const isDirect = order.fulfillmentMethod === 'DIRECT';
                const isSelected = selectedIds.includes(order.id);
                return (
                  <tr key={order.id} onClick={() => setDetailOrderId(order.id)} className={`hover:bg-slate-50/80 transition-all group cursor-pointer ${isSelected ? 'bg-indigo-50/30' : ''}`}>
                    <td className="py-6 px-6 text-center" onClick={(e) => e.stopPropagation()}>
                       <button onClick={() => toggleSelect(order.id)} className={`${isSelected ? 'text-indigo-600' : 'text-slate-300 hover:text-indigo-400'}`}>
                          {isSelected ? <CheckSquare size={20} /> : <Square size={20} />}
                       </button>
                    </td>
                    <td className="py-6 px-6">
                      <div className="flex flex-col">
                        <span className="text-sm font-black text-slate-900 group-hover:text-indigo-600 transition-colors italic tracking-tight">{order.id}</span>
                        <span className="text-[10px] font-bold text-slate-400 mt-1 uppercase tracking-tight">{order.customerName} • {order.channel}</span>
                      </div>
                    </td>
                    <td className="py-6 px-4 text-center">
                      <span className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest border ${
                        isDirect ? 'bg-amber-50 border-amber-200 text-amber-600' : 'bg-indigo-50 border-indigo-200 text-indigo-600'
                      }`}>
                        {isDirect ? <ZapOff size={14} /> : <Server size={14} />}
                        {isDirect ? 'Direct' : 'WMS'}
                      </span>
                    </td>
                    <td className="py-6 px-6">
                       <div className="flex items-center gap-4">
                         <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isDirect ? 'bg-amber-100 text-amber-600 shadow-sm' : 'bg-slate-100 text-slate-600 shadow-sm'}`}>
                           {isDirect ? <PackageCheck size={18} /> : <Server size={18} />}
                         </div>
                         <div className="min-w-0">
                            <p className="text-xs font-black text-slate-800 truncate">{order.wmsNode || '미배정 (직접 발송)'}</p>
                            <p className="text-[9px] text-slate-400 font-bold uppercase tracking-widest mt-0.5 truncate">{order.routingLogic}</p>
                         </div>
                       </div>
                    </td>
                    <td className="py-6 px-4 text-right font-black text-slate-900 text-sm italic">
                      ₩ {order.totalAmount.toLocaleString()}
                    </td>
                    <td className="py-6 px-4 text-center">
                      <span className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest border ${statusInfo.color} border-current`}>
                        {statusInfo.label}
                      </span>
                    </td>
                    <td className="py-6 px-8 text-right">
                      <ChevronRight size={18} className="text-slate-300 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
                    </td>
                  </tr>
                );
              }) : (
                <tr>
                  <td colSpan={7} className="py-32 text-center">
                    <div className="flex flex-col items-center gap-4 text-slate-300">
                      <SearchX size={48} />
                      <p className="text-sm font-bold text-slate-400 uppercase tracking-widest italic">No orders found</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Bulk Action Bar */}
      {selectedIds.length > 0 && (
        <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-[200] w-full max-w-lg px-4 animate-in slide-in-from-bottom-5 duration-300">
           <div className="bg-slate-900 text-white p-5 rounded-[2.5rem] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.5)] flex items-center justify-between gap-6 border border-white/10 backdrop-blur-md">
              <div className="flex items-center gap-4">
                 <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center font-black text-lg shadow-inner">
                    {selectedIds.length}
                 </div>
                 <div>
                    <p className="text-xs font-black uppercase tracking-widest text-indigo-400 italic">Orders Ready</p>
                    <p className="text-[10px] text-slate-500 font-bold uppercase">Batch Actions</p>
                 </div>
              </div>
              <div className="flex items-center gap-2">
                 <button title="송장 출력" className="p-3 hover:bg-white/10 rounded-xl text-slate-400 hover:text-white transition-all"><Printer size={20}/></button>
                 <button title="출고 지시" className="p-3 hover:bg-indigo-500/20 rounded-xl text-indigo-400 hover:text-white transition-all"><Send size={20}/></button>
                 <button title="상태 변경" className="p-3 hover:bg-white/10 rounded-xl text-slate-400 hover:text-white transition-all"><Tag size={20}/></button>
                 <div className="w-px h-6 bg-white/10 mx-2"></div>
                 <button onClick={() => setSelectedIds([])} className="p-3 hover:bg-white/10 rounded-xl text-slate-400"><X size={20}/></button>
              </div>
           </div>
        </div>
      )}

      <OrderDetailModal isOpen={!!detailOrderId} onClose={() => setDetailOrderId(null)} orderId={detailOrderId} />
      <NewOrderModal isOpen={isNewOrderModalOpen} onClose={() => setIsNewOrderModalOpen(false)} onAdd={(o) => setOrders([o, ...orders])} />
    </div>
  );
};

export default OrderView;
