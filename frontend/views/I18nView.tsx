
import React, { useState } from 'react';
import { 
  Globe2, ShieldCheck, Coins, FileText, Scale, 
  CheckCircle2, AlertCircle, Zap, ArrowRight, 
  Search, Landmark, ChevronRight, Info, AlertTriangle,
  History, Settings, BarChart3, Target, Sparkles,
  BrainCircuit, Database, Server, MapPin, Gauge,
  Clock, Map, Layers, LayoutGrid, ListFilter,
  Monitor, Calculator, LineChart, Globe, Plus
} from 'lucide-react';
import { useTranslation } from '../App';

const ReadinessCard = ({ country, score, tasks }: any) => (
  <div className="p-8 bg-white border border-slate-200 rounded-[3rem] shadow-sm hover:border-indigo-400 transition-all group">
    <div className="flex items-center justify-between mb-8">
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-2xl group-hover:scale-110 transition-transform">
          {country === 'USA' ? '🇺🇸' : country === 'Japan' ? '🇯🇵' : country === 'Vietnam' ? '🇻🇳' : '🇪🇺'}
        </div>
        <div>
          <h4 className="text-sm font-black text-slate-800">{country} 진출 준비도</h4>
          <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Market Readiness</p>
        </div>
      </div>
      <div className="text-right">
        <p className={`text-2xl font-black ${score > 80 ? 'text-emerald-500' : 'text-amber-500'}`}>{score}%</p>
      </div>
    </div>
    <div className="space-y-3">
       {tasks.map((t: any, i: number) => (
         <div key={i} className="flex items-center justify-between text-[11px] font-bold">
            <span className="text-slate-500">{t.name}</span>
            {t.done ? <CheckCircle2 size={14} className="text-emerald-500" /> : <div className="w-3 h-3 rounded-full border-2 border-slate-200"></div>}
         </div>
       ))}
    </div>
    <div className="mt-8 pt-6 border-t border-slate-50">
       <button className="w-full py-3 bg-slate-50 text-slate-500 text-[10px] font-black uppercase tracking-widest rounded-xl group-hover:bg-indigo-600 group-hover:text-white transition-all">상세 서류 검토</button>
    </div>
  </div>
);

