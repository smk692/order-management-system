
import React from 'react';
import { MENU_GROUPS } from '../constants';
import { useTranslation, useGlobalData } from '../App';
import { X } from 'lucide-react';

interface SidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ activeTab, setActiveTab }) => {
  const { t } = useTranslation();
  const { isSidebarOpen, setIsSidebarOpen } = useGlobalData();

  return (
    <>
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[100] lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        ></div>
      )}

      <aside className={`
        fixed left-0 top-0 h-screen w-64 bg-[#0F172A] text-slate-400 flex flex-col z-[110] border-r border-slate-800 transition-transform duration-300
        ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        <div className="p-8 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 bg-indigo-600 rounded-lg flex items-center justify-center font-black text-white shadow-lg text-lg">G</div>
            <div className="flex flex-col">
              <span className="text-base font-black text-white tracking-tighter leading-none italic uppercase">Global Hub</span>
              <span className="text-[9px] font-bold text-slate-500 uppercase tracking-widest mt-1">OMS Enterprise</span>
            </div>
          </div>
          <button onClick={() => setIsSidebarOpen(false)} className="lg:hidden p-2 text-slate-500 hover:text-white">
            <X size={24} />
          </button>
        </div>
        
        <nav className="flex-1 px-4 py-2 space-y-6 overflow-y-auto scrollbar-hide" aria-label="Main navigation">
          {MENU_GROUPS.map((group) => (
            <div key={group.id} className="space-y-1">
              <h3 className="px-4 text-[9px] font-black text-slate-600 uppercase tracking-[0.2em] mb-3">{t(group.labelKey)}</h3>
              <div className="space-y-0.5">
                {group.items.map((item) => (
                  <button
                    key={item.id}
                    onClick={() => setActiveTab(item.id)}
                    aria-current={activeTab === item.id ? 'page' : undefined}
                    className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg transition-all duration-200 group relative ${
                      activeTab === item.id
                        ? 'bg-indigo-600 text-white shadow-md'
                        : 'hover:bg-slate-800 hover:text-slate-200'
                    }`}
                  >
                    <span className={`${activeTab === item.id ? 'text-white' : 'text-slate-500 group-hover:text-slate-300'}`}>
                      {item.icon}
                    </span>
                    <span className="font-bold text-xs">{t(item.labelKey)}</span>
                  </button>
                ))}
              </div>
            </div>
          ))}
        </nav>

        <div className="p-6 mt-auto">
          <div className="bg-slate-800/40 rounded-xl p-3 flex items-center gap-3 border border-slate-700/50">
            <div className="w-8 h-8 rounded-lg bg-slate-700 flex items-center justify-center text-[10px] font-black text-white">JD</div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-black text-white truncate">홍길동 관리자</p>
              <p className="text-[9px] text-slate-500 font-bold uppercase mt-0.5">Admin</p>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
