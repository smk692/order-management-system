
import React, { useState } from 'react';
import { 
  Search, Plus, Filter, LayoutGrid, List, ChevronRight, 
  Archive, Trash2, Tag, CheckCircle2, X, Square, CheckSquare,
  SearchX, ArrowRight, Sparkles, Box
} from 'lucide-react';
import ProductDetailModal from '../components/ProductDetailModal';
import NewProductModal from '../components/NewProductModal';
import { Product } from '../types';

const MASTER_PRODUCTS: Product[] = [
  { 
    id: 'OMS-FG0015687674', 
    sku: 'OUT-QLT-001',
    name: { ko: '[유통기한임박] 스텔라앤츄이스 독 로우 코티드 키블 10kg', en: 'Stella & Chewy\'s Dog Food 10kg' }, 
    brand: 'Stella & Chewy\'s',
    category: '반려동물 > 강아지 사료', 
    basePrice: 89000, 
    totalStock: 156, 
    status: 'ACTIVE',
    uom: 'PCS',
    hsCode: '6103.42.0000',
    countryOfOrigin: '미국 (US)',
    materialContent: 'Chicken 90%',
    netWeight: 10,
    grossWeight: 11,
    dimensions: { width: 62, length: 39, height: 8, unit: 'cm' },
    barcodes: [{ code: 'A594195', isMain: true }],
    logisticsInfo: {
      tempMgmt: 'Normal', shelfLifeMgmt: true, snMgmt: false,
      isDangerous: false, isFragile: false, isHighValue: false, isNonStandard: false
    },
    manufacturerDetails: { name: 'Stella & Chewy\'s LLC', address: 'Oak Creek, WI' },
    customsStrategies: []
  }
];

