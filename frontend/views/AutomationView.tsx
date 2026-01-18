
import React, { useState } from 'react';
import { 
  Zap, Plus, Play, Pause, Trash2, Settings2, 
  ArrowRight, ShieldCheck, AlertCircle, X, 
  Save, CheckCircle2, ChevronRight, MessageSquare, 
  Settings, Terminal, Database, Bell, Check
} from 'lucide-react';

const INITIAL_RULES = [
  { id: 'RULE-001', name: 'VIP 주문 자동 배정', desc: '100만원 이상 주문 건 VIP 전담팀 자동 할당', status: 'active', trigger: '신규 주문 발생', action: '담당자 변경', lastRun: '12분 전' },
  { id: 'RULE-002', name: '품절 위험 알림', desc: '재고 10개 미만 시 슬랙 채널로 즉시 알림', status: 'active', trigger: '재고 수량 변경', action: 'Slack 메시지 전송', lastRun: '1시간 전' },
  { id: 'RULE-003', name: '장기 미처리 주문 경고', desc: '상품준비중 단계에서 48시간 정체 시 관리자 메일 발송', status: 'paused', trigger: '시간 경과', action: '이메일 발송', lastRun: '어제' },
];

const AutomationView: React.FC = () => {
  const [rules, setRules] = useState(INITIAL_RULES);
  const [showNewRuleModal, setShowNewRuleModal] = useState(false);
  const [wizardStep, setWizardStep] = useState(1);
  const [newRule, setNewRule] = useState({ name: '', trigger: '', action: '' });

  const toggleStatus = (id: string) => {
    setRules(prev => prev.map(rule => 
      rule.id === id ? { ...rule, status: rule.status === 'active' ? 'paused' : 'active' } : rule
    ));
  };

  const deleteRule = (id: string) => {
    if (confirm("이 자동화 규칙을 영구적으로 삭제하시겠습니까?")) {
      setRules(prev => prev.filter(r => r.id !== id));
    }
  };

  const handleCreateRule = () => {
    const rule = {
      id: `RULE-00${rules.length + 1}`,
      name: newRule.name || '새 자동화 규칙',
      desc: '사용자 정의 규칙',
      status: 'active',
      trigger: newRule.trigger || '수동 실행',
      action: newRule.action || '알림 발송',
      lastRun: '기록 없음'
    };
    setRules([...rules, rule]);
    setShowNewRuleModal(false);
    setWizardStep(1);
    setNewRule({ name: '', trigger: '', action: '' });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-black text-slate-900 tracking-tight">자동화 엔진</h1>
          <p className="text-slate-500 text-sm font-medium mt-1">반복적인 업무를 트리거 기반의 자동화 규칙으로 최적화합니다.</p>
        </div>
        <button 
          onClick={() => setShowNewRuleModal(true)}
          className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-2xl text-sm font-black hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-500/20 active:scale-95"
        >
          <Plus size={20} /> 신규 규칙 생성
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          { label: '활성 규칙', value: rules.filter(r => r.status === 'active').length, icon: <Play size={20} />, color: 'bg-emerald-50 text-emerald-600' },
          { label: '오늘 처리된 자동화', value: '1,242건', icon: <Zap size={20} />, color: 'bg-indigo-50 text-indigo-600' },
          { label: '최근 오류', value: '0건', icon: <AlertCircle size={20} />, color: 'bg-slate-50 text-slate-400' },
        ].map((stat, i) => (
          <div key={i} className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm flex items-center justify-between">
            <div>
              <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">{stat.label}</p>
              <p className="text-2xl font-black text-slate-900">{stat.value}</p>
            </div>
            <div className={`w-12 h-12 rounded-2xl flex items-center justify-center ${stat.color}`}>
              {stat.icon}
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-[2.5rem] border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-slate-50 border-b border-slate-100">
              <tr>
                <th className="py-5 px-8 text-[11px] font-black text-slate-400 uppercase tracking-widest">Rule Name & Desc</th>
                <th className="py-5 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest">Trigger / Action</th>
                <th className="py-5 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest">Last Run</th>
                <th className="py-5 px-6 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">Status</th>
                <th className="py-5 px-8 w-20"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {rules.map((rule) => (
                <tr key={rule.id} className={`hover:bg-slate-50/50 transition-all ${rule.status === 'paused' ? 'opacity-60' : ''}`}>
                  <td className="py-6 px-8">
                    <div className="flex items-center gap-4">
                      <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${rule.status === 'active' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200' : 'bg-slate-200 text-slate-400'}`}>
                        <Zap size={18} />
                      </div>
                      <div>
                        <p className="text-sm font-black text-slate-900">{rule.name}</p>
                        <p className="text-xs text-slate-500 mt-0.5">{rule.desc}</p>
                      </div>
                    </div>
                  </td>
                  <td className="py-6 px-6">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-black px-2 py-1 bg-slate-100 rounded-lg text-slate-500">{rule.trigger}</span>
                      <ArrowRight size={14} className="text-slate-300" />
                      <span className="text-[10px] font-black px-2 py-1 bg-blue-50 rounded-lg text-blue-600">{rule.action}</span>
                    </div>
                  </td>
                  <td className="py-6 px-6 text-xs font-bold text-slate-400">{rule.lastRun}</td>
                  <td className="py-6 px-6 text-center">
                    <button 
                      onClick={() => toggleStatus(rule.id)}
                      className={`inline-flex items-center gap-2 px-3 py-1.5 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all ${
                        rule.status === 'active' 
                          ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' 
                          : 'bg-slate-100 text-slate-400 border border-slate-200'
                      }`}
                    >
                      {rule.status === 'active' ? <Play size={10} fill="currentColor" /> : <Pause size={10} fill="currentColor" />}
                      {rule.status === 'active' ? 'Running' : 'Paused'}
                    </button>
                  </td>
                  <td className="py-6 px-8 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button className="p-2 hover:bg-slate-100 rounded-xl text-slate-400 hover:text-slate-900 transition-all"><Settings2 size={18} /></button>
                      <button onClick={() => deleteRule(rule.id)} className="p-2 hover:bg-rose-50 rounded-xl text-slate-400 hover:text-rose-600 transition-all"><Trash2 size={18} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* New Rule Wizard Modal */}
      {showNewRuleModal && (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-md" onClick={() => setShowNewRuleModal(false)}></div>
          <div className="relative bg-white w-full max-w-xl rounded-[2.5rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">신규 규칙 마법사</h2>
              <button onClick={() => setShowNewRuleModal(false)} className="p-2 hover:bg-slate-100 rounded-full text-slate-400"><X size={24} /></button>
            </div>
            
            <div className="p-8">
              <div className="flex items-center justify-between mb-8">
                {[1, 2, 3].map(step => (
                  <div key={step} className="flex flex-col items-center gap-2 relative flex-1">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold z-10 transition-all ${wizardStep >= step ? 'bg-indigo-600 text-white' : 'bg-slate-100 text-slate-300'}`}>
                      {wizardStep > step ? <Check size={18} /> : step}
                    </div>
                    {step < 3 && <div className={`absolute top-5 left-1/2 w-full h-0.5 -z-0 ${wizardStep > step ? 'bg-indigo-600' : 'bg-slate-100'}`}></div>}
                  </div>
                ))}
              </div>

              {wizardStep === 1 && (
                <div className="space-y-4 animate-in fade-in">
                  <p className="text-sm font-bold text-slate-800">1. 규칙의 이름을 정해주세요.</p>
                  <input 
                    type="text" 
                    value={newRule.name}
                    onChange={(e) => setNewRule({...newRule, name: e.target.value})}
                    placeholder="예: 블랙프라이데이 자동 할인"
                    className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-500/10"
                  />
                  <div className="p-4 bg-blue-50 rounded-2xl border border-blue-100 flex gap-3">
                    <AlertCircle className="text-blue-500 shrink-0" size={18} />
                    <p className="text-xs text-blue-700 leading-relaxed font-medium">관리하기 쉬운 이름을 입력하면 나중에 검색이 편리합니다.</p>
                  </div>
                </div>
              )}

              {wizardStep === 2 && (
                <div className="space-y-4 animate-in fade-in">
                  <p className="text-sm font-bold text-slate-800">2. 언제 실행할까요? (트리거)</p>
                  <div className="grid grid-cols-2 gap-3">
                    {['신규 주문', '재고 변경', '배송 시작', '결제 실패'].map(t => (
                      <button 
                        key={t}
                        onClick={() => setNewRule({...newRule, trigger: t})}
                        className={`p-4 rounded-2xl border text-sm font-bold transition-all ${newRule.trigger === t ? 'border-indigo-600 bg-indigo-50 text-indigo-700' : 'border-slate-100 hover:border-slate-300'}`}
                      >
                        {t}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {wizardStep === 3 && (
                <div className="space-y-4 animate-in fade-in">
                  <p className="text-sm font-bold text-slate-800">3. 어떤 작업을 수행할까요? (액션)</p>
                  <div className="grid grid-cols-2 gap-3">
                    {['Slack 알림', '이메일 발송', '주문 상태 변경', '재고 조정'].map(a => (
                      <button 
                        key={a}
                        onClick={() => setNewRule({...newRule, action: a})}
                        className={`p-4 rounded-2xl border text-sm font-bold transition-all ${newRule.action === a ? 'border-indigo-600 bg-indigo-50 text-indigo-700' : 'border-slate-100 hover:border-slate-300'}`}
                      >
                        {a}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="p-6 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
              <button 
                onClick={() => wizardStep > 1 ? setWizardStep(wizardStep - 1) : setShowNewRuleModal(false)}
                className="px-6 py-3 text-slate-500 font-bold text-sm hover:bg-slate-100 rounded-2xl"
              >
                {wizardStep === 1 ? '취소' : '이전'}
              </button>
              <button 
                onClick={() => wizardStep < 3 ? setWizardStep(wizardStep + 1) : handleCreateRule()}
                className="px-8 py-3 bg-indigo-600 text-white font-bold text-sm rounded-2xl shadow-xl shadow-indigo-500/20 active:scale-95"
              >
                {wizardStep === 3 ? '규칙 생성하기' : '다음 단계'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AutomationView;
