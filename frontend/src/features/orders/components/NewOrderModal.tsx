
import React, { useState } from 'react';
import { X, Save, ShoppingCart, User, Phone, MapPin, CreditCard, Plus, Trash2, Box } from 'lucide-react';

interface NewOrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (order: any) => void;
}

const MOCK_SELECTABLE_PRODUCTS = [
  { id: 'PROD-001', name: '울트라 경량 퀼팅 자켓', price: 89000 },
  { id: 'PROD-002', name: '베이직 코튼 슬림 팬츠', price: 45000 },
  { id: 'PROD-003', name: '캐시미어 블렌드 니트', price: 129000 },
];

const NewOrderModal: React.FC<NewOrderModalProps> = ({ isOpen, onClose, onAdd }) => {
  const [formData, setFormData] = useState({
    channel: 'MALL',
    customerName: '',
    phone: '',
    address: '',
    status: 'NEW'
  });
  const [selectedItems, setSelectedItems] = useState<any[]>([]);

  if (!isOpen) return null;

  const totalAmount = selectedItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const addItem = (product: any) => {
    setSelectedItems(prev => {
      const existing = prev.find(i => i.id === product.id);
      if (existing) {
        return prev.map(i => i.id === product.id ? { ...i, quantity: i.quantity + 1 } : i);
      }
      return [...prev, { ...product, quantity: 1 }];
    });
  };

  const removeItem = (id: string) => {
    setSelectedItems(prev => prev.filter(i => i.id !== id));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.customerName) {
      alert("고객명을 입력해주세요.");
      return;
    }
    if (selectedItems.length === 0) {
      alert("최소 하나 이상의 상품을 추가해주세요.");
      return;
    }
    
    const newOrder = {
      id: `ORD-${new Date().getTime().toString().slice(-8)}`,
      channel: formData.channel,
      orderDate: new Date().toISOString().replace('T', ' ').slice(0, 16),
      customerName: formData.customerName,
      totalAmount: totalAmount,
      status: formData.status,
      items: selectedItems.map(i => ({
        productId: i.id,
        productName: i.name,
        quantity: i.quantity,
        price: i.price
      }))
    };

    onAdd(newOrder);
    onClose();
    // Reset
    setFormData({ channel: 'MALL', customerName: '', phone: '', address: '', status: 'NEW' });
    setSelectedItems([]);
  };

  return (
    <div className="fixed inset-0 z-[150] flex items-center justify-center p-4" role="dialog" aria-modal="true" aria-labelledby="new-order-modal-title">
      <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={onClose}></div>

      <form
        onSubmit={handleSubmit}
        className="relative bg-white w-full max-w-3xl rounded-[2.5rem] shadow-2xl overflow-hidden flex flex-col animate-in zoom-in-95 duration-200 max-h-[90vh]"
      >
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-blue-500/20">
              <ShoppingCart size={20} />
            </div>
            <div>
              <h2 id="new-order-modal-title" className="text-xl font-bold text-slate-900">신규 주문 수동 등록</h2>
              <p className="text-xs text-slate-400 font-medium">전화 또는 오프라인 발생 주문을 직접 생성합니다.</p>
            </div>
          </div>
          <button type="button" onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full text-slate-400 transition-colors">
            <X size={24} />
          </button>
        </div>

        <div className="p-8 space-y-8 overflow-y-auto">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">주문 채널</label>
                <select 
                  value={formData.channel}
                  onChange={(e) => setFormData({...formData, channel: e.target.value})}
                  className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm outline-none"
                >
                  <option value="MALL">자사몰</option>
                  <option value="OFFLINE">오프라인 매장</option>
                  <option value="DIRECT">전화 주문</option>
                </select>
              </div>
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">고객명</label>
                <div className="relative">
                  <User className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={16} />
                  <input 
                    type="text" 
                    value={formData.customerName}
                    onChange={(e) => setFormData({...formData, customerName: e.target.value})}
                    placeholder="홍길동"
                    className="w-full pl-10 pr-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm outline-none" 
                  />
                </div>
              </div>
            </div>
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">연락처</label>
                <div className="relative">
                  <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" size={16} />
                  <input 
                    type="text" 
                    value={formData.phone}
                    onChange={(e) => setFormData({...formData, phone: e.target.value})}
                    placeholder="010-0000-0000"
                    className="w-full pl-10 pr-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm outline-none" 
                  />
                </div>
              </div>
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">주문 상태</label>
                <select 
                  value={formData.status}
                  onChange={(e) => setFormData({...formData, status: e.target.value as any})}
                  className="w-full p-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm outline-none"
                >
                  <option value="NEW">입금대기 (NEW)</option>
                  <option value="PAID">결제완료 (PAID)</option>
                </select>
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">배송지 주소</label>
            <div className="relative">
              <MapPin className="absolute left-4 top-4 text-slate-300" size={16} />
              <textarea 
                rows={2}
                value={formData.address}
                onChange={(e) => setFormData({...formData, address: e.target.value})}
                placeholder="상세 주소를 입력하세요"
                className="w-full pl-10 pr-4 py-4 bg-slate-50 border border-slate-200 rounded-2xl text-sm outline-none"
              ></textarea>
            </div>
          </div>

          {/* Product Selection Area */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="p-6 bg-slate-50 rounded-[2rem] border border-slate-100">
              <h3 className="text-sm font-bold text-slate-800 mb-4 flex items-center gap-2">
                <Plus size={16} className="text-blue-600" />
                상품 선택
              </h3>
              <div className="space-y-2">
                {MOCK_SELECTABLE_PRODUCTS.map(p => (
                  <button 
                    key={p.id}
                    type="button"
                    onClick={() => addItem(p)}
                    className="w-full p-4 bg-white border border-slate-200 rounded-2xl flex items-center justify-between hover:border-blue-500 hover:shadow-md transition-all group"
                  >
                    <div className="text-left">
                      <p className="text-sm font-bold text-slate-800">{p.name}</p>
                      <p className="text-xs text-slate-400">₩ {p.price.toLocaleString()}</p>
                    </div>
                    <Plus size={18} className="text-slate-300 group-hover:text-blue-600" />
                  </button>
                ))}
              </div>
            </div>

            <div className="p-6 bg-slate-900 rounded-[2rem] text-white">
              <h3 className="text-sm font-bold mb-4 flex items-center gap-2">
                <Box size={16} className="text-indigo-400" />
                선택된 상품 ({selectedItems.length})
              </h3>
              <div className="space-y-3 max-h-[200px] overflow-y-auto mb-4 pr-2">
                {selectedItems.map(item => (
                  <div key={item.id} className="flex items-center justify-between p-3 bg-white/5 rounded-xl border border-white/10">
                    <div className="flex-1">
                      <p className="text-xs font-bold">{item.name}</p>
                      <p className="text-[10px] text-slate-400">{item.quantity}개 x ₩ {item.price.toLocaleString()}</p>
                    </div>
                    <button 
                      type="button"
                      onClick={() => removeItem(item.id)}
                      className="p-1.5 text-slate-500 hover:text-rose-400 transition-colors"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                ))}
                {selectedItems.length === 0 && (
                  <p className="text-xs text-slate-500 italic text-center py-4">상품을 선택해주세요.</p>
                )}
              </div>
              <div className="pt-4 border-t border-slate-800 flex justify-between items-end">
                <span className="text-xs text-slate-400 font-bold">합계</span>
                <span className="text-2xl font-bold text-indigo-400">₩ {totalAmount.toLocaleString()}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="p-6 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
          <button 
            type="button"
            onClick={onClose} 
            className="px-6 py-2.5 bg-white border border-slate-200 text-slate-600 text-sm font-bold rounded-xl hover:bg-slate-100 transition-colors"
          >
            취소
          </button>
          <button 
            type="submit"
            className="flex items-center gap-2 px-8 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 transition-colors shadow-lg shadow-blue-500/20"
          >
            <Save size={18} />
            주문 생성
          </button>
        </div>
      </form>
    </div>
  );
};

export default NewOrderModal;
