
import React, { useState } from 'react';
import { Bell, Search, Globe, Menu } from 'lucide-react';
import GlobalSearchModal from './GlobalSearchModal';
import { useTranslation, useGlobalData } from '../App';

const Header: React.FC = () => {
  const { lang, t } = useTranslation();
  const { setIsSidebarOpen } = useGlobalData();
  const [showGlobalSearch, setShowGlobalSearch] = useState(false);

  return (
    <header className="h-16 bg-white border-b border-slate-200 sticky top-0 z-[90] px-4 md:px-8 flex items-center justify-between shadow-sm">
      <div className="flex items-center gap-4">
        <button 
          onClick={() => setIsSidebarOpen(true)}
          className="lg:hidden p-2 text-slate-500 hover:bg-slate-50 rounded-lg"
        >
          <Menu size={20} />
        </button>
        
        <div className="hidden md:block">
          <button 
            onClick={() => setShowGlobalSearch(true)}
            className="flex items-center gap-3 pl-4 pr-12 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-400 hover:border-indigo-300 hover:bg-white transition-all text-left"
          >
            <Search size={14} className="text-slate-300" />
            <span className="font-medium">{t('search_placeholder')}</span>
          </button>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button className="flex items-center gap-1.5 text-slate-500 hover:text-indigo-600 font-black text-[10px] uppercase transition-colors">
          <Globe size={14} className="hidden sm:block" />
          {lang === 'ko' ? 'KR' : 'EN'}
        </button>
        
        <button className="p-2 rounded-lg bg-slate-50 text-slate-400 relative hover:text-indigo-600 transition-colors border border-slate-200">
          <Bell size={18} />
          <span className="absolute top-1.5 right-1.5 w-1.5 h-1.5 bg-rose-500 border border-white rounded-full"></span>
        </button>
        
        <div className="w-px h-6 bg-slate-200 hidden sm:block mx-1"></div>
        
        <div className="flex items-center gap-3 cursor-pointer group">
          <div className="w-9 h-9 rounded-lg bg-slate-900 flex items-center justify-center text-white font-black shadow-md text-sm">H</div>
          <div className="text-left hidden lg:block">
            <p className="text-xs font-black text-slate-800 leading-none">홍길동님</p>
            <p className="text-[9px] text-slate-400 font-bold uppercase mt-1">Admin</p>
          </div>
        </div>
      </div>

      <GlobalSearchModal isOpen={showGlobalSearch} onClose={() => setShowGlobalSearch(false)} />
    </header>
  );
};

export default Header;
