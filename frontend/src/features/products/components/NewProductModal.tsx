
import React, { useState } from 'react';
import { 
  X, Save, CheckCircle2, ChevronRight, ChevronLeft, 
  Archive, Info, AlertTriangle, Barcode, Box, 
  Layout, Sparkles, Database, ShieldCheck, Zap
} from 'lucide-react';

interface NewProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (product: any) => void;
}

const NewProductModal: React.FC<NewProductModalProps> = ({ isOpen, onClose, onAdd }) => {
  const [step, setStep] = useState(1);
  const [useBarcode, setUseBarcode] = useState<boolean | null>(null);
  const [formData, setFormData] = useState({
    nameKo: '',
    ownerName: '본사',
    sku: '',
    category: '',
    price: '',
    stock: '',
    uom: '개',
    barcode: '',
    origin: '대한민국 (KR)',
    weightGross: '',
    weightNet: '',
    dimW: '',
    dimL: '',
    dimH: '',
  });

  if (!isOpen) return null;

  const nextStep = () => setStep(prev => prev + 1);
  const prevStep = () => setStep(prev => prev - 1);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.nameKo || !formData.sku) {
      alert("상품명과 SKU 코드는 필수 입력 항목입니다.");
      setStep(1);
      return;
    }

    const newProduct = {
      id: `OMS-${Math.floor(Math.random() * 1000000)}`,
      sku: formData.sku,
      name: { 'ko': formData.nameKo },
      brand: 'Global Brand',
      category: formData.category || '미분류',
      basePrice: parseInt(formData.price) || 0,
      totalStock: parseInt(formData.stock) || 0,
      status: 'ACTIVE',
      uom: formData.uom,
      barcodes: useBarcode && formData.barcode ? [{ code: formData.barcode, isMain: true }] : [],
      dimensions: {
        width: parseFloat(formData.dimW) || 0,
        length: parseFloat(formData.dimL) || 0,
        height: parseFloat(formData.dimH) || 0,
        unit: 'mm'
      },
      netWeight: parseFloat(formData.weightNet) || 0,
      grossWeight: parseFloat(formData.weightGross) || 0,
    };

    onAdd(newProduct);
    onClose();
    // Reset state
    setStep(1);
    setUseBarcode(null);
  };

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="new-product-modal-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-md" onClick={onClose}></div>

      <div className="relative bg-white w-full max-w-2xl rounded-[3rem] shadow-2xl overflow-hidden flex flex-col animate-in zoom-in-95 duration-300 border border-slate-200">
        {/* Step Indicator */}
        <div className="p-8 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
           <div className="flex items-center gap-3">
              {[1, 2, 3].map(i => (
                <div key={i} className={`flex items-center gap-2 px-3 py-1.5 rounded-full text-[10px] font-black transition-all ${step === i ? 'bg-indigo-600 text-white shadow-lg' : step > i ? 'bg-emerald-500 text-white' : 'bg-white text-slate-300 border border-slate-100'}`}>
                   <span>STEP 0{i}</span>
                   {step > i && <CheckCircle2 size={12} />}
                </div>
              ))}
           </div>
           <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-xl text-slate-400"><X size={24}/></button>
        </div>

        <div className="p-10 min-h-[500px] flex flex-col">
           {step === 1 && (
             <div className="space-y-8 animate-in fade-in slide-in-from-right-4 duration-300">
                <div className="space-y-1">
                   <h2 id="new-product-modal-title" className="text-2xl font-black text-slate-900 tracking-tight">상품 기본 식별 정보</h2>
                   <p className="text-sm text-slate-400 font-medium italic">마스터 상품을 생성하기 위한 필수 단계입니다.</p>
                </div>
                <div className="space-y-6">
                   <div className="space-y-2">
                      <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">상품명 (KO)</label>
                      <input 
                        type="text" 
                        value={formData.nameKo} 
                        onChange={(e) => setFormData({...formData, nameKo: e.target.value})} 
                        className="w-full p-4 bg-slate-50 border border-slate-100 rounded-2xl text-sm font-bold outline-none focus:ring-4 focus:ring-indigo-500/5 focus:bg-white transition-all shadow-inner" 
                        placeholder="정확한 상품 명칭을 입력하세요" 
                      />
                   </div>
                   <div className="grid grid-cols-2 gap-6">
                      <div className="space-y-2">
                         <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">관리 코드 (SKU)</label>
                         <input 
                          type="text" 
                          value={formData.sku} 
                          onChange={(e) => setFormData({...formData, sku: e.target.value})} 
                          className="w-full p-4 bg-slate-50 border border-slate-100 rounded-2xl text-sm font-bold outline-none focus:ring-4 focus:ring-indigo-500/5 transition-all shadow-inner" 
                          placeholder="SKU-XXXX" 
                        />
                      </div>
                      <div className="space-y-2">
                         <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">카테고리</label>
                         <input 
                          type="text" 
                          value={formData.category} 
                          onChange={(e) => setFormData({...formData, category: e.target.value})} 
                          className="w-full p-4 bg-slate-50 border border-slate-100 rounded-2xl text-sm font-bold outline-none focus:ring-4 focus:ring-indigo-500/5 transition-all shadow-inner" 
                          placeholder="식품 > 반려동물 > 사료" 
                        />
                      </div>
                   </div>
                </div>
             </div>
           )}

           {step === 2 && (
             <div className="space-y-8 animate-in fade-in slide-in-from-right-4 duration-300">
                <div className="space-y-1">
                   <div className="flex items-center justify-between">
                      <h2 className="text-2xl font-black text-slate-900 tracking-tight">바코드 등록 (선택)</h2>
                      <span className="px-3 py-1 bg-amber-50 text-amber-600 rounded-lg text-[10px] font-black uppercase border border-amber-100">Optional Step</span>
                   </div>
                   <p className="text-sm text-slate-400 font-medium italic">바코드가 없는 경우 시스템 SKU로 자동 식별됩니다.</p>
                </div>
                
                <div className="space-y-4">
                   <button 
                    onClick={() => setUseBarcode(true)}
                    className={`w-full p-6 rounded-3xl border-2 text-left transition-all flex items-center justify-between group ${useBarcode === true ? 'border-indigo-600 bg-indigo-50/50 shadow-lg' : 'border-slate-100 bg-white hover:border-slate-200'}`}
                   >
                      <div className="flex items-center gap-5">
                         <div className={`w-14 h-14 rounded-2xl flex items-center justify-center transition-colors ${useBarcode === true ? 'bg-indigo-600 text-white shadow-xl' : 'bg-slate-50 text-slate-400 group-hover:bg-slate-100'}`}>
                            <Barcode size={28} />
                         </div>
                         <div>
                            <p className={`text-sm font-black ${useBarcode === true ? 'text-indigo-900' : 'text-slate-700'}`}>바코드 정보가 있습니다</p>
                            <p className="text-xs text-slate-400 font-bold uppercase tracking-widest mt-0.5">Physical Barcode Scan / Key-in</p>
                         </div>
                      </div>
                      <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${useBarcode === true ? 'border-indigo-600 bg-indigo-600' : 'border-slate-200'}`}>
                         {useBarcode === true && <div className="w-2 h-2 bg-white rounded-full"></div>}
                      </div>
                   </button>

                   <button 
                    onClick={() => { setUseBarcode(false); setFormData({...formData, barcode: ''}); }}
                    className={`w-full p-6 rounded-3xl border-2 text-left transition-all flex items-center justify-between group ${useBarcode === false ? 'border-slate-900 bg-slate-50 shadow-lg' : 'border-slate-100 bg-white hover:border-slate-200'}`}
                   >
                      <div className="flex items-center gap-5">
                         <div className={`w-14 h-14 rounded-2xl flex items-center justify-center transition-colors ${useBarcode === false ? 'bg-slate-900 text-white shadow-xl' : 'bg-slate-50 text-slate-400 group-hover:bg-slate-100'}`}>
                            <Database size={28} />
                         </div>
                         <div>
                            <p className={`text-sm font-black ${useBarcode === false ? 'text-slate-900' : 'text-slate-700'}`}>바코드가 없거나 추후 등록</p>
                            <p className="text-xs text-slate-400 font-bold uppercase tracking-widest mt-0.5">Use System ID as identifier</p>
                         </div>
                      </div>
                      <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${useBarcode === false ? 'border-slate-900 bg-slate-900' : 'border-slate-200'}`}>
                         {useBarcode === false && <div className="w-2 h-2 bg-white rounded-full"></div>}
                      </div>
                   </button>
                </div>

                {useBarcode === true && (
                  <div className="space-y-2 animate-in zoom-in-95 duration-200">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">주 바코드 번호 (Main EAN/UPC)</label>
                    <div className="relative">
                       <Barcode size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" />
                       <input 
                        type="text" 
                        autoFocus
                        value={formData.barcode} 
                        onChange={(e) => setFormData({...formData, barcode: e.target.value})} 
                        className="w-full pl-12 pr-4 py-4 bg-white border border-indigo-200 rounded-2xl text-sm font-black text-indigo-600 outline-none focus:ring-4 focus:ring-indigo-500/10" 
                        placeholder="스캐너를 사용하거나 직접 입력하세요" 
                       />
                    </div>
                  </div>
                )}
             </div>
           )}

           {step === 3 && (
             <div className="space-y-8 animate-in fade-in slide-in-from-right-4 duration-300">
                <div className="space-y-1">
                   <h2 className="text-2xl font-black text-slate-900 tracking-tight">최종 확인 및 운영 정보</h2>
                   <p className="text-sm text-slate-400 font-medium italic">등록 전 입력 정보를 검토해주십시오.</p>
                </div>
                <div className="grid grid-cols-2 gap-6 bg-slate-50 p-8 rounded-[2rem] border border-slate-100">
                   <div className="space-y-4">
                      <div>
                         <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">상품명</p>
                         <p className="text-sm font-black text-slate-900 mt-1">{formData.nameKo}</p>
                      </div>
                      <div>
                         <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">관리 SKU</p>
                         <p className="text-sm font-black text-indigo-600 mt-1">{formData.sku}</p>
                      </div>
                   </div>
                   <div className="space-y-4 border-l border-slate-200 pl-6">
                      <div>
                         <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">바코드 상태</p>
                         <p className="text-sm font-black text-slate-900 mt-1">{useBarcode ? formData.barcode : '시스템 ID 사용'}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Zap size={14} className="text-amber-500 fill-amber-500" />
                        <span className="text-[10px] font-black uppercase text-amber-600">등록 즉시 WMS 연동 가능</span>
                      </div>
                   </div>
                </div>
                <div className="p-4 bg-indigo-50 rounded-2xl border border-indigo-100 flex gap-3">
                   <ShieldCheck className="text-indigo-600 shrink-0" size={18} />
                   <p className="text-[11px] text-indigo-700 font-bold leading-relaxed">이 상품은 마스터 데이터로 등록되어 모든 연동 채널에서 주문 수집 및 재고 관리가 가능해집니다.</p>
                </div>
             </div>
           )}

           {/* Navigation Buttons */}
           <div className="mt-auto pt-10 flex justify-between gap-3">
              <button 
                onClick={step === 1 ? onClose : prevStep}
                className="px-8 py-4 bg-slate-100 text-slate-500 text-xs font-black uppercase tracking-widest rounded-2xl hover:bg-slate-200 transition-all flex items-center gap-2"
              >
                <ChevronLeft size={16} /> {step === 1 ? '취소' : '이전 단계'}
              </button>
              
              {step < 3 ? (
                <button 
                  onClick={nextStep}
                  disabled={step === 1 && (!formData.nameKo || !formData.sku)}
                  className="px-10 py-4 bg-slate-900 text-white text-xs font-black uppercase tracking-widest rounded-2xl shadow-xl active:scale-95 transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  다음 단계 <ChevronRight size={16} />
                </button>
              ) : (
                <button 
                  onClick={handleSubmit}
                  className="px-12 py-4 bg-indigo-600 text-white text-xs font-black uppercase tracking-widest rounded-2xl shadow-xl shadow-indigo-500/20 active:scale-95 transition-all flex items-center gap-2"
                >
                  <Save size={18} /> 마스터 등록 완료
                </button>
              )}
           </div>
        </div>
      </div>
    </div>
  );
};

export default NewProductModal;
