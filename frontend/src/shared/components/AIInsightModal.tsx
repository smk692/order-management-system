
import React from 'react';
import { X, Sparkles, TrendingDown, AlertTriangle, Lightbulb, ArrowRight, CheckCircle2 } from 'lucide-react';

interface AIInsightModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const AIInsightModal: React.FC<AIInsightModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[120] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="ai-insight-title">
      <div className="absolute inset-0 bg-slate-900/40 backdrop-blur-md" onClick={onClose}></div>

      <div className="relative bg-white w-full max-w-2xl rounded-[40px] shadow-2xl overflow-hidden flex flex-col animate-in slide-in-from-bottom-8 duration-500">
        <div className="p-8 bg-gradient-to-br from-violet-600 to-indigo-700 text-white relative">
          <div className="absolute top-0 right-0 w-48 h-48 bg-white/10 rounded-full blur-3xl -mr-16 -mt-16"></div>
          <div className="flex justify-between items-start relative z-10">
            <div className="flex items-center gap-3 px-4 py-2 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold mb-6">
              <Sparkles size={16} />
              Gemini Pro Intelligence
            </div>
            <button onClick={onClose} className="p-2 hover:bg-white/10 rounded-full transition-colors">
              <X size={24} />
            </button>
          </div>
          <h2 id="ai-insight-title" className="text-3xl font-bold mb-2">재고 최적화 분석 리포트</h2>
          <p className="text-indigo-100 text-sm opacity-80 font-medium">실시간 판매 데이터와 계절적 수요 예측을 반영한 결과입니다.</p>
        </div>

        <div className="flex-1 p-8 space-y-6 bg-slate-50/50">
          {/* Observation */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-5 bg-white rounded-3xl border border-slate-100 shadow-sm flex gap-4">
              <div className="w-10 h-10 bg-rose-50 rounded-2xl flex items-center justify-center text-rose-500 flex-shrink-0">
                <TrendingDown size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase mb-1">관찰된 패턴</h4>
                <p className="text-sm font-bold text-slate-800 leading-tight">아우터 카테고리 판매량<br />전주 대비 18% 하락</p>
              </div>
            </div>
            <div className="p-5 bg-white rounded-3xl border border-slate-100 shadow-sm flex gap-4">
              <div className="w-10 h-10 bg-amber-50 rounded-2xl flex items-center justify-center text-amber-500 flex-shrink-0">
                <AlertTriangle size={20} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase mb-1">예상 위험</h4>
                <p className="text-sm font-bold text-slate-800 leading-tight">SKU-002 모델<br />3주 내 재고 과잉 가능성</p>
              </div>
            </div>
          </div>

          {/* Detailed Recommendation */}
          <div className="p-6 bg-indigo-50 border border-indigo-100 rounded-3xl space-y-4">
            <h3 className="text-sm font-bold text-indigo-900 flex items-center gap-2">
              <Lightbulb size={18} className="text-indigo-600" />
              AI 권장 조치사항
            </h3>
            <ul className="space-y-3">
              {[
                '클래식 청바지(SKU-002)의 안전재고를 50개에서 30개로 하향 조정하십시오.',
                '재고 회전율이 낮은 상품 12종에 대해 네이버 스토어 타임세일 프로모션을 제안합니다.',
                '아마존 US 창고의 입고 스케줄을 5일 연기하여 물류 보관 비용을 절감하십시오.',
              ].map((rec, idx) => (
                <li key={idx} className="flex gap-3 text-sm text-indigo-800/80 font-medium leading-relaxed">
                  <div className="mt-1 flex-shrink-0 w-4 h-4 bg-indigo-200 rounded-full flex items-center justify-center text-[10px] font-bold text-indigo-700">
                    {idx + 1}
                  </div>
                  {rec}
                </li>
              ))}
            </ul>
          </div>

          <div className="space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase ml-1">자동화 반영 대기 중</h3>
            <div className="p-4 bg-white border border-slate-100 rounded-2xl flex items-center justify-between group cursor-pointer hover:border-indigo-300 transition-all">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 bg-slate-50 rounded-lg flex items-center justify-center text-slate-400 group-hover:bg-indigo-50 group-hover:text-indigo-600">
                  <CheckCircle2 size={16} />
                </div>
                <span className="text-sm font-bold text-slate-700">안전재고 자동 조정 활성화</span>
              </div>
              <ArrowRight size={16} className="text-slate-300 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
            </div>
          </div>
        </div>

        <div className="p-8 bg-white border-t border-slate-100 flex gap-3">
          <button onClick={onClose} className="flex-1 py-3 text-slate-500 font-bold text-sm hover:bg-slate-50 rounded-2xl transition-colors">
            나중에 확인
          </button>
          <button className="flex-1 py-3 bg-indigo-600 text-white font-bold text-sm rounded-2xl shadow-xl shadow-indigo-200 hover:bg-indigo-700 transition-all">
            권장사항 즉시 적용
          </button>
        </div>
      </div>
    </div>
  );
};

export default AIInsightModal;
