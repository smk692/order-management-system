
import React, { useState, useEffect } from 'react';
import { 
  X, Archive, Ruler, Save, Settings2, BadgeCheck, 
  Barcode, Thermometer, PackageSearch, Printer, Monitor, 
  FileStack, Layout, List, Share2, Loader2,
  ChevronRight, ToggleLeft, ToggleRight, RefreshCw, Link2, ExternalLink, AlertCircle
} from 'lucide-react';

interface ProductDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  productId: string | null;
  setActiveTab?: (tab: string) => void;
}

// 공통 텍스트 입력 필드
const InfoField = ({ label, value, fieldName, isEditing, onChange, highlight = false, placeholder = "" }: any) => (
  <div className="flex flex-col gap-1 py-2 px-3">
    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">{label}</span>
    {isEditing ? (
      <input 
        value={value || ''} 
        placeholder={placeholder}
        onChange={(e) => onChange(fieldName, e.target.value)}
        className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20 w-full"
      />
    ) : (
      <span className={`text-xs font-semibold ${highlight ? 'text-indigo-600' : 'text-slate-700'} break-keep min-h-[1.5rem] flex items-center`}>
        {value || '-'}
      </span>
    )}
  </div>
);

// 단위 선택 필드 (Select Box)
const SelectField = ({ label, value, fieldName, options, isEditing, onChange }: any) => (
  <div className="flex flex-col gap-1 py-2 px-3">
    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">{label}</span>
    {isEditing ? (
      <select 
        value={value || ''} 
        onChange={(e) => onChange(fieldName, e.target.value)}
        className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20 w-full appearance-none cursor-pointer"
      >
        <option value="">선택</option>
        {options.map((opt: string) => <option key={opt} value={opt}>{opt}</option>)}
      </select>
    ) : (
      <span className="text-xs font-semibold text-slate-700 min-h-[1.5rem] flex items-center">
        {value || '-'}
      </span>
    )}
  </div>
);

