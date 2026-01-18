
import React, { useState, useEffect } from 'react';
import { 
  X, Package, Box, Truck, CreditCard, User, MapPin, 
  Clock, History, MoreVertical, FileText, Send,
  Printer, Database, Edit3, ChevronRight,
  UserCheck, ReceiptText, Smartphone, Mail,
  Weight, Archive, ShieldAlert, Save, RotateCcw, CheckCircle2,
  Server, Zap, ArrowRight, Share2, PackageCheck, ZapOff,
  Store, UserCircle
} from 'lucide-react';
import { OrderStatus, Order } from '../types';
import { ORDER_STATUS_MAP } from '../constants';

interface OrderDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  orderId: string | null;
}

const OrderDetailModal: React.FC<OrderDetailModalProps> = ({ isOpen, onClose, orderId }) => {
  const [order, setOrder] = useState<Order | null>(null);
  const [memoInput, setMemoInput] = useState('');
  
  // Mock fetching order detail
  useEffect(() => {
    if (orderId) {
      const isDirect = orderId.includes('-010') || orderId.includes('-045');
      setOrder({
        id: orderId,
        channel: isDirect ? 'OFFLINE' : 'NAVER',
        orderDate: '2025-01-18 14:30',
        customerName: '홍길동',
        totalAmount: 137000,
        status: 'PAID',
        fulfillmentMethod: isDirect ? 'DIRECT' : 'WMS',
        wmsNode: isDirect ? '본사 쇼룸(직배)' : '김포 자동화 센터 (JD)',
        routingLogic: isDirect ? '매장 방문 수령 / 퀵 배송' : '재고 근접 배정',
        items: []
      });
    }
  }, [orderId]);

  if (!isOpen || !order) return null;

  const isDirect = order.fulfillmentMethod === 'DIRECT';

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/70 backdrop-blur-md" onClick={onClose}></div>
      
      <div className="relative bg-white w-full max-w-6xl rounded-[3rem] shadow-2xl overflow-hidden flex flex-col max-h-[95vh] animate-in zoom-in-95 duration-300">
        
        {/* Header */}
        <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-white relative z-20">
          <div className="flex items-center gap-5">
            <div className={`w-14 h-14 rounded-[1.25rem] flex items-center justify-center text-white shadow-xl ${isDirect ? 'bg-amber-500 shadow-amber-500/20' : 'bg-indigo-600 shadow-indigo-500/20'}`}>
              {isDirect ? <PackageCheck size={28} /> : <Package size={28} />}
            </div>
            <div>
              <div className="flex items-center gap-3 mb-1">
                <h2 className="text-2xl font-black text-slate-900 tracking-tight">{order.id}</h2>
                <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest border ${
                  isDirect ? 'bg-amber-50 text-amber-600 border-amber-100' : 'bg-indigo-50 text-indigo-600 border-indigo-100'
                }`}>
                  {isDirect ? 'Direct Fulfillment' : 'WMS Standard'}
                </span>
              </div>
              <p className="text-xs text-slate-400 font-bold flex items-center gap-2">
                <Clock size={12} /> {order.orderDate} <span className="text-slate-200">|</span> 
                <span className="text-indigo-500 font-black">채널: {order.channel}</span>
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-3 hover:bg-slate-100 rounded-2xl text-slate-400">
            <X size={28} />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-10 space-y-10">
          
          {/* Fulfillment Insight Visualization */}
          <div className={`${isDirect ? 'bg-amber-950' : 'bg-slate-900'} rounded-[2.5rem] p-10 text-white relative overflow-hidden transition-colors duration-500`}>
            <div className={`absolute -right-10 -bottom-10 w-64 h-64 rounded-full blur-3xl ${isDirect ? 'bg-amber-600/10' : 'bg-indigo-600/20'}`}></div>
            
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-10">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-xl ${isDirect ? 'bg-amber-500' : 'bg-indigo-500'}`}>
                    {isDirect ? <ZapOff size={20} /> : <Share2 size={20} />}
                  </div>
                  <div>
                    <h3 className="text-lg font-black tracking-tight">
                      {isDirect ? '직접 배송 프로세스 (Non-WMS)' : '풀필먼트 라우팅 분석 (WMS Routing)'}
                    </h3>
                    <p className="text-[10px] opacity-60 font-bold uppercase tracking-widest">
                      {isDirect ? 'Merchant Direct Process Enabled' : 'OMS Routing Engine v4.2'}
                    </p>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between max-w-4xl mx-auto py-4">
                {/* Step 1: Channel Source */}
                <div className="flex flex-col items-center gap-4">
                  <div className="w-16 h-16 bg-white/5 border border-white/10 rounded-2xl flex items-center justify-center text-2xl shadow-inner">🛍️</div>
                  <p className="text-[10px] font-black text-indigo-400 uppercase">Source</p>
                </div>

                <div className="flex-1 px-4"><div className="w-full h-0.5 bg-white/10"></div></div>

                {/* Step 2: Routing / Logic */}
                <div className={`flex flex-col items-center gap-4 px-8 py-6 bg-white/5 border border-white/10 rounded-[2rem] shadow-xl ${isDirect ? 'border-amber-500/30' : 'border-indigo-500/30'}`}>
                   <div className={`w-12 h-12 rounded-xl flex items-center justify-center shadow-lg ${isDirect ? 'bg-amber-600 shadow-amber-500/20' : 'bg-indigo-600 shadow-indigo-500/20'}`}>
                     {isDirect ? <Store size={24} /> : <Database size={24} />}
                   </div>
                   <div className="text-center">
                     <p className="text-xs font-black">{isDirect ? 'Merchant Pickup' : 'Auto Routing'}</p>
                     <p className="text-[9px] text-slate-400 font-medium mt-1">{order.routingLogic}</p>
                   </div>
                </div>

                <div className="flex-1 px-4"><div className="w-full h-0.5 bg-white/10"></div></div>

                {/* Step 3: Destination Node */}
                <div className="flex flex-col items-center gap-4">
                  <div className={`w-16 h-16 rounded-2xl flex items-center justify-center shadow-lg ${isDirect ? 'bg-amber-500/10 border border-amber-500/30 text-amber-400' : 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-400'}`}>
                    {isDirect ? <UserCircle size={32} /> : <Server size={32} />}
                  </div>
                  <div className="text-center">
                    <p className="text-[10px] uppercase font-black">{isDirect ? 'Direct Handle' : 'WMS Node'}</p>
                    <p className="text-xs font-black">{order.wmsNode}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
            <div className="lg:col-span-8 space-y-10">
               {/* 1. Progress Stepper */}
               <div className="flex items-center justify-between px-12 py-10 bg-slate-50 rounded-[3rem] border border-slate-100">
                {['결제완료', isDirect ? '직송지시' : 'WMS 전송', isDirect ? '준비완료' : '상품피킹', '배송완료'].map((step, idx) => (
                  <div key={idx} className="flex flex-col items-center gap-4 relative z-10 flex-1">
                    <div className={`w-14 h-14 rounded-2xl flex items-center justify-center text-lg font-black transition-all ${idx < 2 ? (isDirect ? 'bg-amber-500' : 'bg-indigo-600') + ' text-white shadow-xl' : 'bg-white border-2 border-slate-100 text-slate-200'}`}>
                      {idx + 1}
                    </div>
                    <p className={`text-xs font-black ${idx < 2 ? 'text-slate-900' : 'text-slate-300'}`}>{step}</p>
                  </div>
                ))}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                 <div className="bg-white p-8 rounded-[2.5rem] border border-slate-100 shadow-sm space-y-6">
                    <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                      <User size={14} className="text-indigo-600" /> 수령인 정보
                    </h3>
                    <div className="space-y-4">
                       <p className="text-sm font-black text-slate-800">{order.customerName} | 010-****-****</p>
                       <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 text-xs font-bold text-slate-600 leading-relaxed">
                          서울특별시 강남구 테헤란로 123, 10층 (역삼동, OMS 타워)
                       </div>
                    </div>
                 </div>

                 <div className="bg-white p-8 rounded-[2.5rem] border border-slate-100 shadow-sm space-y-6">
                    <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                      <Truck size={14} className="text-indigo-600" /> 배송 특이사항
                    </h3>
                    <div className="p-6 bg-indigo-50 border border-indigo-100 rounded-3xl">
                       <p className="text-xs text-indigo-700 font-bold leading-relaxed">
                         {isDirect ? "본 주문은 WMS 자동 배정이 제외된 건입니다. 담당자가 수동으로 배송 수단을 선택하고 송장을 입력해야 합니다." : "표준 WMS 라우팅이 적용된 주문입니다. 김포 센터에서 정해진 시간에 일괄 출고됩니다."}
                       </p>
                    </div>
                 </div>
              </div>
            </div>

            <div className="lg:col-span-4 space-y-8">
                <div className="bg-slate-900 rounded-[2.5rem] p-8 text-white shadow-2xl">
                  <h3 className="text-[11px] font-black text-slate-500 uppercase tracking-widest mb-8 italic">Billing Summary</h3>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center text-xs font-bold text-slate-500">
                      <span>상품 합계</span>
                      <span className="text-white">₩ 134,000</span>
                    </div>
                    <div className="flex justify-between items-center text-xs font-bold text-slate-500">
                      <span>배송비</span>
                      <span className="text-white">₩ 3,000</span>
                    </div>
                    <div className="h-px bg-white/10 my-4"></div>
                    <div className="flex justify-between items-end">
                      <span className="text-sm font-black text-slate-400 uppercase tracking-widest">Total</span>
                      <p className={`text-3xl font-black ${isDirect ? 'text-amber-400' : 'text-indigo-400'} italic`}>₩ 137,000</p>
                    </div>
                  </div>
                </div>

                <div className="p-4 bg-slate-50 rounded-3xl border border-slate-100">
                  <button className="w-full py-4 bg-white border border-slate-200 text-slate-600 text-xs font-black uppercase tracking-widest rounded-2xl hover:bg-slate-100 transition-all">
                    주문 로그 확인
                  </button>
                </div>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="p-8 bg-slate-50 border-t border-slate-100 flex justify-end gap-3 relative z-20">
          <button onClick={onClose} className="px-10 py-4 bg-white border border-slate-200 text-slate-500 text-sm font-black rounded-2xl">닫기</button>
          {isDirect ? (
            <button className="px-12 py-4 bg-amber-600 text-white text-sm font-black rounded-2xl shadow-xl shadow-amber-500/20 hover:bg-amber-700 transition-all flex items-center gap-3">
              <PackageCheck size={20} /> 직송 송장 직접 입력
            </button>
          ) : (
            <button className="px-12 py-4 bg-indigo-600 text-white text-sm font-black rounded-2xl shadow-xl shadow-indigo-500/20 hover:bg-indigo-700 transition-all">
              WMS 강제 출고 지시
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default OrderDetailModal;
