
import React, { useState, useMemo } from 'react';
import { 
  GitMerge, Server, ShoppingCart, ArrowRight, 
  ChevronRight, Info, ShieldCheck, AlertCircle, 
  Settings2, Plus, Trash2, Layers, Search,
  Database, Zap, Globe, LayoutGrid, CheckCircle2,
  ArrowUpRight, Link as LinkIcon, Activity,
  Filter, ShieldAlert, Cpu, BarChart3, 
  Globe2, MousePointer2, Settings, Save, X, 
  Loader2, Package, Check, ToggleRight, RotateCcw,
  ZapOff, MapPin
} from 'lucide-react';
import { useGlobalData } from '../App';

interface MappingRule {
  nodeId: string;
  role: 'PRIMARY' | 'BACKUP' | 'REGIONAL';
  priority: number;
}

const MappingView: React.FC = () => {
  const { channels, warehouses } = useGlobalData();
  const [selectedChannelId, setSelectedChannelId] = useState<string | null>('CH-001');
  const [isSaving, setIsSaving] = useState(false);
  const [isSmartRouting, setIsSmartRouting] = useState(true);
  
  const [mappings, setMappings] = useState<Record<string, MappingRule[]>>({
    'CH-001': [
      { nodeId: 'WH-001', role: 'PRIMARY', priority: 1 },
      { nodeId: 'WH-002', role: 'BACKUP', priority: 2 }
    ],
    'CH-002': [
      { nodeId: 'WH-002', role: 'PRIMARY', priority: 1 }
    ]
  });

  const currentRules = useMemo(() => mappings[selectedChannelId || ''] || [], [mappings, selectedChannelId]);
  const selectedChannel = useMemo(() => channels.find(c => c.id === selectedChannelId), [channels, selectedChannelId]);

  const toggleNode = (nodeId: string) => {
    if (!selectedChannelId) return;
    const rules = [...currentRules];
    const index = rules.findIndex(r => r.nodeId === nodeId);
    if (index > -1) {
      rules.splice(index, 1);
    } else {
      rules.push({ nodeId, role: 'BACKUP', priority: rules.length + 1 });
    }
    setMappings({ ...mappings, [selectedChannelId]: rules });
  };

  const updateRole = (nodeId: string, role: 'PRIMARY' | 'BACKUP' | 'REGIONAL') => {
    if (!selectedChannelId) return;
    const rules = currentRules.map(r => r.nodeId === nodeId ? { ...r, role } : r);
    setMappings({ ...mappings, [selectedChannelId]: rules });
  };

  const handleSaveMesh = async () => {
    setIsSaving(true);
    await new Promise(resolve => setTimeout(resolve, 1500));
    setIsSaving(false);
    alert("물류 라우팅 아키텍처가 전 세계 노드에 동기화되었습니다.");
  };

  return (
    <div className="space-y-8 pb-32">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <h1 className="text-4xl font-black text-slate-900 tracking-tight italic uppercase">Logistics Mesh</h1>
            <div className="px-3 py-1 bg-indigo-600 text-white rounded-full text-[10px] font-black uppercase tracking-widest animate-pulse">Live Topology</div>
          </div>
          <p className="text-slate-500 font-medium italic">채널과 물리 거점 사이의 라우팅 레이어를 지능적으로 설계합니다.</p>
        </div>
        <div className="flex gap-3">
          <button className="flex items-center gap-2 px-6 py-3 bg-white border border-slate-200 rounded-2xl text-xs font-black text-slate-600 hover:bg-slate-50 transition-all shadow-sm">
            <RotateCcw size={16} /> 구성 초기화
          </button>
          <button 
            onClick={handleSaveMesh}
            disabled={isSaving || !selectedChannelId}
            className={`flex items-center gap-3 px-8 py-3 rounded-2xl font-black text-sm transition-all active:scale-95 shadow-xl ${
              isSaving ? 'bg-slate-100 text-slate-400' : 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-indigo-200'
            }`}
          >
            {isSaving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
            변경사항 배포
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        {/* Left: Channels Navigation */}
        <div className="lg:col-span-3 space-y-6">
          <div className="bg-white p-8 rounded-[3rem] border border-slate-200 shadow-sm space-y-8 h-fit">
            <div>
              <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-widest flex items-center gap-2 mb-1">
                <ShoppingCart size={14} className="text-indigo-600" /> Sales Channels
              </h3>
              <p className="text-[10px] text-slate-300 font-bold uppercase">Select source to map</p>
            </div>
            <div className="space-y-2">
              {channels.map(ch => (
                <button 
                  key={ch.id}
                  onClick={() => setSelectedChannelId(ch.id)}
                  className={`w-full p-5 rounded-[2rem] border transition-all text-left flex items-center justify-between group relative ${
                    selectedChannelId === ch.id ? 'border-indigo-600 bg-indigo-600 text-white shadow-xl' : 'border-slate-100 hover:border-indigo-100 bg-slate-50'
                  }`}
                >
                  <div className="flex items-center gap-3 relative z-10 min-w-0">
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-lg shrink-0 ${selectedChannelId === ch.id ? 'bg-white/20' : 'bg-white shadow-sm'}`}>
                      {ch.logo}
                    </div>
                    <div className="min-w-0">
                      <p className="text-xs font-black truncate">{ch.name}</p>
                      <span className={`text-[8px] font-bold uppercase tracking-widest ${selectedChannelId === ch.id ? 'text-indigo-200' : 'text-slate-400'}`}>{ch.id}</span>
                    </div>
                  </div>
                  <ChevronRight size={16} className={selectedChannelId === ch.id ? 'text-white' : 'text-slate-200 group-hover:text-indigo-400 transition-all'} />
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Right: Dynamic Mesh Workspace */}
        <div className="lg:col-span-9 space-y-8">
          {selectedChannelId ? (
            <div className="space-y-8 animate-in fade-in zoom-in-95 duration-500">
              {/* Dynamic Topology Section */}
              <div className="bg-slate-950 rounded-[4rem] p-12 text-white relative overflow-hidden border border-slate-800 min-h-[450px] flex flex-col items-center justify-center">
                <div className="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/carbon-fibre.png')] opacity-20"></div>
                <div className="absolute inset-0 bg-gradient-to-b from-indigo-500/5 to-transparent"></div>
                
                {/* Visual Connection Lines Simulation (SVG) */}
                <svg className="absolute inset-0 w-full h-full pointer-events-none opacity-20">
                   <defs>
                      <radialGradient id="grad1" cx="50%" cy="50%" r="50%">
                         <stop offset="0%" stopColor="#6366f1" stopOpacity="1" />
                         <stop offset="100%" stopColor="#6366f1" stopOpacity="0" />
                      </radialGradient>
                   </defs>
                   {currentRules.map((r, i) => (
                      <line 
                        key={i} 
                        x1="50%" y1="50%" 
                        x2={`${20 + (i * 30)}%`} y2="80%" 
                        stroke="#6366f1" 
                        strokeWidth="1" 
                        strokeDasharray="4 4" 
                        className="animate-[dash_10s_linear_infinite]"
                      />
                   ))}
                </svg>

                <div className="relative z-10 flex flex-col items-center gap-16 w-full">
                  {/* Channel Core */}
                  <div className="flex flex-col items-center gap-4">
                    <div className="w-24 h-24 bg-indigo-600 rounded-[2.5rem] flex items-center justify-center text-4xl shadow-[0_0_50px_rgba(99,102,241,0.4)] animate-bounce-slow">
                       {selectedChannel?.logo}
                    </div>
                    <div className="text-center">
                      <h3 className="text-lg font-black italic tracking-widest uppercase">{selectedChannel?.name} CORE</h3>
                      <p className="text-[10px] text-indigo-400 font-bold uppercase tracking-[0.2em] mt-1">Primary Traffic Source</p>
                    </div>
                  </div>

                  {/* Mapped Nodes Satellites */}
                  <div className="flex flex-wrap justify-center gap-10 w-full">
                    {currentRules.length > 0 ? currentRules.map((rule) => {
                      const warehouse = warehouses.find(w => w.id === rule.nodeId);
                      return (
                        <div key={rule.nodeId} className="flex flex-col items-center gap-4 group animate-in zoom-in-75 duration-500">
                          <div className={`relative w-16 h-16 rounded-2xl flex items-center justify-center transition-all duration-500 ${
                            rule.role === 'PRIMARY' ? 'bg-emerald-500 shadow-[0_0_30px_rgba(16,185,129,0.3)]' : 
                            rule.role === 'REGIONAL' ? 'bg-amber-500 shadow-[0_0_30px_rgba(245,158,11,0.3)]' : 'bg-slate-700'
                          }`}>
                            <Server size={28} />
                            <div className="absolute -top-2 -right-2 w-6 h-6 bg-white text-slate-900 rounded-full flex items-center justify-center text-[10px] font-black shadow-lg">
                              {rule.priority}
                            </div>
                          </div>
                          <div className="text-center">
                            <p className="text-xs font-black whitespace-nowrap">{warehouse?.name}</p>
                            <span className={`text-[8px] font-black uppercase tracking-widest px-2 py-0.5 rounded-full mt-1 inline-block ${
                              rule.role === 'PRIMARY' ? 'bg-emerald-500/20 text-emerald-400' : 
                              rule.role === 'REGIONAL' ? 'bg-amber-500/20 text-amber-400' : 'bg-slate-500/20 text-slate-400'
                            }`}>
                              {rule.role}
                            </span>
                          </div>
                        </div>
                      );
                    }) : (
                      <div className="flex flex-col items-center gap-3 opacity-30 italic">
                        <ZapOff size={32} />
                        <p className="text-sm font-bold uppercase tracking-widest">No Active Nodes Connected</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Node Configuration Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {warehouses.map(wh => {
                  const rule = currentRules.find(r => r.nodeId === wh.id);
                  const isMapped = !!rule;
                  return (
                    <div key={wh.id} className={`p-8 rounded-[3rem] border transition-all duration-500 relative overflow-hidden group ${
                      isMapped ? 'bg-white border-indigo-600 shadow-2xl shadow-indigo-500/5' : 'bg-slate-50 border-slate-100 opacity-60 hover:opacity-100'
                    }`}>
                      {isMapped && <div className="absolute top-0 right-0 p-4 animate-in fade-in"><CheckCircle2 size={24} className="text-indigo-600" /></div>}
                      
                      <div className="flex items-center gap-5 mb-8">
                         <div className={`w-14 h-14 rounded-2xl flex items-center justify-center transition-all duration-500 ${isMapped ? 'bg-indigo-600 text-white shadow-xl rotate-3' : 'bg-white text-slate-300'}`}>
                            <MapPin size={28} />
                         </div>
                         <div>
                            <h4 className="text-base font-black text-slate-900 italic uppercase">{wh.name}</h4>
                            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">{wh.region} Node • {wh.type}</p>
                         </div>
                      </div>

                      <div className="space-y-6">
                        {isMapped ? (
                          <div className="space-y-4 animate-in slide-in-from-bottom-2">
                             <div className="flex items-center justify-between p-2 bg-slate-50 rounded-2xl border border-slate-100">
                                {(['PRIMARY', 'REGIONAL', 'BACKUP'] as const).map((r) => (
                                  <button
                                    key={r}
                                    onClick={() => updateRole(wh.id, r)}
                                    className={`flex-1 py-2 text-[9px] font-black uppercase tracking-tighter rounded-xl transition-all ${
                                      rule.role === r ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-400 hover:text-slate-600'
                                    }`}
                                  >
                                    {r}
                                  </button>
                                ))}
                             </div>
                             <div className="flex gap-2">
                                <button className="flex-1 py-3 bg-slate-900 text-white rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-slate-800 transition-all">라우팅 로직 설정</button>
                                <button onClick={() => toggleNode(wh.id)} className="px-4 py-3 bg-rose-50 text-rose-600 rounded-xl hover:bg-rose-100 transition-all"><Trash2 size={16}/></button>
                             </div>
                          </div>
                        ) : (
                          <button 
                            onClick={() => toggleNode(wh.id)}
                            className="w-full py-4 border-2 border-dashed border-slate-200 rounded-[2rem] text-xs font-black text-slate-400 hover:border-indigo-400 hover:text-indigo-600 hover:bg-white transition-all flex items-center justify-center gap-2 group/btn"
                          >
                            <Plus size={18} className="group-hover/btn:rotate-90 transition-transform" /> Connect to Mesh
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>

              {/* Advanced Smart Routing Toggle */}
              <div className="bg-indigo-50 border border-indigo-100 rounded-[3rem] p-8 flex items-center justify-between gap-10">
                 <div className="flex items-center gap-6">
                    <div className="w-16 h-16 bg-white rounded-3xl flex items-center justify-center text-indigo-600 shadow-xl shadow-indigo-500/10">
                       <Cpu size={32} />
                    </div>
                    <div>
                       <h4 className="text-lg font-black text-indigo-900 italic">AI Dynamic Load Balancing</h4>
                       <p className="text-sm text-indigo-600 font-medium max-w-md leading-relaxed">
                         창고별 부하와 배송지 거리를 실시간 분석하여 최적의 노드를 자동 할당합니다. <span className="font-black underline cursor-pointer">학습 로그 보기</span>
                       </p>
                    </div>
                 </div>
                 <button 
                  onClick={() => setIsSmartRouting(!isSmartRouting)}
                  className={`relative w-20 h-10 rounded-full transition-all duration-500 flex items-center px-1.5 ${isSmartRouting ? 'bg-indigo-600' : 'bg-slate-300'}`}
                 >
                    <div className={`w-7 h-7 bg-white rounded-full shadow-lg transition-all duration-500 ${isSmartRouting ? 'translate-x-10' : 'translate-x-0'}`}></div>
                 </button>
              </div>
            </div>
          ) : (
            <div className="h-[600px] bg-white border-2 border-dashed border-slate-200 rounded-[5rem] flex flex-col items-center justify-center text-center p-20 animate-pulse">
               <GitMerge size={80} className="text-slate-100 mb-8" />
               <h3 className="text-2xl font-black text-slate-300 tracking-tight uppercase">Select Gateway Channel</h3>
               <p className="text-sm text-slate-300 font-medium mt-4 max-w-sm">구성 아키텍처를 설계할 판매 채널을 좌측 사이드바에서 선택하여 메쉬 네트워크를 가동하십시오.</p>
            </div>
          )}
        </div>
      </div>

      <style dangerouslySetInnerHTML={{ __html: `
        @keyframes dash {
          to { stroke-dashoffset: -40; }
        }
        .animate-bounce-slow {
          animation: bounce 3s infinite;
        }
        @keyframes bounce {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-10px); }
        }
      `}} />
    </div>
  );
};

export default MappingView;