const ProductView: React.FC<{ setActiveTab?: (tab: string) => void }> = ({ setActiveTab }) => {
  const [products, setProducts] = useState<Product[]>(MASTER_PRODUCTS);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [isNewProductModalOpen, setIsNewProductModalOpen] = useState(false);
  const [viewType, setViewType] = useState<'grid' | 'table'>('table');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);

  const filteredProducts = products.filter(p => 
    p.name.ko.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.sku.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleAddProduct = (newProduct: Product) => {
    setProducts([newProduct, ...products]);
  };

  const toggleSelect = (id: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setSelectedIds(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === filteredProducts.length) setSelectedIds([]);
    else setSelectedIds(filteredProducts.map(p => p.id));
  };

  return (
    <div className="space-y-8 relative pb-32">
      {/* Search Header Section */}
      <div className="bg-white p-8 rounded-[2.5rem] border border-slate-200 shadow-sm space-y-8 transition-all">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="space-y-2">
            <h1 className="text-3xl font-black text-slate-900 tracking-tight flex items-center gap-3 italic uppercase">
              Product Master Console
            </h1>
            <p className="text-slate-500 font-medium text-sm">SKU, 상품명 또는 바코드로 마스터 데이터를 통합 조회합니다.</p>
          </div>
          <div className="flex items-center gap-2">
            <button 
              onClick={() => setIsNewProductModalOpen(true)}
              className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-2xl text-sm font-black hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-500/20 active:scale-95"
            >
              <Plus size={20} /> 새 상품 등록
            </button>
          </div>
        </div>

        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative group">
            <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors" size={20} />
            <input 
              type="text" 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="찾으시는 상품의 SKU, 명칭, ID를 입력하세요..." 
              className="w-full pl-14 pr-6 py-4 bg-slate-50 border border-transparent rounded-[1.5rem] text-sm font-bold outline-none focus:bg-white focus:ring-4 focus:ring-indigo-500/5 focus:border-indigo-200 transition-all shadow-inner" 
            />
          </div>
          <button className="px-6 py-4 bg-white border border-slate-200 rounded-[1.5rem] text-xs font-black text-slate-600 flex items-center justify-center gap-2 hover:bg-slate-50 transition-all">
            <Filter size={16} /> 상세 필터
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      {filteredProducts.length > 0 ? (
        <div className="bg-white rounded-[2.5rem] border border-slate-200 shadow-sm overflow-hidden animate-in fade-in duration-500">
           <div className="overflow-x-auto">
              <table className="w-full text-left min-w-[1000px]">
                <thead>
                  <tr className="border-b border-slate-100 bg-slate-50/50">
                    <th className="w-16 py-6 px-6 text-center">
                      <button onClick={toggleSelectAll} className="text-slate-400">
                        {selectedIds.length === filteredProducts.length && filteredProducts.length > 0 ? <CheckSquare size={20} className="text-indigo-600" /> : <Square size={20} />}
                      </button>
                    </th>
                    <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest">식별 정보 (ID/SKU)</th>
                    <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest">상품 마스터 정보</th>
                    <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">카테고리</th>
                    <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-right">기준 판매가</th>
                    <th className="py-6 px-4 text-[11px] font-black text-slate-400 uppercase tracking-widest text-center">가용 재고</th>
                    <th className="py-6 px-8 w-20"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {filteredProducts.map(prod => {
                    const isSelected = selectedIds.includes(prod.id);
                    return (
                      <tr key={prod.id} onClick={() => setSelectedProductId(prod.id)} className={`hover:bg-slate-50/80 transition-all group cursor-pointer ${isSelected ? 'bg-indigo-50/30' : ''}`}>
                        <td className="py-6 px-6 text-center" onClick={(e) => e.stopPropagation()}>
                           <button onClick={() => toggleSelect(prod.id)} className={`${isSelected ? 'text-indigo-600' : 'text-slate-300 hover:text-indigo-400'}`}>
                              {isSelected ? <CheckSquare size={20} /> : <Square size={20} />}
                           </button>
                        </td>
                        <td className="py-6 px-4">
                           <div className="flex flex-col gap-0.5">
                              <span className="text-[10px] font-black text-indigo-600 uppercase tracking-tighter">OMS: {prod.id}</span>
                              <span className="text-xs font-black text-slate-900 mt-0.5">SKU: {prod.sku}</span>
                           </div>
                        </td>
                        <td className="py-6 px-4">
                           <p className="text-sm font-black text-slate-800 group-hover:text-indigo-600 transition-colors truncate max-w-sm">{prod.name.ko}</p>
                           <p className="text-[10px] text-slate-400 font-bold mt-1 uppercase tracking-tighter">{prod.brand}</p>
                        </td>
                        <td className="py-6 px-4 text-center">
                           <span className="text-[10px] font-black text-slate-500 bg-slate-100 border border-slate-200 px-3 py-1.5 rounded-xl uppercase tracking-tighter">
                             {prod.category.split(' > ').pop()}
                           </span>
                        </td>
                        <td className="py-6 px-4 text-right font-black text-slate-900 text-sm italic">₩ {prod.basePrice.toLocaleString()}</td>
                        <td className="py-6 px-4 text-center">
                          <span className={`px-3 py-1.5 rounded-xl text-[11px] font-black border ${prod.totalStock < 20 ? 'bg-rose-50 text-rose-600 border-rose-100' : 'bg-emerald-50 text-emerald-600 border-emerald-100'}`}>
                            {prod.totalStock.toLocaleString()} EA
                          </span>
                        </td>
                        <td className="py-6 px-8 text-right">
                           <ChevronRight size={18} className="text-slate-300 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
           </div>
        </div>
      ) : (
        /* Empty State with Action */
        <div className="bg-white border-2 border-dashed border-slate-200 rounded-[4rem] p-24 text-center animate-in zoom-in-95 duration-500">
           <div className="w-24 h-24 bg-slate-50 rounded-[2.5rem] flex items-center justify-center mx-auto mb-8 text-slate-300">
              <SearchX size={48} />
           </div>
           <h3 className="text-2xl font-black text-slate-900 tracking-tight mb-3">검색 결과가 없습니다</h3>
           <p className="text-slate-500 font-medium mb-10 max-w-sm mx-auto break-keep">
             입력하신 "{searchQuery}"에 해당하는 상품을 찾을 수 없습니다. 신규 상품으로 등록하시겠습니까?
           </p>
           <div className="flex items-center justify-center gap-4">
              <button 
                onClick={() => setSearchQuery('')}
                className="px-8 py-3 bg-white border border-slate-200 text-slate-500 text-xs font-black uppercase tracking-widest rounded-2xl hover:bg-slate-50 transition-all"
              >
                검색어 초기화
              </button>
              <button 
                onClick={() => setIsNewProductModalOpen(true)}
                className="px-10 py-3 bg-slate-900 text-white text-xs font-black uppercase tracking-widest rounded-2xl shadow-xl active:scale-95 transition-all flex items-center gap-3"
              >
                <Plus size={18} /> 새 상품으로 등록하기
              </button>
           </div>
        </div>
      )}

      {/* Bulk Action Bar */}
      {selectedIds.length > 0 && (
        <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-[200] w-full max-w-lg px-4 animate-in slide-in-from-bottom-5 duration-300">
           <div className="bg-slate-900 text-white p-5 rounded-[2.5rem] shadow-[0_25px_50px_-12px_rgba(0,0,0,0.5)] flex items-center justify-between gap-6 border border-white/10 backdrop-blur-md">
              <div className="flex items-center gap-4">
                 <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center font-black text-lg shadow-inner">
                    {selectedIds.length}
                 </div>
                 <div>
                    <p className="text-xs font-black uppercase tracking-widest text-indigo-400">Selected</p>
                    <p className="text-[10px] text-slate-500 font-bold">일괄 작업을 선택하세요</p>
                 </div>
              </div>
              <div className="flex items-center gap-2">
                 <button className="p-3 hover:bg-white/10 rounded-xl text-slate-400 hover:text-white transition-all"><Tag size={20}/></button>
                 <button className="p-3 hover:bg-rose-500/20 rounded-xl text-rose-400 transition-all"><Trash2 size={20}/></button>
                 <div className="w-px h-6 bg-white/10 mx-2"></div>
                 <button onClick={() => setSelectedIds([])} className="p-3 hover:bg-white/10 rounded-xl text-slate-400"><X size={20}/></button>
              </div>
           </div>
        </div>
      )}

      <ProductDetailModal 
        isOpen={!!selectedProductId} 
        onClose={() => setSelectedProductId(null)} 
        productId={selectedProductId} 
        setActiveTab={setActiveTab}
      />
      <NewProductModal isOpen={isNewProductModalOpen} onClose={() => setIsNewProductModalOpen(false)} onAdd={handleAddProduct} />
    </div>
  );
};

export default ProductView;
