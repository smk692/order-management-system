
import React, { useState } from 'react';
import { X, Copy, Terminal, ChevronRight, Hash, Code, Braces, TerminalSquare, MousePointer2, RefreshCw, Loader2 } from 'lucide-react';

interface LogDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  logId: string | null;
}

const LogDetailModal: React.FC<LogDetailModalProps> = ({ isOpen, onClose, logId }) => {
  const [isRetrying, setIsRetrying] = useState(false);

  if (!isOpen || !logId) return null;

  const mockJson = {
    request: {
      timestamp: new Date().toISOString(),
      endpoint: "https://api.navershop.com/v1/orders/ORD-00124",
      method: "GET",
      headers: {
        "X-API-Key": "NVR_SECRET_********",
        "Accept": "application/json",
        "Cache-Control": "no-cache"
      },
      params: { "include_shipment": "true", "culture": "ko-KR" }
    },
    response: {
      status: 200,
      responseTime: "124ms",
      body: {
        "order_id": "ORD-20250118-005",
        "customer": {
          "id": "CUST-9921",
          "tier": "VIP"
        },
        "items": [
          { "sku": "SKU-00124", "qty": 1, "price": 89000, "currency": "KRW" }
        ],
        "status": "PAID"
      }
    }
  };

  const copyToClipboard = (data: any) => {
    const textToCopy = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
    navigator.clipboard.writeText(textToCopy);
    alert('JSON 데이터가 복사되었습니다.');
  };

  const copyAsCurl = () => {
    const curl = `curl -X GET "${mockJson.request.endpoint}" \\
  -H "X-API-Key: ${mockJson.request.headers['X-API-Key']}" \\
  -H "Accept: application/json"`;
    navigator.clipboard.writeText(curl);
    alert('CURL 명령어가 복사되었습니다.');
  };

  const handleRetryTransaction = () => {
    setIsRetrying(true);
    setTimeout(() => {
      setIsRetrying(false);
      alert(`${logId} 트랜잭션의 재처리가 완료되었습니다.`);
    }, 1500);
  };

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-slate-900/70 backdrop-blur-md" onClick={onClose}></div>
      
      <div className="relative bg-[#0F172A] w-full max-w-5xl rounded-[3rem] shadow-2xl border border-slate-700/50 overflow-hidden flex flex-col max-h-[92vh] animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-8 border-b border-white/5 flex items-center justify-between bg-white/[0.02]">
          <div className="flex items-center gap-5">
            <div className="w-14 h-14 bg-indigo-500/10 rounded-2xl flex items-center justify-center text-indigo-400 border border-indigo-500/20 shadow-inner">
              <TerminalSquare size={28} />
            </div>
            <div>
              <div className="flex items-center gap-3">
                 <h2 className="text-xl font-black text-white italic tracking-tight uppercase">API Intelligence Analyzer</h2>
                 <span className="px-2 py-0.5 bg-emerald-500/10 text-emerald-500 rounded-lg text-[10px] font-black uppercase tracking-widest border border-emerald-500/20">Success</span>
              </div>
              <p className="text-xs text-slate-500 font-mono mt-1 tracking-tight">{logId} | TraceID: 4f92-a1b2-c3d4-e5f6</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
             <button onClick={copyAsCurl} className="px-4 py-2 bg-white/5 hover:bg-white/10 text-slate-400 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all">Copy as CURL</button>
             <button onClick={onClose} className="p-3 hover:bg-white/10 rounded-2xl text-slate-500 transition-colors">
               <X size={28} />
             </button>
          </div>
        </div>

        {/* Content - Two Column Terminal View */}
        <div className="flex-1 overflow-y-auto p-10 grid grid-cols-1 lg:grid-cols-2 gap-10">
          {/* Request Section */}
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h3 className="text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] flex items-center gap-2">
                <ChevronRight size={14} className="text-indigo-400" /> Incoming Request
              </h3>
              <button onClick={() => copyToClipboard(mockJson.request)} className="p-2 hover:bg-white/5 rounded-xl text-slate-600 hover:text-slate-300 transition-all">
                <Copy size={16} />
              </button>
            </div>
            <div className="bg-slate-950/80 rounded-[2rem] border border-white/5 p-8 font-mono text-[11px] leading-relaxed text-indigo-300/90 overflow-x-auto shadow-inner relative group">
              <Braces className="absolute right-6 top-6 opacity-5 group-hover:opacity-10 transition-opacity" size={80} />
              <pre className="relative z-10">{JSON.stringify(mockJson.request, null, 2)}</pre>
            </div>
          </div>

          {/* Response Section */}
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h3 className="text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] flex items-center gap-2">
                <ChevronRight size={14} className="text-emerald-400" /> Outgoing Response
              </h3>
              <button onClick={() => copyToClipboard(mockJson.response)} className="p-2 hover:bg-white/5 rounded-xl text-slate-600 hover:text-slate-300 transition-all">
                <Copy size={16} />
              </button>
            </div>
            <div className="bg-slate-950/80 rounded-[2rem] border border-white/5 p-8 font-mono text-[11px] leading-relaxed text-emerald-300/90 overflow-x-auto shadow-inner relative group">
              <div className="mb-6 flex items-center gap-3 relative z-10">
                <span className="px-3 py-1 bg-emerald-50 text-slate-900 rounded-lg font-black text-[10px] uppercase">200 OK</span>
                <span className="text-[10px] text-slate-600 font-bold tracking-widest italic">Time: 124ms</span>
              </div>
              <Braces className="absolute right-6 top-6 opacity-5 group-hover:opacity-10 transition-opacity" size={80} />
              <pre className="relative z-10">{JSON.stringify(mockJson.response, null, 2)}</pre>
            </div>
          </div>
        </div>

        {/* Footer Info */}
        <div className="p-8 bg-black/20 border-t border-white/5 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex flex-wrap gap-8">
            <div className="flex items-center gap-3">
              <div className="w-2 h-2 rounded-full bg-emerald-500"></div>
              <div>
                 <p className="text-[9px] font-black text-slate-600 uppercase tracking-widest">Protocol</p>
                 <p className="text-xs font-bold text-slate-300">REST / JSON</p>
              </div>
            </div>
            <div className="flex items-center gap-3 border-l border-white/10 pl-8">
              <div className="w-2 h-2 rounded-full bg-indigo-500"></div>
              <div>
                 <p className="text-[9px] font-black text-slate-600 uppercase tracking-widest">Version</p>
                 <p className="text-xs font-bold text-slate-300">API v2.4-stable</p>
              </div>
            </div>
          </div>
          
          <div className="flex gap-3 w-full sm:w-auto">
            <button 
              onClick={handleRetryTransaction}
              disabled={isRetrying}
              className="flex-1 sm:flex-none px-8 py-4 bg-white/10 text-white text-xs font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-white/20 transition-all flex items-center justify-center gap-3"
            >
              {isRetrying ? <Loader2 size={14} className="animate-spin" /> : <RefreshCw size={14} />}
              Retry Transaction
            </button>
            <button onClick={onClose} className="flex-1 sm:flex-none px-12 py-4 bg-indigo-600 text-white text-xs font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-indigo-700 transition-all shadow-2xl shadow-indigo-900/50 active:scale-95">
              CLOSE ANALYZER
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LogDetailModal;
