
import React, { useState } from 'react';
import { 
  Link2, Activity, ShieldCheck, Wifi, Terminal, 
  RefreshCw, AlertCircle, Plus, X, Globe, Lock, 
  Check, Loader2, Zap, Server, 
  Database, ArrowUpRight, Radio, 
  Search, Filter, ChevronRight, HardDrive, Cpu,
  Play, Repeat, Bug, Code, Send, Trash2, ArrowDownCircle,
  Settings
} from 'lucide-react';
import LogDetailModal from '../components/LogDetailModal';
import { useGlobalData } from '../App';

const InterfaceView: React.FC = () => {
  const { channels: globalChannels } = useGlobalData();
  const [selectedLogId, setSelectedLogId] = useState<string | null>(null);
  const [isSyncing, setIsSyncing] = useState(false);
  const [activeView, setActiveView] = useState<'connections' | 'webhooks'>('webhooks');
  const [showSimulator, setShowSimulator] = useState(false);
  const [simulatorPayload, setSimulatorPayload] = useState('{\n  "event": "ORDER_CREATED",\n  "order_id": "MOCK-12345",\n  "amount": 45000\n}');
  const [retryingIds, setRetryingIds] = useState<string[]>([]);

  const handleGlobalSync = () => {
    setIsSyncing(true);
    setTimeout(() => setIsSyncing(false), 1500);
  };

  const handleRetry = (id: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setRetryingIds(prev => [...prev, id]);
    
    // Simulate API call
    setTimeout(() => {
      setRetryingIds(prev => prev.filter(item => item !== id));
      alert(`[${id}] 패킷 재전송이 성공적으로 요청되었습니다.`);
    }, 1500);
  };

  const handleBulkRetry = () => {
    if (confirm("실패 상태인 모든 웹훅을 재시도하시겠습니까?")) {
      setIsSyncing(true);
      setTimeout(() => {
        setIsSyncing(false);
        alert("모든 실패 패킷의 재처리가 예약되었습니다.");
      }, 2000);
    }
  };

  return (
    <div className="space-y-10 pb-32">
      {/* 1. Global System Health Banner */}
      <div className="bg-slate-900 rounded-[3rem] p-10 text-white relative overflow-hidden shadow-2xl">
         <div className="absolute top-0 right-0 p-12 opacity-10 rotate-12"><Zap size={200} /></div>
         <div className="relative z-10 flex flex-col md:flex-row justify-between gap-10">
            <div className="space-y-6">
               <div className="flex items-center gap-3">
                  <div className="px-4 py-1.5 bg-emerald-500 rounded-full text-[10px] font-black uppercase tracking-[0.2em] animate-pulse">Gateway Live</div>
                  <span className="text-slate-400 text-xs font-bold italic">NOC Debugger v5.0</span>
               </div>
               <div>
                  <h1 className="text-4xl font-black italic uppercase tracking-tight">Interface Control Center</h1>
                  <p className="text-slate-400 text-sm mt-2 max-w-md font-medium leading-relaxed">
                    실시간 데이터 패킷 수신 및 API 통신 상태를 감시합니다. 
                    장애 발생 시 <span className="text-indigo-400 font-black">Debugger</span> 탭에서 페이로드를 분석하세요.
                  </p>
               </div>
            </div>

            <div className="flex flex-col gap-3 justify-end">
               <button 
                onClick={() => setShowSimulator(true)}
                className="px-8 py-4 bg-indigo-600 text-white rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-indigo-500 transition-all shadow-xl shadow-indigo-900/50 flex items-center justify-center gap-3 active:scale-95"
               >
                 <Bug size={18} /> 웹훅 시뮬레이터 실행
               </button>
               <button 
                onClick={handleGlobalSync}
                className="px-8 py-4 bg-white/5 border border-white/10 text-white rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-white/10 transition-all flex items-center justify-center gap-3"
               >
                 {isSyncing ? <Loader2 size={18} className="animate-spin" /> : <RefreshCw size={18} />} 노드 상태 갱신
               </button>
            </div>
         </div>
      </div>

      {/* 2. Navigation Tabs */}
      <div className="flex bg-white p-2 rounded-[2rem] border border-slate-200 shadow-sm w-fit mx-auto">
         {[
           { id: 'webhooks', label: '웹훅 디버거 (Inbound)', icon: <Radio size={16} /> },
           { id: 'connections', label: '채널 커넥터 (Outbound)', icon: <Link2 size={16} /> },
         ].map(tab => (
           <button 
            key={tab.id}
            onClick={() => setActiveView(tab.id as any)}
            className={`px-8 py-3.5 rounded-full text-[11px] font-black uppercase tracking-widest transition-all flex items-center gap-3 ${
              activeView === tab.id ? 'bg-indigo-600 text-white shadow-xl shadow-indigo-100' : 'text-slate-400 hover:text-slate-600 hover:bg-slate-50'
            }`}
           >
             {tab.icon} {tab.label}
           </button>
         ))}
      </div>

      {activeView === 'webhooks' && (
        <div className="animate-in fade-in duration-500 space-y-8">
           {/* Webhook Search & Filter Bar */}
           <div className="flex flex-wrap items-center justify-between gap-4 bg-white p-4 rounded-[2.5rem] border border-slate-200 shadow-sm">
              <div className="flex-1 min-w-[300px] relative">
                 <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300" size={18} />
                 <input type="text" placeholder="Trace ID, 주문번호, 이벤트명 검색..." className="w-full pl-14 pr-4 py-3.5 bg-slate-50 border-none rounded-2xl text-sm font-bold outline-none" />
              </div>
              <div className="flex gap-2">
                 <button 
                  onClick={handleBulkRetry}
                  className="px-6 py-3 bg-rose-50 text-rose-600 rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-rose-100 transition-all border border-rose-100 flex items-center gap-2"
                 >
                   <RefreshCw size={14} /> 실패 건 전체 재시도
                 </button>
                 <div className="w-px h-8 bg-slate-100 mx-2"></div>
                 {['ALL', 'SUCCESS', 'FAILED'].map(s => (
                   <button key={s} className="px-5 py-3 rounded-xl text-[10px] font-black uppercase tracking-widest bg-slate-50 text-slate-400 hover:bg-indigo-50 hover:text-indigo-600 transition-all">
                     {s}
                   </button>
                 ))}
              </div>
           </div>

           <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
              {/* Live Webhook Feed List */}
              <div className="lg:col-span-8 bg-white rounded-[3rem] border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[600px]">
                 <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-slate-50/30">
                    <h3 className="text-sm font-black text-slate-900 flex items-center gap-3 uppercase italic">
                       <Terminal size={18} className="text-indigo-600" /> Incoming Webhook Stream
                    </h3>
                    <div className="flex items-center gap-4">
                       <span className="text-[10px] font-black text-slate-400 animate-pulse uppercase tracking-widest">Live Monitoring</span>
                       <button className="p-2 bg-white rounded-xl border border-slate-100 text-slate-400 hover:text-indigo-600 transition-all shadow-sm"><Trash2 size={16} /></button>
                    </div>
                 </div>
                 
                 <div className="overflow-x-auto">
                    <table className="w-full text-left">
                       <thead>
                          <tr className="bg-slate-50/50 border-b border-slate-100">
                             <th className="py-5 px-8 text-[10px] font-black text-slate-400 uppercase tracking-widest">Time / Status</th>
                             <th className="py-5 px-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Event Type</th>
                             <th className="py-5 px-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Source</th>
                             <th className="py-5 px-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Latency</th>
                             <th className="py-5 px-8 text-right text-[10px] font-black text-slate-400 uppercase tracking-widest">Action</th>
                          </tr>
                       </thead>
                       <tbody className="divide-y divide-slate-50">
                          {[
                            { id: 'W-901', time: '18:55:12', event: 'ORDER.PAID', status: 200, source: 'Naver Store', logo: '🟢', lat: '124ms', success: true },
                            { id: 'W-902', time: '18:54:40', event: 'ORDER.CANCELLED', status: 200, source: 'Coupang', logo: '🚀', lat: '98ms', success: true },
                            { id: 'W-903', time: '18:53:15', event: 'STOCK.UPDATED', status: 500, source: 'Amazon US', logo: '📦', lat: '2400ms', success: false },
                            { id: 'W-904', time: '18:51:02', event: 'ORDER.PAID', status: 200, source: 'Shopify', logo: '🛍️', lat: '142ms', success: true },
                            { id: 'W-905', time: '18:48:55', event: 'CLAIM.RETURNED', status: 400, source: 'Lazada', logo: '🧡', lat: '210ms', success: false },
                          ].map((log) => (
                            <tr key={log.id} onClick={() => setSelectedLogId(log.id)} className="group hover:bg-slate-50/80 cursor-pointer transition-all">
                               <td className="py-6 px-8">
                                  <div className="flex items-center gap-4">
                                     <span className="text-[11px] font-bold text-slate-400 font-mono italic">{log.time}</span>
                                     <span className={`px-2 py-1 rounded-lg text-[10px] font-black border ${log.success ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-rose-50 text-rose-600 border-rose-100'}`}>
                                        HTTP {log.status}
                                     </span>
                                  </div>
                               </td>
                               <td className="py-6 px-6">
                                  <span className="text-xs font-black text-slate-800 tracking-tight italic uppercase">{log.event}</span>
                               </td>
                               <td className="py-6 px-6">
                                  <div className="flex items-center gap-2">
                                     <span className="text-lg">{log.logo}</span>
                                     <span className="text-xs font-bold text-slate-600">{log.source}</span>
                                  </div>
                               </td>
                               <td className="py-6 px-6">
                                  <span className="text-[11px] font-bold text-slate-400">{log.lat}</span>
                               </td>
                               <td className="py-6 px-8 text-right">
                                  <div className="flex items-center justify-end gap-2">
                                     <button 
                                      onClick={(e) => handleRetry(log.id, e)} 
                                      disabled={retryingIds.includes(log.id)}
                                      className={`px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all flex items-center gap-2 ${
                                        log.success 
                                          ? 'bg-slate-100 text-slate-400 hover:bg-indigo-50 hover:text-indigo-600' 
                                          : 'bg-indigo-600 text-white shadow-lg shadow-indigo-100 hover:bg-indigo-700'
                                      } ${retryingIds.includes(log.id) ? 'opacity-50' : ''}`}
                                     >
                                        {retryingIds.includes(log.id) ? (
                                          <Loader2 size={12} className="animate-spin" />
                                        ) : (
                                          <RefreshCw size={12} />
                                        )}
                                        Retry
                                     </button>
                                     <ChevronRight size={16} className="text-slate-300 group-hover:translate-x-1 transition-transform" />
                                  </div>
                               </td>
                            </tr>
                          ))}
                       </tbody>
                    </table>
                 </div>
                 <div className="mt-auto p-6 bg-slate-50/50 border-t border-slate-100 flex justify-center">
                    <button className="text-xs font-black text-slate-400 hover:text-indigo-600 uppercase tracking-widest">Show More Debug Logs</button>
                 </div>
              </div>

              {/* Inspector Preview Panel */}
              <div className="lg:col-span-4 space-y-6">
                 <div className="bg-slate-900 rounded-[3rem] p-8 text-white shadow-2xl space-y-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-8 opacity-[0.03]"><ArrowDownCircle size={120} /></div>
                    <div className="relative z-10">
                       <h3 className="text-xs font-black text-indigo-400 uppercase tracking-[0.2em] mb-8 flex items-center gap-2">
                          <Activity size={16} /> Webhook Reliability
                       </h3>
                       <div className="space-y-6">
                          <div>
                             <p className="text-[10px] text-slate-500 font-bold uppercase mb-2">Success Rate (24h)</p>
                             <div className="flex items-end gap-3">
                                <span className="text-4xl font-black italic">99.8%</span>
                                <span className="text-emerald-400 text-xs font-bold pb-1 flex items-center gap-1"><ArrowUpRight size={14}/> +0.2%</span>
                             </div>
                          </div>
                          <div className="grid grid-cols-2 gap-4">
                             <div className="p-4 bg-white/5 border border-white/10 rounded-2xl">
                                <p className="text-[9px] text-slate-500 font-bold uppercase">Total Inbound</p>
                                <p className="text-lg font-black">12,402</p>
                             </div>
                             <div className="p-4 bg-white/5 border border-white/10 rounded-2xl">
                                <p className="text-[9px] text-slate-500 font-bold uppercase">Processing Errors</p>
                                <p className="text-lg font-black text-rose-400">24</p>
                             </div>
                          </div>
                       </div>
                    </div>
                 </div>

                 <div className="bg-white p-8 rounded-[3rem] border border-slate-200 shadow-sm space-y-6">
                    <h3 className="text-sm font-black text-slate-900 uppercase italic flex items-center gap-2">
                       <ShieldCheck size={18} className="text-emerald-500" /> Security Status
                    </h3>
                    <div className="space-y-4">
                       {[
                         { label: 'Signature Validation', status: 'Enabled', color: 'text-emerald-600 bg-emerald-50' },
                         { label: 'SSL/TLS Cipher', status: 'v1.3 Strong', color: 'text-indigo-600 bg-indigo-50' },
                         { label: 'IP Whitelist', status: '8 Entries', color: 'text-slate-600 bg-slate-50' },
                       ].map((s, i) => (
                         <div key={i} className="flex justify-between items-center">
                            <span className="text-[11px] font-bold text-slate-400">{s.label}</span>
                            <span className={`px-2 py-1 rounded-lg text-[10px] font-black uppercase ${s.color}`}>{s.status}</span>
                         </div>
                       ))}
                    </div>
                    <button className="w-full py-4 bg-slate-900 text-white rounded-2xl text-[10px] font-black uppercase tracking-[0.2em] shadow-xl hover:bg-slate-800 active:scale-95 transition-all mt-4">Audit Endpoints</button>
                 </div>
              </div>
           </div>
        </div>
      )}

      {activeView === 'connections' && (
        <div className="animate-in fade-in duration-500 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {globalChannels.map((item) => (
            <div key={item.id} className="bg-white p-8 rounded-[3rem] border border-slate-200 shadow-sm group hover:border-indigo-400 hover:shadow-2xl transition-all flex flex-col justify-between h-[300px] relative overflow-hidden">
               <div className="absolute top-0 right-0 p-8 opacity-[0.02] -mr-6 -mt-6 group-hover:scale-110 transition-transform"><Database size={140} /></div>
               <div className="relative z-10">
                  <div className="flex items-center justify-between mb-8">
                     <div className="w-14 h-14 bg-slate-50 rounded-2xl flex items-center justify-center text-3xl shadow-inner group-hover:scale-110 transition-transform">
                        {item.logo}
                     </div>
                     <div className="text-right">
                        <div className={`flex items-center justify-end gap-2 mb-1`}>
                           <div className={`w-2 h-2 rounded-full ${item.status === 'CONNECTED' ? 'bg-emerald-500 animate-pulse' : 'bg-rose-500'}`}></div>
                           <span className={`text-[10px] font-black uppercase tracking-widest ${item.status === 'CONNECTED' ? 'text-emerald-500' : 'text-rose-500'}`}>{item.status}</span>
                        </div>
                        <p className="text-[10px] text-slate-400 font-bold uppercase italic">Sync: {item.lastSync}</p>
                     </div>
                  </div>
                  <h3 className="text-xl font-black text-slate-900 group-hover:text-indigo-600 transition-colors">{item.name}</h3>
                  <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mt-1">API Node: {item.id}</p>
               </div>
               <div className="relative z-10 pt-6 mt-6 border-t border-slate-50 flex gap-2">
                  <button className="flex-1 py-3 bg-slate-50 hover:bg-indigo-50 text-slate-500 hover:text-indigo-600 rounded-xl text-[10px] font-black uppercase tracking-widest border border-slate-100 hover:border-indigo-100 transition-all flex items-center justify-center gap-2">
                     <Settings size={12} /> 설정
                  </button>
                  <button className="flex-1 py-3 bg-indigo-600 text-white rounded-xl text-[10px] font-black uppercase tracking-widest shadow-xl shadow-indigo-100 hover:bg-indigo-700 active:scale-95 transition-all">
                     데이터 갱신
                  </button>
               </div>
            </div>
          ))}
        </div>
      )}

      {/* Simulator Modal */}
      {showSimulator && (
        <div className="fixed inset-0 z-[250] flex items-center justify-center p-4 animate-in fade-in duration-200">
           <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={() => setShowSimulator(false)}></div>
           <div className="relative bg-white w-full max-w-2xl rounded-[3rem] shadow-2xl overflow-hidden flex flex-col animate-in zoom-in-95 border border-slate-100">
              <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
                 <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center text-white shadow-xl shadow-indigo-200">
                       <Code size={24} />
                    </div>
                    <div>
                       <h2 className="text-xl font-black text-slate-900 italic uppercase">Webhook Simulator</h2>
                       <p className="text-xs text-slate-400 font-bold uppercase mt-1">시스템 응답을 가상 테스트합니다</p>
                    </div>
                 </div>
                 <button onClick={() => setShowSimulator(false)} className="p-2 hover:bg-slate-200 rounded-full text-slate-400 transition-colors"><X size={24}/></button>
              </div>
              <div className="p-10 space-y-6">
                 <div className="space-y-3">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Target Endpoint / Event</label>
                    <select className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold outline-none focus:ring-4 focus:ring-indigo-500/5">
                       <option>ORDER.CREATED (주문 생성 수신)</option>
                       <option>STOCK.CHANGED (재고 변동 동기화)</option>
                       <option>CLAIM.REQUESTED (클레임 접수 알림)</option>
                    </select>
                 </div>
                 <div className="space-y-3">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">JSON Payload Editor</label>
                    <textarea 
                      value={simulatorPayload}
                      onChange={(e) => setSimulatorPayload(e.target.value)}
                      className="w-full h-48 p-6 bg-[#0F172A] text-emerald-400 font-mono text-xs rounded-[2rem] border border-white/5 outline-none focus:ring-4 focus:ring-indigo-500/10 shadow-inner"
                    ></textarea>
                 </div>
              </div>
              <div className="p-8 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
                 <button onClick={() => setShowSimulator(false)} className="px-8 py-3 bg-white border border-slate-200 text-slate-500 text-xs font-black rounded-xl uppercase tracking-widest">취소</button>
                 <button onClick={() => { alert('웹훅 패킷이 가상 큐(Queue)로 발송되었습니다.'); setShowSimulator(false); }} className="px-10 py-3 bg-indigo-600 text-white rounded-xl text-xs font-black uppercase tracking-widest shadow-xl shadow-indigo-500/20 active:scale-95 transition-all flex items-center gap-2">
                    <Send size={14} /> 가상 패킷 발송
                 </button>
              </div>
           </div>
        </div>
      )}

      <LogDetailModal 
        isOpen={!!selectedLogId} 
        onClose={() => setSelectedLogId(null)} 
        logId={selectedLogId} 
      />
    </div>
  );
};

export default InterfaceView;
