
import React, { useState } from 'react';
import { 
  X, AlertCircle, RefreshCcw, Truck, CornerUpLeft, 
  CheckCircle, MessageSquare, ArrowRight, ShieldCheck, 
  Trash2, Plus, Info, Sparkles, CreditCard, Box, 
  History, Camera, ChevronRight, CheckCircle2, AlertTriangle,
  RotateCcw, MapPin, XCircle, FileText, ShieldAlert
} from 'lucide-react';

interface ClaimDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  claimId: string | null;
}

const ClaimDetailModal: React.FC<ClaimDetailModalProps> = ({ isOpen, onClose, claimId }) => {
  const [activeSubTab, setActiveSubTab] = useState<'details' | 'logistics' | 'refund'>('details');

  if (!isOpen || !claimId) return null;

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="claim-detail-title">
      <div className="absolute inset-0 bg-slate-900/70 backdrop-blur-md" onClick={onClose}></div>

      <div className="relative bg-white w-full max-w-6xl rounded-[3rem] shadow-2xl overflow-hidden flex flex-col max-h-[92vh] animate-in zoom-in-95 duration-300">
        {/* Header */}
        <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-white relative z-20">
          <div className="flex items-center gap-5">
            <div className="w-14 h-14 bg-rose-600 rounded-2xl flex items-center justify-center text-white shadow-xl shadow-rose-500/20">
              <RotateCcw size={28} />
            </div>
            <div>
              <div className="flex items-center gap-3 mb-1">
                <h2 id="claim-detail-title" className="text-2xl font-black text-slate-900 tracking-tight">{claimId}</h2>
                <span className="px-3 py-1 bg-rose-50 text-rose-600 border border-rose-100 rounded-full text-[11px] font-black uppercase tracking-widest">반품 요청 (Return Requested)</span>
              </div>
              <p className="text-xs text-slate-400 font-bold tracking-widest uppercase flex items-center gap-2">
                Order ID: ORD-20250118-005 <span className="text-slate-200">|</span> Channel: 자사몰
              </p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button className="flex items-center gap-2 px-4 py-2 bg-slate-100 text-slate-600 rounded-xl text-xs font-black hover:bg-slate-200 transition-all">
              <History size={14} /> 히스토리
            </button>
            <button onClick={onClose} className="p-3 hover:bg-slate-100 rounded-2xl text-slate-400 transition-colors">
              <X size={28} />
            </button>
          </div>
        </div>

        {/* Tab Switcher */}
        <div className="flex px-8 border-b border-slate-100 bg-slate-50/50">
          {[
            { id: 'details', label: '클레임 상세 사유', icon: <AlertCircle size={16} /> },
            { id: 'logistics', label: '회수 물류 추적', icon: <Truck size={16} /> },
            { id: 'refund', label: '환불 정산 계산기', icon: <CreditCard size={16} /> },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveSubTab(tab.id as any)}
              className={`flex items-center gap-2 px-8 py-5 text-sm font-black transition-all relative ${
                activeSubTab === tab.id ? 'text-rose-600' : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              {tab.icon} {tab.label}
              {activeSubTab === tab.id && <div className="absolute bottom-0 left-0 right-0 h-1 bg-rose-600 rounded-t-full"></div>}
            </button>
          ))}
        </div>

        <div className="flex-1 overflow-y-auto p-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
            {/* Left Content Column */}
            <div className="lg:col-span-8 space-y-10">
              {activeSubTab === 'details' && (
                <div className="space-y-10 animate-in fade-in slide-in-from-bottom-4">
                  {/* AI Analysis Card */}
                  <div className="p-8 bg-gradient-to-br from-indigo-600 to-violet-700 rounded-[2.5rem] text-white relative overflow-hidden group shadow-2xl shadow-indigo-500/20">
                    <div className="absolute -right-10 -top-10 w-40 h-40 bg-white/10 rounded-full blur-3xl group-hover:scale-150 transition-transform duration-700"></div>
                    <div className="flex items-center gap-2 mb-6">
                      <Sparkles size={20} className="text-blue-300 animate-pulse" />
                      <span className="text-[10px] font-black uppercase tracking-[0.2em] text-blue-200">Gemini AI Analysis Report</span>
                    </div>
                    <h3 className="text-xl font-black mb-4 leading-tight">"단순 변심에 의한 반품 확률 92%"</h3>
                    <p className="text-sm text-indigo-100/80 leading-relaxed mb-8">
                      고객의 구매 이력과 클레임 텍스트 패턴 분석 결과, 상품 결함보다는 사이즈 미스 또는 단순 변심일 확률이 매우 높습니다. 
                      고객 부담 배송비를 차감한 환불 처리를 제안하며, 재구매 유도를 위한 5% 할인 쿠폰 발급을 권장합니다.
                    </p>
                    <div className="flex gap-3">
                      <button className="px-6 py-2.5 bg-white text-indigo-600 rounded-xl text-xs font-black shadow-xl hover:bg-slate-50 active:scale-95 transition-all">AI 권장사항 적용</button>
                      <button className="px-6 py-2.5 bg-white/10 border border-white/20 text-white rounded-xl text-xs font-black hover:bg-white/20 active:scale-95 transition-all">상세 분석 로그</button>
                    </div>
                  </div>

                  {/* Reasons & Evidence */}
                  <div className="space-y-6">
                    <h4 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2 ml-2">
                      <MessageSquare size={14} className="text-rose-500" /> 고객 작성 사유 및 증빙
                    </h4>
                    <div className="p-8 bg-slate-50 border border-slate-100 rounded-[2.5rem] space-y-6">
                      <div className="grid grid-cols-2 gap-8">
                        <div>
                          <p className="text-[10px] font-black text-slate-400 uppercase tracking-tight mb-2">클레임 분류</p>
                          <p className="text-sm font-black text-slate-800">단순변심 (구매자 부담 반품)</p>
                        </div>
                        <div>
                          <p className="text-[10px] font-black text-slate-400 uppercase tracking-tight mb-2">상세 사유</p>
                          <p className="text-sm font-medium text-slate-600 leading-relaxed">
                            "색상이 생각보다 밝아서 제가 가지고 있는 옷들과 매치가 잘 안 되네요. 다른 상품으로 재구매하겠습니다."
                          </p>
                        </div>
                      </div>
                      <div className="pt-6 border-t border-slate-200">
                        <p className="text-[10px] font-black text-slate-400 uppercase tracking-tight mb-4">증빙 이미지 (2)</p>
                        <div className="flex gap-4">
                          {[1, 2].map(i => (
                            <div key={i} className="w-24 h-24 bg-white border border-slate-200 rounded-2xl flex items-center justify-center text-slate-300 hover:border-indigo-400 transition-all cursor-zoom-in group relative overflow-hidden">
                              <Camera size={24} className="group-hover:scale-110 transition-transform" />
                              <div className="absolute inset-0 bg-slate-900/0 group-hover:bg-slate-900/10 transition-colors"></div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Claim Item */}
                  <div className="space-y-4">
                    <h4 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2 ml-2">
                      <Box size={14} className="text-indigo-600" /> 반품 대상 상품 정보
                    </h4>
                    <div className="p-6 bg-white border border-slate-200 rounded-[2rem] flex items-center gap-6 group hover:border-indigo-300 transition-all">
                      <div className="w-20 h-20 bg-slate-50 rounded-2xl flex items-center justify-center text-4xl shadow-inner group-hover:scale-105 transition-transform">🧥</div>
                      <div className="flex-1">
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="text-sm font-black text-slate-900">울트라 경량 퀼팅 자켓</p>
                            <p className="text-xs text-slate-400 font-bold mt-1 uppercase tracking-tighter">SKU: OUT-00124 | Black / L</p>
                          </div>
                          <div className="text-right">
                            <p className="text-sm font-black text-slate-900">₩ 89,000</p>
                            <p className="text-[10px] text-slate-400 font-bold">1개</p>
                          </div>
                        </div>
                        <div className="mt-4 flex gap-2">
                          <span className="px-2 py-1 bg-emerald-50 text-emerald-600 rounded-lg text-[10px] font-black uppercase tracking-widest border border-emerald-100">입고 검수 완료 가능</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {activeSubTab === 'logistics' && (
                <div className="space-y-8 animate-in fade-in slide-in-from-right-4">
                   <div className="bg-slate-900 rounded-[3rem] p-10 text-white relative overflow-hidden">
                      <div className="absolute right-0 top-0 p-10 opacity-20"><Truck size={120} /></div>
                      <div className="relative z-10">
                        <h3 className="text-[10px] font-black text-indigo-400 uppercase tracking-[0.2em] mb-8">회수 택배 현황 (CJ Logistics)</h3>
                        <div className="flex justify-between items-center px-10">
                          {[
                            { label: '회수지시', active: true },
                            { label: '수거중', active: true },
                            { label: '센터입고', active: false },
                            { label: '검수완료', active: false },
                          ].map((step, i) => (
                            <div key={i} className="flex flex-col items-center gap-3 relative flex-1">
                               <div className={`w-4 h-4 rounded-full border-4 border-slate-800 z-10 ${step.active ? 'bg-indigo-400 ring-4 ring-indigo-400/20' : 'bg-slate-700'}`}></div>
                               <span className={`text-[11px] font-black uppercase tracking-widest ${step.active ? 'text-white' : 'text-slate-500'}`}>{step.label}</span>
                               {i < 3 && <div className={`absolute top-2 left-1/2 w-full h-0.5 -z-0 ${step.active ? 'bg-indigo-400' : 'bg-slate-700'}`}></div>}
                            </div>
                          ))}
                        </div>
                        <div className="mt-12 p-6 bg-white/5 rounded-2xl border border-white/10 flex items-center justify-between">
                           <div className="flex items-center gap-4">
                              <p className="text-xs text-slate-400 font-bold">회수 송장번호</p>
                              <p className="text-sm font-black text-white hover:text-indigo-400 cursor-pointer underline decoration-indigo-400/50">6542-1234-9001</p>
                           </div>
                           <button className="px-4 py-2 bg-indigo-600 text-white text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-indigo-700 transition-all">상세 조회</button>
                        </div>
                      </div>
                   </div>
                   
                   <div className="space-y-4">
                      <h4 className="text-[11px] font-black text-slate-400 uppercase tracking-widest ml-2">회수지 정보</h4>
                      <div className="p-8 bg-white border border-slate-200 rounded-[2.5rem] flex items-center gap-10">
                         <div className="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center text-slate-400"><MapPin size={32}/></div>
                         <div className="flex-1">
                            <p className="text-sm font-black text-slate-900">김민지 <span className="text-xs text-slate-400 font-medium ml-2">010-1234-5678</span></p>
                            <p className="text-xs text-slate-500 mt-2 font-medium leading-relaxed">서울특별시 강남구 테헤란로 123, 10층 (역삼동, OMS 타워)</p>
                         </div>
                         <button className="px-6 py-3 border border-slate-200 rounded-xl text-xs font-black hover:bg-slate-50">배송지 수정</button>
                      </div>
                   </div>
                </div>
              )}

              {activeSubTab === 'refund' && (
                <div className="space-y-8 animate-in fade-in slide-in-from-right-4">
                  <div className="bg-slate-50 p-10 rounded-[3rem] border border-slate-100 space-y-10">
                    <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] flex items-center gap-2">
                      <CreditCard size={14} className="text-emerald-500" /> 환불 정산 내역 계산기
                    </h3>
                    
                    <div className="space-y-6">
                      <div className="flex justify-between items-center pb-4 border-b border-slate-200">
                        <span className="text-sm font-bold text-slate-600">최초 결제 금액</span>
                        <span className="text-sm font-black text-slate-900">₩ 89,000</span>
                      </div>
                      <div className="flex justify-between items-center pb-4 border-b border-slate-200">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-bold text-slate-600">반품 배송비 (구매자 부담)</span>
                          <Info size={14} className="text-slate-300" />
                        </div>
                        <span className="text-sm font-black text-rose-600">- ₩ 6,000</span>
                      </div>
                      <div className="flex justify-between items-center pb-4 border-b border-slate-200">
                        <span className="text-sm font-bold text-slate-600">사용한 쿠폰 혜택 회수</span>
                        <span className="text-sm font-black text-rose-600">- ₩ 0</span>
                      </div>
                      <div className="flex justify-between items-center pt-6">
                        <span className="text-lg font-black text-slate-900">최종 환불 예정액</span>
                        <div className="text-right">
                          <p className="text-3xl font-black text-emerald-600">₩ 83,000</p>
                          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-1">Payment Method: 현대카드 (신용)</p>
                        </div>
                      </div>
                    </div>

                    <div className="p-6 bg-emerald-50 border border-emerald-100 rounded-[2rem] flex items-start gap-4">
                      <CheckCircle2 size={24} className="text-emerald-500 shrink-0" />
                      <div>
                        <p className="text-sm font-black text-emerald-900">자동 환불 시스템 대기 중</p>
                        <p className="text-xs text-emerald-700/80 leading-relaxed mt-1">
                          반품 승인 처리 시 연동된 PG사를 통해 승인 취소가 즉시 요청됩니다. 체크카드의 경우 환불 완료까지 약 3~5일이 소요될 수 있습니다.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Right Action Column */}
            <div className="lg:col-span-4 space-y-8">
              {/* Process Card */}
              <div className="bg-white border border-slate-200 rounded-[2.5rem] p-8 shadow-sm space-y-8">
                <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                  <ShieldCheck size={16} className="text-indigo-600" /> Claim Action Center
                </h3>
                
                <div className="space-y-3">
                  <button onClick={() => alert("반품 승인 및 회수 지시를 내립니다.")} className="w-full py-4 bg-rose-600 text-white text-sm font-black rounded-2xl shadow-xl shadow-rose-500/20 hover:bg-rose-700 active:scale-95 transition-all flex items-center justify-center gap-3">
                    <CheckCircle size={18} /> 반품 승인 (Refund Ok)
                  </button>
                  <button onClick={() => alert("클레임을 거부하고 반송 처리를 안내합니다.")} className="w-full py-4 bg-white border border-slate-200 text-slate-400 text-sm font-black rounded-2xl hover:bg-rose-50 hover:text-rose-500 hover:border-rose-200 active:scale-95 transition-all flex items-center justify-center gap-3">
                    <XCircle size={18} /> 클레임 거부 (Reject)
                  </button>
                </div>
              </div>

              {/* Memo Section */}
              <div className="bg-slate-50 rounded-[2.5rem] p-8 border border-slate-100 space-y-6">
                <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
                  <FileText size={16} className="text-indigo-600" /> Internal Memo
                </h3>
                <div className="space-y-4 max-h-[200px] overflow-y-auto pr-2 scrollbar-hide">
                  <div className="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm relative">
                     <p className="text-xs text-slate-600 leading-relaxed font-medium">"고객님이 사진을 추가로 보내주기로 하셨습니다. 확인 전까지 보류 바랍니다."</p>
                     <p className="text-[9px] font-black text-slate-400 mt-2 uppercase">Admin Hong • 16:45</p>
                  </div>
                </div>
                <div className="relative">
                  <textarea placeholder="운영자 메모 추가..." className="w-full p-4 bg-white border border-slate-200 rounded-2xl text-xs font-bold outline-none focus:ring-4 focus:ring-indigo-500/5 min-h-[100px]"></textarea>
                  <button className="absolute bottom-3 right-3 p-2 bg-indigo-600 text-white rounded-xl shadow-lg shadow-indigo-500/20 hover:bg-indigo-700 transition-all">
                    <ArrowRight size={14} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-8 bg-slate-50 border-t border-slate-100 flex justify-between items-center relative z-20">
          <div className="flex items-center gap-4 text-slate-400">
             <ShieldCheck size={16} className="text-emerald-500" />
             <span className="text-[10px] font-black uppercase tracking-[0.2em]">Transaction Protected by SSL-256</span>
          </div>
          <button onClick={onClose} className="px-10 py-4 bg-slate-900 text-white text-sm font-black rounded-2xl shadow-xl shadow-slate-900/10 active:scale-95 transition-all">
            목록으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
};

export default ClaimDetailModal;