// 토글 스위치 필드
const ToggleField = ({ label, value, fieldName, isEditing, onChange }: any) => (
  <div className="flex flex-col gap-1 py-2 px-3">
    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">{label}</span>
    <div className="flex items-center h-[1.5rem]">
      {isEditing ? (
        <button 
          onClick={() => onChange(fieldName, !value)}
          className={`flex items-center gap-2 transition-all ${value ? 'text-indigo-600' : 'text-slate-300'}`}
        >
          {value ? <ToggleRight size={24} /> : <ToggleLeft size={24} />}
          <span className="text-xs font-bold">{value ? '사용' : '미사용'}</span>
        </button>
      ) : (
        <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${value ? 'bg-indigo-50 text-indigo-600 border-indigo-100' : 'bg-slate-50 text-slate-400 border-slate-100'}`}>
          {value ? '예 (YES)' : '아니오 (NO)'}
        </span>
      )}
    </div>
  </div>
);

const ProductDetailModal: React.FC<ProductDetailModalProps> = ({ isOpen, onClose, productId, setActiveTab }) => {
  const [activeTab, setActiveTabLocal] = useState<'basic' | 'logistics' | 'channels'>('basic');
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [productData, setProductData] = useState<any>({});

  // 단위 옵션들
  const UOM_OPTIONS = ['개', '세트', '박스', 'kg', 'EA', '台', 'PCS', 'BOX'];
  const DIM_UNIT_OPTIONS = ['mm', 'cm', 'm', 'inch'];
  const WEIGHT_UNIT_OPTIONS = ['kg', 'g', 'lb'];

  // 데이터 초기화
  useEffect(() => {
    if (productId && isOpen) {
      setProductData({
        omsId: 'OMS-FG0015687674',
        platformId: '594195',
        sku: 'OUT-QLT-001',
        name: '[유통기한임박] 스텔라앤츄이스 독 로우 코티드 키블 10kg',
        ownerName: '펫프렌즈 (PET FRIENDS)',
        custCode: 'C20240428992581',
        cat1: '반려동물',
        cat2: '강아지',
        cat3: '사료',
        cat4: '건식사료',
        uom: '개',
        sizeType: '중형',
        consignment: false,
        origPackaging: true,
        consumables: false,
        gift: false,
        nonStandard: false,
        dimW: '620',
        dimL: '390',
        dimH: '85',
        dimUnit: 'mm',
        weightGross: '11',
        weightGrossUnit: 'kg',
        weightNet: '10',
        weightNetUnit: 'kg',
        logistics: {
          shelfLife: true,
          tempMgmt: '상온 (Normal)',
          expDays: '7',
          inboundDays: '180',
          warningDays: '180'
        },
        barcodes: [{ code: 'A594195', unit: '1 기본단위' }],
        channels: [
          { id: 'CH-001', name: '자사몰', logo: '🛍️', status: 'ACTIVE', channelSku: 'M-594195', price: 89000, lastSync: '12분 전' },
          { id: 'CH-002', name: '네이버', logo: '🟢', status: 'ACTIVE', channelSku: 'N-2024-X12', price: 89000, lastSync: '5분 전' },
          { id: 'CH-003', name: '쿠팡', logo: '🚀', status: 'ACTIVE', channelSku: 'C-778844', price: 92000, lastSync: '방금 전' },
          { id: 'CH-004', name: '아마존 US', logo: '📦', status: 'ERROR', channelSku: 'AZ-DOG-10K', price: 75.99, lastSync: '2시간 전' },
        ]
      });
    }
  }, [productId, isOpen]);

  const handleFieldChange = (field: string, value: any) => {
    setProductData((prev: any) => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    setIsSaving(true);
    await new Promise(resolve => setTimeout(resolve, 800));
    setIsEditing(false);
    setIsSaving(false);
    alert("마스터 데이터가 성공적으로 수정되었습니다.");
  };

  const handleNavigateToMapping = () => {
    if (setActiveTab) {
      setActiveTab('product_mapping');
      onClose();
    }
  };

  if (!isOpen || !productData.omsId) return null;

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="product-detail-modal-title">
      <div className="absolute inset-0 bg-slate-900/70 backdrop-blur-md" onClick={onClose}></div>

      <div className="relative bg-white w-full max-w-7xl rounded-xl shadow-2xl overflow-hidden flex flex-col max-h-[92vh] animate-in zoom-in-95 duration-200 border border-slate-200">

        {/* Header */}
        <div className="px-6 py-4 flex items-center justify-between border-b border-slate-200 bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-slate-800 rounded flex items-center justify-center text-white shadow-md">
              <Archive size={20} />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 id="product-detail-modal-title" className="text-lg font-bold text-slate-900">상품 마스터 정보</h2>
                <span className="px-2 py-0.5 bg-emerald-50 text-emerald-600 rounded text-[10px] font-black border border-emerald-100 uppercase tracking-widest">정상 운영</span>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {!isEditing ? (
              <button onClick={() => setIsEditing(true)} className="flex items-center gap-2 px-4 py-2 rounded text-xs font-bold bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-all">
                <Settings2 size={14}/>
                <span>정보 수정</span>
              </button>
            ) : (
              <button onClick={handleSave} disabled={isSaving} className="flex items-center gap-2 px-4 py-2 rounded text-xs font-bold bg-indigo-600 text-white shadow-lg active:scale-95 transition-all">
                {isSaving ? <Loader2 size={14} className="animate-spin" /> : <Save size={14}/>}
                <span>수정 내용 저장</span>
              </button>
            )}
            <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded text-slate-400"><X size={24} /></button>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="flex px-6 border-b border-slate-200 bg-white">
          {['기본 정보', '물류 설정', '채널 연동 현황'].map((label, idx) => {
            const ids = ['basic', 'logistics', 'channels'];
            return (
              <button
                key={ids[idx]}
                onClick={() => setActiveTabLocal(ids[idx] as any)}
                className={`px-6 py-4 text-xs font-bold transition-all relative flex items-center gap-2 ${activeTab === ids[idx] ? 'text-indigo-600 bg-slate-50' : 'text-slate-400 hover:text-slate-600'}`}
              >
                {label}
                {activeTab === ids[idx] && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-indigo-600"></div>}
              </button>
            );
          })}
        </div>

        {/* Content Area */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-slate-50/30 scrollbar-hide">
          {activeTab === 'basic' && (
            <div className="space-y-6 animate-in fade-in duration-200">
              <section className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-sm">
                <div className="px-4 py-3 bg-slate-50 border-b border-slate-200 flex items-center gap-2">
                  <FileStack size={14} className="text-slate-500" />
                  <h3 className="text-sm font-bold text-slate-800 tracking-tight">마스터 기본 정보</h3>
                </div>
                <div className="p-6 flex flex-col md:flex-row gap-8">
                  <div className="w-full md:w-48 h-48 bg-slate-100 border border-slate-200 rounded-lg flex items-center justify-center text-slate-300 shrink-0">
                    <Monitor size={48} strokeWidth={1} />
                  </div>
                  <div className="flex-1 grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-y-2">
                    <div className="col-span-2 lg:col-span-2"><InfoField label="OMS ID" value={productData.omsId} fieldName="omsId" isEditing={isEditing} onChange={handleFieldChange} highlight /></div>
                    <div className="col-span-2 lg:col-span-2"><InfoField label="플랫폼 ID" value={productData.platformId} fieldName="platformId" isEditing={isEditing} onChange={handleFieldChange} highlight /></div>
                    <div className="col-span-1 lg:col-span-1 flex items-center pt-2"><span className="px-3 py-1 bg-indigo-50 text-indigo-600 rounded text-[10px] font-bold border border-indigo-100">채널 연동 중</span></div>
                    
                    <div className="col-span-5"><InfoField label="상품명" value={productData.name} fieldName="name" isEditing={isEditing} onChange={handleFieldChange} /></div>
                    
                    <div className="col-span-5 flex flex-col gap-1 py-2 px-3">
                      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">상품 카테고리 (1차 {'>'} 2차 {'>'} 3차 {'>'} 4차 직접 입력)</span>
                      {isEditing ? (
                        <div className="grid grid-cols-4 gap-2">
                          <input value={productData.cat1 || ''} placeholder="1차" onChange={(e) => handleFieldChange('cat1', e.target.value)} className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20" />
                          <input value={productData.cat2 || ''} placeholder="2차" onChange={(e) => handleFieldChange('cat2', e.target.value)} className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20" />
                          <input value={productData.cat3 || ''} placeholder="3차" onChange={(e) => handleFieldChange('cat3', e.target.value)} className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20" />
                          <input value={productData.cat4 || ''} placeholder="4차" onChange={(e) => handleFieldChange('cat4', e.target.value)} className="text-xs font-bold text-indigo-600 bg-white border border-indigo-200 rounded px-2 py-1.5 outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        </div>
                      ) : (
                        <div className="flex items-center gap-2 text-xs font-semibold text-slate-700 h-[1.5rem]">
                          <span>{productData.cat1 || '-'}</span> <ChevronRight size={12} className="text-slate-300"/>
                          <span>{productData.cat2 || '-'}</span> <ChevronRight size={12} className="text-slate-300"/>
                          <span>{productData.cat3 || '-'}</span> <ChevronRight size={12} className="text-slate-300"/>
                          <span>{productData.cat4 || '-'}</span>
                        </div>
                      )}
                    </div>

                    <InfoField label="화주사명" value={productData.ownerName} fieldName="ownerName" isEditing={isEditing} onChange={handleFieldChange} />
                    <InfoField label="물류 고객사 코드" value={productData.custCode} fieldName="custCode" isEditing={isEditing} onChange={handleFieldChange} />
                    <SelectField label="기본 단위 (UoM)" value={productData.uom} fieldName="uom" options={UOM_OPTIONS} isEditing={isEditing} onChange={handleFieldChange} />
                    <InfoField label="크기 분류" value={productData.sizeType} fieldName="sizeType" isEditing={isEditing} onChange={handleFieldChange} />
                    <div className="col-span-1"></div>

                    <ToggleField label="위탁 판매 여부" value={productData.consignment} fieldName="consignment" isEditing={isEditing} onChange={handleFieldChange} />
                    <ToggleField label="원포장 여부" value={productData.origPackaging} fieldName="origPackaging" isEditing={isEditing} onChange={handleFieldChange} />
                    <ToggleField label="소모품 여부" value={productData.consumables} fieldName="consumables" isEditing={isEditing} onChange={handleFieldChange} />
                    <ToggleField label="사은품 여부" value={productData.gift} fieldName="gift" isEditing={isEditing} onChange={handleFieldChange} />
                    <ToggleField label="비규격 여부" value={productData.nonStandard} fieldName="nonStandard" isEditing={isEditing} onChange={handleFieldChange} />
                  </div>
                </div>
              </section>

              <section className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-sm">
                <div className="px-4 py-3 bg-slate-50 border-b border-slate-200 flex items-center gap-2">
                  <Ruler size={14} className="text-slate-500" />
                  <h3 className="text-sm font-bold text-slate-800 tracking-tight">상품 제원 정보</h3>
                </div>
                <div className="p-6">
                   <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-x-8 gap-y-6 mb-8">
                      <div className="flex flex-col gap-1 py-2 px-3">
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">규격 (W x L x H)</span>
                        <div className="flex items-center gap-2 h-[1.5rem]">
                          {isEditing ? (
                            <>
                              <input value={productData.dimW} onChange={(e) => handleFieldChange('dimW', e.target.value)} className="w-12 text-xs font-bold text-center border border-indigo-200 rounded text-indigo-600 outline-none" />
                              <span className="text-slate-300">x</span>
                              <input value={productData.dimL} onChange={(e) => handleFieldChange('dimL', e.target.value)} className="w-12 text-xs font-bold text-center border border-indigo-200 rounded text-indigo-600 outline-none" />
                              <span className="text-slate-300">x</span>
                              <input value={productData.dimH} onChange={(e) => handleFieldChange('dimH', e.target.value)} className="w-12 text-xs font-bold text-center border border-indigo-200 rounded text-indigo-600 outline-none" />
                              <select value={productData.dimUnit} onChange={(e) => handleFieldChange('dimUnit', e.target.value)} className="ml-1 text-[10px] font-bold text-indigo-600 bg-slate-50 border border-indigo-100 rounded px-1 outline-none">
                                {DIM_UNIT_OPTIONS.map(u => <option key={u} value={u}>{u}</option>)}
                              </select>
                            </>
                          ) : (
                            <span className="text-xs font-semibold text-slate-700">
                              {productData.dimW} x {productData.dimL} x {productData.dimH} {productData.dimUnit}
                            </span>
                          )}
                        </div>
                      </div>

                      <div className="flex flex-col gap-1 py-2 px-3">
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">총 중량 (Gross)</span>
                        <div className="flex items-center gap-2 h-[1.5rem]">
                          {isEditing ? (
                            <>
                              <input value={productData.weightGross} onChange={(e) => handleFieldChange('weightGross', e.target.value)} className="w-full text-xs font-bold border border-indigo-200 rounded text-indigo-600 outline-none px-2" />
                              <select value={productData.weightGrossUnit} onChange={(e) => handleFieldChange('weightGrossUnit', e.target.value)} className="text-[10px] font-bold text-indigo-600 bg-slate-50 border border-indigo-100 rounded px-1 outline-none">
                                {WEIGHT_UNIT_OPTIONS.map(u => <option key={u} value={u}>{u}</option>)}
                              </select>
                            </>
                          ) : (
                            <span className="text-xs font-semibold text-slate-700">{productData.weightGross}{productData.weightGrossUnit}</span>
                          )}
                        </div>
                      </div>

                      <div className="flex flex-col gap-1 py-2 px-3">
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-tight">순 중량 (Net)</span>
                        <div className="flex items-center gap-2 h-[1.5rem]">
                          {isEditing ? (
                            <>
                              <input value={productData.weightNet} onChange={(e) => handleFieldChange('weightNet', e.target.value)} className="w-full text-xs font-bold border border-indigo-200 rounded text-indigo-600 outline-none px-2" />
                              <select value={productData.weightNetUnit} onChange={(e) => handleFieldChange('weightNetUnit', e.target.value)} className="text-[10px] font-bold text-indigo-600 bg-slate-50 border border-indigo-100 rounded px-1 outline-none">
                                {WEIGHT_UNIT_OPTIONS.map(u => <option key={u} value={u}>{u}</option>)}
                              </select>
                            </>
                          ) : (
                            <span className="text-xs font-semibold text-slate-700">{productData.weightNet}{productData.weightNetUnit}</span>
                          )}
                        </div>
                      </div>
                   </div>

                   <div className="space-y-3">
                      <p className="text-[10px] font-bold text-slate-400 uppercase ml-3">관리 바코드</p>
                      <div className="flex flex-wrap gap-4 px-3">
                        {productData.barcodes?.map((b: any, i: number) => (
                          <div key={i} className="flex flex-col gap-1">
                             <span className="text-[10px] text-indigo-600 font-bold ml-1">{b.unit}</span>
                             <div className="flex items-center gap-4 px-4 py-2 border border-slate-200 rounded-lg bg-slate-50 group transition-all">
                                <Barcode size={20} className="text-slate-400" />
                                <span className="text-xs font-mono font-bold text-slate-800">{b.code}</span>
                                <button className="p-1 hover:bg-white rounded text-indigo-600" title="바코드 출력"><Printer size={14}/></button>
                             </div>
                          </div>
                        ))}
                      </div>
                   </div>
                </div>
              </section>
            </div>
          )}

          {activeTab === 'logistics' && (
            <div className="space-y-6 animate-in fade-in duration-200">
               <section className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-sm">
                <div className="px-4 py-3 bg-slate-50 border-b border-slate-200 flex items-center gap-2">
                  <Thermometer size={14} className="text-slate-500" />
                  <h3 className="text-sm font-bold text-slate-800 tracking-tight">물류 운영 설정</h3>
                </div>
                <div className="p-8 space-y-8">
                   <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
                      <ToggleField label="유통기한 관리" value={productData.logistics?.shelfLife} fieldName="logistics_shelfLife" isEditing={isEditing} onChange={(f, v) => setProductData((p: any) => ({ ...p, logistics: { ...p.logistics, shelfLife: v } }))} />
                   </div>
                   <div className="grid grid-cols-4 gap-6 px-3">
                      <InfoField label="유통기한 (일)" value={productData.logistics?.expDays} fieldName="logistics_expDays" isEditing={isEditing} onChange={(f, v) => setProductData((p: any) => ({ ...p, logistics: { ...p.logistics, expDays: v } }))} />
                      <InfoField label="입고 제한 (일)" value={productData.logistics?.inboundDays} fieldName="logistics_inboundDays" isEditing={isEditing} onChange={(f, v) => setProductData((p: any) => ({ ...p, logistics: { ...p.logistics, inboundDays: v } }))} />
                      <InfoField label="유통 경고 (일)" value={productData.logistics?.warningDays} fieldName="logistics_warningDays" isEditing={isEditing} onChange={(f, v) => setProductData((p: any) => ({ ...p, logistics: { ...p.logistics, warningDays: v } }))} />
                   </div>
                </div>
               </section>
            </div>
          )}

          {activeTab === 'channels' && (
            <div className="space-y-6 animate-in fade-in duration-200">
               <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  {productData.channels?.map((channel: any) => (
                    <div key={channel.id} className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm hover:border-indigo-300 transition-all group">
                       <div className="flex items-center justify-between mb-4">
                          <div className="flex items-center gap-3">
                             <span className="text-2xl">{channel.logo}</span>
                             <div>
                                <h4 className="text-sm font-black text-slate-800">{channel.name}</h4>
                                <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">{channel.id}</p>
                             </div>
                          </div>
                          <div className={`px-2 py-0.5 rounded text-[10px] font-black uppercase border ${
                            channel.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-rose-50 text-rose-600 border-rose-100'
                          }`}>
                            {channel.status}
                          </div>
                       </div>
                       
                       <div className="space-y-3 pt-3 border-t border-slate-100">
                          <div className="flex justify-between items-center">
                             <span className="text-[10px] font-bold text-slate-400 uppercase">Channel SKU</span>
                             <span className="text-xs font-mono font-bold text-slate-700">{channel.channelSku}</span>
                          </div>
                          <div className="flex justify-between items-center">
                             <span className="text-[10px] font-bold text-slate-400 uppercase">판매가</span>
                             <span className="text-xs font-black text-slate-900">
                               {typeof channel.price === 'number' ? `₩ ${channel.price.toLocaleString()}` : `$ ${channel.price}`}
                             </span>
                          </div>
                          <div className="flex justify-between items-center">
                             <span className="text-[10px] font-bold text-slate-400 uppercase">최근 동기화</span>
                             <span className="text-[10px] font-bold text-slate-500">{channel.lastSync}</span>
                          </div>
                       </div>

                       <div className="mt-5 flex gap-2">
                          <button className="flex-1 py-2 bg-slate-50 hover:bg-slate-100 text-slate-500 rounded-lg text-[10px] font-black uppercase tracking-widest border border-slate-100 flex items-center justify-center gap-2 transition-all">
                             <RefreshCw size={12} /> 동기화
                          </button>
                          <button 
                            onClick={handleNavigateToMapping}
                            className="flex-1 py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 rounded-lg text-[10px] font-black uppercase tracking-widest border border-indigo-100 flex items-center justify-center gap-2 transition-all"
                          >
                             <Link2 size={12} /> 매핑 관리
                          </button>
                       </div>
                    </div>
                  ))}
                  
                  {/* 연동 채널 추가 카드 */}
                  <button className="bg-slate-50 border-2 border-dashed border-slate-200 rounded-xl p-5 flex flex-col items-center justify-center text-center gap-2 hover:bg-white hover:border-indigo-300 group transition-all min-h-[180px]">
                     <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center text-slate-300 group-hover:scale-110 group-hover:text-indigo-600 transition-all shadow-sm">
                        <Share2 size={20} />
                     </div>
                     <span className="text-xs font-black text-slate-400 group-hover:text-indigo-600">신규 채널 연동</span>
                  </button>
               </div>

               <div className="bg-indigo-900 rounded-xl p-8 text-white relative overflow-hidden shadow-xl">
                  <div className="absolute right-0 top-0 p-8 opacity-10"><AlertCircle size={120} /></div>
                  <div className="relative z-10">
                     <div className="flex items-center gap-3 mb-4">
                        <AlertCircle className="text-indigo-300" size={24} />
                        <h3 className="text-lg font-black italic">채널별 가격 정책 주의사항</h3>
                     </div>
                     <p className="text-sm text-indigo-100/80 leading-relaxed max-w-2xl mb-6">
                        현재 쿠팡과 아마존 US의 판매가가 마스터 기준가와 상이하게 설정되어 있습니다. 
                        환율 변동 및 플랫폼 수수료를 고려하여 가격을 책정하였으나, 정기적인 수익성 검토를 권장합니다.
                     </p>
                     <button className="px-6 py-2.5 bg-white text-indigo-900 rounded-xl text-xs font-black uppercase tracking-widest shadow-xl hover:bg-indigo-50 transition-all flex items-center gap-2">
                        채널 가격 정책 설정 바로가기 <ChevronRight size={14} />
                     </button>
                  </div>
               </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-white border-t border-slate-200 flex justify-between items-center">
          <div className="flex items-center gap-2 text-slate-400">
             <BadgeCheck size={16} className="text-indigo-500" />
             <span className="text-[10px] font-black uppercase tracking-[0.2em] italic opacity-60">Global Hub Data Integrity System</span>
          </div>
          <button onClick={onClose} className="px-6 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 text-xs font-bold rounded">닫기</button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailModal;
