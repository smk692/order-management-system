
import React, { useState } from 'react';
import { User, Users, Bell, Shield, Save, Mail, MessageSquare, Smartphone, Globe, Loader2 } from 'lucide-react';

const SettingsView: React.FC = () => {
  const [activeSubTab, setActiveSubTab] = useState('general');
  const [autoSync, setAutoSync] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [notifications, setNotifications] = useState({
    email: true,
    slack: false,
    push: false
  });

  const handleSave = () => {
    setIsSaving(true);
    setTimeout(() => {
      setIsSaving(false);
      alert("시스템 설정이 성공적으로 저장되었습니다.");
    }, 1500);
  };

  const handleInvite = () => {
    const email = prompt("초대할 팀원의 이메일을 입력하세요:");
    if (email) alert(`${email}님에게 초대 메일을 발송했습니다.`);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">시스템 설정</h1>
          <p className="text-slate-500 text-sm">시스템 운영 환경 및 사용자 권한을 관리합니다.</p>
        </div>
        <button 
          onClick={handleSave}
          disabled={isSaving}
          className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-blue-500/20 hover:bg-blue-700 transition-all disabled:opacity-70"
        >
          {isSaving ? <Loader2 size={18} className="animate-spin" /> : <Save size={18} />}
          {isSaving ? "저장 중..." : "설정 저장"}
        </button>
      </div>

      <div className="flex flex-col lg:flex-row gap-8">
        {/* Sidebar Nav */}
        <div className="w-full lg:w-64 space-y-1">
          {[
            { id: 'general', label: '계정 설정', icon: <User size={18} /> },
            { id: 'team', label: '팀원 관리', icon: <Users size={18} /> },
            { id: 'notifications', label: '알림 설정', icon: <Bell size={18} /> },
            { id: 'security', label: '보안 관리', icon: <Shield size={18} /> },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveSubTab(tab.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                activeSubTab === tab.id 
                  ? 'bg-white text-blue-600 shadow-sm border border-slate-200' 
                  : 'text-slate-500 hover:bg-slate-100'
              }`}
            >
              {tab.icon}
              {tab.label}
            </button>
          ))}
        </div>

        {/* Content Area */}
        <div className="flex-1 bg-white rounded-3xl border border-slate-200 shadow-sm p-8 min-h-[500px]">
          {activeSubTab === 'general' && (
            <div className="space-y-8 animate-in fade-in duration-300">
              <div>
                <h3 className="text-lg font-bold text-slate-800 mb-6">프로필 정보</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">사용자 이름</label>
                    <input type="text" defaultValue="홍길동" className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium outline-none focus:ring-2 focus:ring-blue-500/20" />
                  </div>
                  <div className="space-y-2">
                    <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">이메일 주소</label>
                    <input type="email" defaultValue="kildong.hong@globaloms.com" className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium outline-none focus:ring-2 focus:ring-blue-500/20" />
                  </div>
                  <div className="space-y-2">
                    <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">기본 언어</label>
                    <select className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium outline-none">
                      <option>한국어 (KO)</option>
                      <option>English (US)</option>
                      <option>日本語 (JP)</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">표시 통화</label>
                    <select className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium outline-none">
                      <option>KRW (₩)</option>
                      <option>USD ($)</option>
                      <option>JPY (¥)</option>
                    </select>
                  </div>
                </div>
              </div>
              <hr className="border-slate-100" />
              <div>
                <h3 className="text-lg font-bold text-slate-800 mb-6">시스템 환경</h3>
                <div className="space-y-4">
                  <div className="flex items-center justify-between p-5 bg-slate-50 rounded-2xl border border-slate-100">
                    <div>
                      <p className="text-sm font-bold text-slate-800">자동 데이터 동기화</p>
                      <p className="text-[11px] text-slate-500 font-medium">채널별 주문 데이터를 5분마다 자동으로 가져옵니다.</p>
                    </div>
                    <button 
                      onClick={() => setAutoSync(!autoSync)}
                      className={`w-12 h-6 rounded-full relative transition-all duration-300 ${autoSync ? 'bg-blue-600 shadow-inner' : 'bg-slate-300'}`}
                    >
                      <div className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow-md transition-all duration-300 ${autoSync ? 'translate-x-7' : 'translate-x-1'}`}></div>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeSubTab === 'team' && (
            <div className="space-y-6 animate-in fade-in duration-300">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-bold text-slate-800">팀원 및 권한</h3>
                <button 
                  onClick={handleInvite}
                  className="px-4 py-2 bg-slate-100 text-slate-600 text-xs font-bold rounded-xl hover:bg-slate-200 transition-colors"
                >
                  초대하기
                </button>
              </div>
              <div className="divide-y divide-slate-100 border border-slate-100 rounded-2xl overflow-hidden">
                {[
                  { name: '홍길동', role: 'Owner', email: 'owner@example.com', status: 'Active' },
                  { name: '김철수', role: 'Editor', email: 'chulsoo@example.com', status: 'Active' },
                  { name: '이영희', role: 'Viewer', email: 'younghee@example.com', status: 'Pending' },
                ].map((member) => (
                  <div key={member.email} className="p-4 bg-white flex items-center justify-between group hover:bg-slate-50 transition-colors">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-indigo-50 rounded-full flex items-center justify-center font-bold text-indigo-600">{member.name[0]}</div>
                      <div>
                        <p className="text-sm font-bold text-slate-800">{member.name}</p>
                        <p className="text-[11px] text-slate-400 font-medium">{member.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <span className="text-[11px] font-bold text-slate-500 uppercase tracking-widest">{member.role}</span>
                      <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${member.status === 'Active' ? 'bg-emerald-100 text-emerald-600' : 'bg-slate-100 text-slate-400'}`}>
                        {member.status}
                      </span>
                      <button 
                        onClick={() => alert(`${member.name}님의 권한을 수정합니다.`)}
                        className="p-1.5 hover:bg-white rounded-lg opacity-0 group-hover:opacity-100 transition-opacity border border-slate-200 shadow-sm"
                      >
                        <Save size={14} className="text-slate-400" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeSubTab === 'notifications' && (
            <div className="space-y-8 animate-in fade-in duration-300">
              <h3 className="text-lg font-bold text-slate-800 mb-6">알림 수단 및 채널</h3>
              <div className="space-y-4">
                {[
                  { id: 'email', icon: <Mail size={20} />, title: '이메일 리포트', desc: '일일 마감 보고서 및 주요 장애 알림' },
                  { id: 'slack', icon: <MessageSquare size={20} />, title: '슬랙(Slack) 연동', desc: '실시간 주문 및 품절 위험 알림' },
                  { id: 'push', icon: <Smartphone size={20} />, title: '모바일 푸시', desc: '긴급 클레임 및 시스템 보안 이슈' },
                ].map((item) => (
                  <div key={item.id} className="flex items-center justify-between p-5 border border-slate-100 rounded-[2rem] hover:bg-slate-50 transition-colors">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 bg-white border border-slate-100 rounded-2xl flex items-center justify-center text-slate-500 shadow-sm">{item.icon}</div>
                      <div>
                        <p className="text-sm font-bold text-slate-800">{item.title}</p>
                        <p className="text-[11px] text-slate-500 font-medium">{item.desc}</p>
                      </div>
                    </div>
                    <button 
                      onClick={() => setNotifications(prev => ({ ...prev, [item.id]: !prev[item.id as keyof typeof prev] }))}
                      className={`w-12 h-6 rounded-full relative transition-all duration-300 ${notifications[item.id as keyof typeof notifications] ? 'bg-blue-600 shadow-inner' : 'bg-slate-300'}`}
                    >
                      <div className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow-md transition-all duration-300 ${notifications[item.id as keyof typeof notifications] ? 'translate-x-7' : 'translate-x-1'}`}></div>
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeSubTab === 'security' && (
            <div className="space-y-8 animate-in fade-in duration-300">
              <h3 className="text-lg font-bold text-slate-800 mb-6">계정 및 데이터 보안</h3>
              <div className="space-y-4">
                <div className="p-8 bg-rose-50 border border-rose-100 rounded-[2.5rem] flex items-start gap-6">
                  <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-rose-500 shadow-sm flex-shrink-0">
                    <Shield size={24} />
                  </div>
                  <div className="flex-1">
                    <p className="text-base font-bold text-rose-800">2단계 인증(2FA) 미설정</p>
                    <p className="text-xs text-rose-600 leading-relaxed mt-2 font-medium">계정 보안을 강화하기 위해 OTP 설정을 강력히 권장합니다. 보안 사고 발생 시 데이터 복구 및 책임 소재 확인이 어려울 수 있습니다.</p>
                    <button 
                      onClick={() => alert("2FA 설정 페이지로 이동합니다.")}
                      className="mt-6 px-6 py-2.5 bg-rose-600 text-white text-xs font-bold rounded-xl hover:bg-rose-700 transition-shadow shadow-lg shadow-rose-200"
                    >
                      보안 설정 바로가기
                    </button>
                  </div>
                </div>
                <div className="p-6 bg-slate-50 border border-slate-100 rounded-[2rem] flex items-center justify-between">
                  <div>
                    <p className="text-sm font-bold text-slate-800">API 접근 제어 (IP 화이트리스트)</p>
                    <p className="text-[11px] text-slate-500 mt-1 font-medium">등록된 특정 IP 주소에서만 시스템 접근을 허용하여 외부 유출을 차단합니다.</p>
                  </div>
                  <button onClick={() => alert("IP 화이트리스트 관리 창을 엽니다.")} className="px-4 py-2 bg-white border border-slate-200 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition-all">관리</button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SettingsView;