const I18nView: React.FC = () => {
  const { t } = useTranslation();
  const [viewType, setViewType] = useState<'global' | 'operational'>('operational');
  const [simulationMode, setSimulationMode] = useState(false);
  
  return (
    <div className="space-y-10 pb-32">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-black text-slate-900 tracking-tight italic uppercase">Strategy Intelligence</h1>
          <p className="text-slate-500 font-medium mt-1">인공지능 기반의 운영 로직 최적화와 글로벌 시장 진출 데이터를 통합 관리합니다.</p>
        </div>
        <div className="flex bg-white p-2 rounded-[2.5rem] border border-slate-200 shadow-sm gap-2">
          <button 
            onClick={() => setViewType('operational')}
            className={`px-8 py-4 rounded-[1.8rem] text-xs font-black transition-all flex items-center gap-3 ${
              viewType === 'operational' ? 'bg-indigo-600 text-white shadow-2xl' : 'text-slate-400 hover:bg-slate-50'
            }`}
          >
            <BrainCircuit size={18} /> 운영 지능 (Operations)
          </button>
          <button 
            onClick={() => setViewType('global')}
            className={`px-8 py-4 rounded-[1.8rem] text-xs font-black transition-all flex items-center gap-3 ${
              viewType === 'global' ? 'bg-indigo-600 text-white shadow-2xl' : 'text-slate-400 hover:bg-slate-50'
            }`}
          >
            <Globe size={18} /> 글로벌 확장 (Global)
          </button>
        </div>
      </div>

      {viewType === 'operational' ? (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 animate-in fade-in duration-500">
          {/* AI 시뮬레이터 패널 */}
          <div className="lg:col-span-8 space-y-8">
            <div className="bg-white p-12 rounded-[4rem] border border-slate-200 shadow-sm relative overflow-hidden group">
               <div className="absolute top-0 right-0 p-12 text-indigo-50 opacity-10"><Calculator size={180} /></div>
               
               <div className="relative z-10 space-y-10">
                  <div className="flex items-center justify-between">
                    <div>
                      <h2 className="text-2xl font-black text-slate-900">AI 운영 시뮬레이터 (Sandbox)</h2>
                      <p className="text-sm text-slate-400 font-bold uppercase tracking-widest mt-1">Optimize logistics cost vs speed</p>
                    </div>
                    <div className={`px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest flex items-center gap-2 ${simulationMode ? 'bg-amber-100 text-amber-700 animate-pulse' : 'bg-indigo-50 text-indigo-600'}`}>
                      <Sparkles size={14} /> {simulationMode ? 'Simulation Running' : 'Ready to Test'}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
                     <div className="space-y-8">
                        <h4 className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] flex items-center gap-2">
                          <Settings size={14} className="text-indigo-600" /> 운영 가중치 조절
                        </h4>
                        {[
                          { label: '물류비용 절감 우선순위', val: 75 },
                          { label: '배송 리드타임 최단화', val: 40 },
                          { label: '재고 분산 및 평준화', val: 60 },
                          { label: '탄소 배출 저감 지수', val: 20 },
                        ].map((s, i) => (
                          <div key={i} className="space-y-3">
                             <div className="flex justify-between items-center text-[11px] font-black">
                                <span className="text-slate-700">{s.label}</span>
                                <span className="text-indigo-600">{s.val}%</span>
                             </div>
                             <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden cursor-pointer group-hover:bg-slate-200 transition-colors">
                                <div className="h-full bg-indigo-600 rounded-full" style={{ width: `${s.val}%` }}></div>
                             </div>
                          </div>
                        ))}
                     </div>

                     <div className="bg-slate-900 rounded-[3rem] p-8 text-white flex flex-col justify-between shadow-2xl relative overflow-hidden">
                        <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/10 to-transparent"></div>
                        <div className="relative z-10">
                           <h4 className="text-[10px] font-black text-indigo-400 uppercase tracking-widest mb-6 flex items-center gap-2">
                             <LineChart size={14} /> Expected Result
                           </h4>
                           <div className="space-y-6">
                              <div>
                                 <p className="text-[10px] text-slate-500 font-bold uppercase mb-1">운영 효율성 점수</p>
                                 <p className="text-4xl font-black text-white italic">84.2</p>
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                 <div>
                                    <p className="text-[9px] text-slate-500 font-bold uppercase">비용 절감</p>
                                    <p className="text-sm font-black text-emerald-400">₩ 1.2M / mo</p>
                                 </div>
                                 <div>
                                    <p className="text-[9px] text-slate-500 font-bold uppercase">평균 리드타임</p>
                                    <p className="text-sm font-black text-amber-400">1.8 Days</p>
                                 </div>
                              </div>
                           </div>
                        </div>
                        <button 
                          onClick={() => setSimulationMode(!simulationMode)}
                          className="w-full mt-10 py-4 bg-indigo-600 text-white rounded-2xl text-[10px] font-black uppercase tracking-widest hover:bg-indigo-500 transition-all shadow-xl shadow-indigo-900/50"
                        >
                          {simulationMode ? '시뮬레이션 중단' : '최적화 알고리즘 가동'}
                        </button>
                     </div>
                  </div>
               </div>
            </div>

            <div className="p-10 bg-white border border-slate-200 rounded-[4rem] shadow-sm">
               <h3 className="text-lg font-black text-slate-900 mb-8 flex items-center gap-3">
                 <Monitor size={20} className="text-indigo-600" /> 실시간 운영 로직 모니터링
               </h3>
               <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {[
                    { label: 'API 처리 지연', val: '124ms', status: 'GOOD' },
                    { label: '라우팅 성공률', val: '99.9%', status: 'PERFECT' },
                    { label: '수동 개입 필요', val: '3건', status: 'WARNING' },
                  ].map((m, i) => (
                    <div key={i} className="p-6 bg-slate-50 rounded-[2rem] border border-slate-100 group hover:border-indigo-200 transition-all">
                       <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2">{m.label}</p>
                       <div className="flex items-center justify-between">
                          <p className="text-xl font-black text-slate-900">{m.val}</p>
                          <span className={`text-[9px] font-black px-2 py-1 rounded-lg ${m.status === 'GOOD' ? 'bg-emerald-50 text-emerald-600' : m.status === 'WARNING' ? 'bg-rose-50 text-rose-600' : 'bg-indigo-50 text-indigo-600'}`}>
                             {m.status}
                          </span>
                       </div>
                    </div>
                  ))}
               </div>
            </div>
          </div>

          {/* 오른쪽: 로직 히스토리 및 변경 승인 */}
          <div className="lg:col-span-4 space-y-8">
             <div className="bg-slate-950 p-10 rounded-[3.5rem] text-white shadow-2xl border border-slate-800 space-y-10">
                <h3 className="text-xs font-black text-indigo-400 uppercase tracking-[0.2em] flex items-center gap-2">
                  <History size={16} /> Strategy Change Log
                </h3>
                <div className="space-y-6">
                   {[
                     { user: 'Admin Hong', action: '수도권 라우팅 가중치 변경', time: '1시간 전' },
                     { user: 'System AI', action: '김포 센터 부하로 인한 우회 로직 가동', time: '3시간 전' },
                     { user: 'Manager Kim', action: '신규 아마존 전용 로직 배포', time: '어제' },
                   ].map((log, i) => (
                     <div key={i} className="flex gap-4 group cursor-pointer">
                        <div className="w-1 h-10 bg-indigo-500/30 rounded-full group-hover:bg-indigo-400 transition-colors"></div>
                        <div>
                           <p className="text-xs font-black text-white">{log.action}</p>
                           <p className="text-[10px] text-slate-500 font-bold uppercase mt-1">{log.user} • {log.time}</p>
                        </div>
                     </div>
                   ))}
                </div>
                <button className="w-full py-4 bg-white/5 border border-white/10 rounded-2xl text-[10px] font-black uppercase tracking-widest hover:bg-white/10 transition-all">전체 로그 감사</button>
             </div>

             <div className="p-10 bg-indigo-600 rounded-[3.5rem] text-white shadow-2xl shadow-indigo-200 flex flex-col gap-6 group">
                <Zap size={32} className="text-indigo-200 group-hover:scale-110 transition-transform" />
                <h4 className="text-xl font-black leading-tight italic">전략 저장 시 모든 하위 시스템에 즉시 동기화됩니다.</h4>
                <p className="text-xs text-indigo-100/70 font-medium leading-relaxed">
                  변경된 라우팅 및 가격 정책은 실시간으로 API를 통해 판매 채널 및 WMS로 전파됩니다. 신중하게 승인하십시오.
                </p>
                <button className="mt-4 w-full py-4 bg-white text-indigo-600 rounded-2xl text-[10px] font-black uppercase tracking-widest shadow-xl active:scale-95 transition-all">전략 최종 승인 및 배포</button>
             </div>
          </div>
        </div>
      ) : (
        <div className="space-y-10 animate-in fade-in duration-500">
           {/* 글로벌 진출 현황 섹션 */}
           <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
              <ReadinessCard country="USA" score={95} tasks={[{name: 'HS Code 매핑', done: true}, {name: '미국법인 Tax ID', done: true}, {name: '물류 노드 연동', done: true}]} />
              <ReadinessCard country="Japan" score={42} tasks={[{name: 'HS Code 매핑', done: true}, {name: '인증 서류(PSE)', done: false}, {name: '현지 CS 구축', done: false}]} />
              <ReadinessCard country="Vietnam" score={15} tasks={[{name: '상표권 등록', done: false}, {name: '수입 라이선스', done: false}, {name: '현지 통화 결제', done: true}]} />
              <div className="p-8 bg-slate-50 border-2 border-dashed border-slate-200 rounded-[3rem] flex flex-col items-center justify-center text-center group cursor-pointer hover:bg-white hover:border-indigo-400 transition-all">
                 <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-slate-300 mb-6 group-hover:scale-110 group-hover:text-indigo-600 transition-all shadow-sm">
                   <Plus size={32} />
                 </div>
                 <h4 className="text-sm font-black text-slate-400 group-hover:text-indigo-600">신규 국가 추가</h4>
                 <p className="text-[10px] text-slate-300 font-bold uppercase mt-1">Add New Global Market</p>
              </div>
           </div>

           {/* 환율 및 금융 리스크 */}
           <div className="bg-white p-12 rounded-[4rem] border border-slate-200 shadow-sm flex flex-col lg:flex-row gap-12">
              <div className="flex-1 space-y-8">
                 <h3 className="text-lg font-black text-slate-900 flex items-center gap-3">
                   <Coins size={24} className="text-amber-500" /> 실시간 글로벌 금융 리스크
                 </h3>
                 <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {[
                      { pair: 'USD / KRW', rate: '1,342.5', trend: 'UP', change: '+2.4' },
                      { pair: 'JPY / KRW', rate: '9.24', trend: 'DOWN', change: '-1.2' },
                      { pair: 'VND / KRW', rate: '0.054', trend: 'FLAT', change: '0.0' },
                    ].map((fx, i) => (
                      <div key={i} className="p-6 bg-slate-50 rounded-[2rem] border border-slate-100 flex items-center justify-between">
                         <div>
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">{fx.pair}</p>
                            <p className="text-xl font-black text-slate-900">{fx.rate}</p>
                         </div>
                         <div className={`text-right ${fx.trend === 'UP' ? 'text-rose-500' : fx.trend === 'DOWN' ? 'text-emerald-500' : 'text-slate-400'}`}>
                            <p className="text-xs font-black">{fx.change}%</p>
                            <ArrowRight size={14} className={fx.trend === 'UP' ? '-rotate-45' : fx.trend === 'DOWN' ? 'rotate-45' : ''} />
                         </div>
                      </div>
                    ))}
                 </div>
              </div>
              <div className="w-full lg:w-80 p-8 bg-amber-50 rounded-[3rem] border border-amber-100 flex flex-col justify-between">
                 <div>
                    <div className="flex items-center gap-2 text-amber-600 mb-4">
                       <AlertTriangle size={20} />
                       <span className="text-xs font-black uppercase tracking-widest">Pricing Alert</span>
                    </div>
                    <p className="text-sm font-black text-amber-900 leading-tight italic">
                      "달러 강세로 인해 미국 시장의 마진이 4.2% 감소했습니다. MSRP 상향 조정을 검토하십시오."
                    </p>
                 </div>
                 <button className="w-full py-4 bg-amber-600 text-white rounded-2xl text-[10px] font-black uppercase tracking-widest shadow-xl shadow-amber-200 hover:bg-amber-700 transition-all">가격 일괄 업데이트</button>
              </div>
           </div>
        </div>
      )}
    </div>
  );
};

export default I18nView;
