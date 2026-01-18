
import React, { useState } from 'react';
import { Truck, Printer, Package, ChevronRight, MapPin, Phone, RefreshCw, CheckCircle, Search } from 'lucide-react';
import ShippingDetailModal from '../components/ShippingDetailModal';

const MOCK_SHIPMENTS = [
  { id: 'SHIP-101', orderId: 'ORD-20250118-001', carrier: 'CJ대한통운', trackingNo: '6542-1234-9001', receiver: '이몽룡', address: '서울시 강남구 테헤란로 123', status: 'IN_TRANSIT', time: '2025-01-18 10:00' },
  { id: 'SHIP-102', orderId: 'ORD-20250118-002', carrier: '우체국택배', trackingNo: '1102-3345-6678', receiver: '성춘향', address: '전라북도 남원시 광한루원길 1', status: 'READY', time: '2025-01-18 11:30' },
  { id: 'SHIP-103', orderId: 'ORD-20250117-089', carrier: '로젠택배', trackingNo: '4456-9900-1122', receiver: '홍길동', address: '강원도 속초시 설악산로 1', status: 'DELIVERED', time: '2025-01-17 15:45' },
];

const ShippingView: React.FC = () => {
  const [selectedShipmentId, setSelectedShipmentId] = useState<string | null>(null);
  const [isSyncing, setIsSyncing] = useState(false);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  const handleSync = () => {
    setIsSyncing(true);
    setTimeout(() => {
      setIsSyncing(false);
      alert("전 채널 배송 정보 동기화가 완료되었습니다.");
    }, 2000);
  };

  const handleBulkPrint = () => {
    if (selectedItems.length === 0) {
      alert("출력할 항목을 먼저 선택해주세요.");
      return;
    }
    alert(`${selectedItems.length}건의 송장 출력을 시작합니다. 프린터 연결을 확인하세요.`);
  };

  const toggleItem = (id: string) => {
    setSelectedItems(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">배송 관리</h1>
          <p className="text-slate-500 text-sm">실시간 택배사 API 연동을 통해 배송 상태를 추적합니다.</p>
        </div>
        <div className="flex gap-2">
          <button 
            onClick={handleBulkPrint}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 rounded-xl text-sm font-medium hover:bg-slate-50 transition-colors"
          >
            <Printer size={16} />
            송장 일괄 출력
          </button>
          <button 
            onClick={handleSync}
            disabled={isSyncing}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-xl text-sm font-bold hover:bg-indigo-700 shadow-lg shadow-indigo-500/20 transition-all disabled:opacity-70"
          >
            {isSyncing ? <RefreshCw size={16} className="animate-spin" /> : <Truck size={16} />}
            배송 데이터 연동
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {[
          { label: '출고 대기', count: 12, icon: <Package size={24} />, color: 'bg-amber-50 text-amber-500' },
          { label: '배송 중', count: 84, icon: <Truck size={24} />, color: 'bg-blue-50 text-blue-500' },
          { label: '배송 완료 (오늘)', count: 42, icon: <CheckCircleIcon size={24} />, color: 'bg-emerald-50 text-emerald-500' },
        ].map((stat, idx) => (
          <div key={idx} className={`bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center gap-4 transition-all ${isSyncing ? 'animate-pulse' : ''}`}>
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${stat.color}`}>
              {stat.icon}
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase mb-1">{stat.label}</p>
              <p className="text-2xl font-bold text-slate-900">{stat.count}건</p>
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center gap-4 bg-slate-50/30">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
            <input type="text" placeholder="수령인, 송장번호, 주문번호 검색..." className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-sm outline-none" />
          </div>
          <p className="text-xs text-slate-400 font-medium">선택됨: <span className="text-blue-600 font-bold">{selectedItems.length}</span></p>
        </div>
        <div className="divide-y divide-slate-50">
          {MOCK_SHIPMENTS.map((ship) => (
            <div 
              key={ship.id} 
              className={`p-6 hover:bg-slate-50 transition-all flex flex-wrap lg:flex-nowrap items-center gap-6 group cursor-pointer ${selectedItems.includes(ship.id) ? 'bg-blue-50/30' : ''}`}
              onClick={() => toggleItem(ship.id)}
            >
              <div className="w-5 h-5 border-2 border-slate-200 rounded flex items-center justify-center transition-colors group-hover:border-blue-400">
                {selectedItems.includes(ship.id) && <div className="w-2.5 h-2.5 bg-blue-600 rounded-sm"></div>}
              </div>

              <div className="flex-1 min-w-[200px]">
                <div className="flex items-center gap-3 mb-2">
                  <span className={`px-2 py-1 rounded text-[10px] font-bold uppercase tracking-widest ${
                    ship.status === 'READY' ? 'bg-amber-100 text-amber-600' :
                    ship.status === 'IN_TRANSIT' ? 'bg-blue-100 text-blue-600' : 'bg-emerald-100 text-emerald-600'
                  }`}>
                    {ship.status === 'READY' ? '출고 대기' : ship.status === 'IN_TRANSIT' ? '배송 중' : '배송 완료'}
                  </span>
                  <span className="text-[10px] text-slate-400 font-bold">{ship.id}</span>
                </div>
                <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
                  {ship.receiver}
                  <span className="text-[11px] font-medium text-slate-400">({ship.orderId})</span>
                </h3>
              </div>

              <div className="flex-1 min-w-[300px] border-l border-slate-100 pl-6 hidden md:block">
                <div className="flex items-start gap-2 text-slate-600 mb-1">
                  <MapPin size={14} className="mt-1 flex-shrink-0" />
                  <p className="text-xs line-clamp-1">{ship.address}</p>
                </div>
                <div className="flex items-center gap-2 text-slate-400">
                  <Phone size={14} />
                  <p className="text-[10px] font-medium">010-****-****</p>
                </div>
              </div>

              <div className="flex-1 border-l border-slate-100 pl-6">
                <p className="text-[10px] font-bold text-slate-400 uppercase mb-1">택배사 / 송장번호</p>
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-slate-700">{ship.carrier}</span>
                  <span className="text-xs font-medium text-blue-600 hover:underline">{ship.trackingNo}</span>
                </div>
              </div>

              <div className="flex items-center gap-3" onClick={(e) => e.stopPropagation()}>
                <button 
                  onClick={() => setSelectedShipmentId(ship.id)}
                  className="px-4 py-2 bg-white border border-slate-200 text-slate-600 rounded-xl text-xs font-bold hover:bg-blue-600 hover:text-white hover:border-blue-600 transition-all"
                >
                  배송 추적
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <ShippingDetailModal 
        isOpen={!!selectedShipmentId} 
        onClose={() => setSelectedShipmentId(null)} 
        shipmentId={selectedShipmentId} 
      />
    </div>
  );
};

const CheckCircleIcon = ({ size, className }: { size: number, className?: string }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
    <polyline points="22 4 12 14.01 9 11.01"></polyline>
  </svg>
);

export default ShippingView;
