
import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, Send, X, Sparkles, Bot, User, Loader2 } from 'lucide-react';
import { aiService } from '../services/aiService';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const AIAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<Message[]>([
    { role: 'assistant', content: '안녕하세요! Global OMS AI 운영 비서입니다. 실시간 매출 분석, 재고 예측 또는 시스템 설정에 대해 궁금한 점을 물어보세요.' }
  ]);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || isTyping) return;

    const userMessage = input;
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setInput('');
    setIsTyping(true);

    try {
      const response = await aiService.chat({
        message: userMessage,
        context: { currentPage: 'dashboard' },
      });
      setMessages(prev => [...prev, { role: 'assistant', content: response.message }]);
    } catch (error) {
      console.error("AI Service Error:", error);
      setMessages(prev => [...prev, { role: 'assistant', content: "죄송합니다. AI 연결에 실패했습니다." }]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className="fixed bottom-6 right-6 z-[200]">
      {!isOpen ? (
        <button
          onClick={() => setIsOpen(true)}
          className="w-14 h-14 bg-indigo-600 text-white rounded-2xl shadow-2xl flex items-center justify-center hover:scale-110 transition-transform active:scale-95 group"
        >
          <Sparkles className="group-hover:animate-pulse" />
          <div className="absolute -top-1 -right-1 w-4 h-4 bg-rose-500 border-2 border-white rounded-full"></div>
        </button>
      ) : (
        <div className="w-[420px] h-[600px] bg-white rounded-[2rem] shadow-2xl border border-slate-200 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-300">
          {/* Header */}
          <div className="p-5 bg-indigo-600 text-white flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center">
                <Bot size={24} />
              </div>
              <div>
                <p className="text-sm font-bold">Global AI Intelligence</p>
                <div className="flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-pulse"></span>
                  <span className="text-[10px] text-indigo-100 font-bold uppercase tracking-wider">Operational Awareness: ON</span>
                </div>
              </div>
            </div>
            <button onClick={() => setIsOpen(false)} className="p-2 hover:bg-white/10 rounded-xl transition-colors">
              <X size={20} />
            </button>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-50/50">
            {messages.map((msg, idx) => (
              <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`flex gap-3 max-w-[85%] ${msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'}`}>
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 ${
                    msg.role === 'user' ? 'bg-indigo-100 text-indigo-600' : 'bg-white text-slate-400 shadow-sm border border-slate-100'
                  }`}>
                    {msg.role === 'user' ? <User size={16} /> : <Bot size={16} />}
                  </div>
                  <div className={`p-4 rounded-2xl text-sm leading-relaxed ${
                    msg.role === 'user' 
                      ? 'bg-indigo-600 text-white rounded-tr-none shadow-md shadow-indigo-200' 
                      : 'bg-white text-slate-800 shadow-sm rounded-tl-none border border-slate-100'
                  }`}>
                    {msg.content}
                  </div>
                </div>
              </div>
            ))}
            {isTyping && (
              <div className="flex justify-start">
                <div className="bg-white p-4 rounded-2xl shadow-sm border border-slate-100 flex items-center gap-3">
                  <Loader2 className="w-4 h-4 text-indigo-600 animate-spin" />
                  <span className="text-xs text-slate-400 font-bold">AI가 데이터를 분석 중입니다...</span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="p-5 border-t border-slate-100 bg-white">
            <div className="flex gap-2 p-1.5 bg-slate-50 border border-slate-200 rounded-2xl focus-within:ring-2 focus-within:ring-indigo-500/20 focus-within:border-indigo-500/50 transition-all">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                placeholder="어떤 통계나 운영 이슈를 도와드릴까요?"
                className="flex-1 bg-transparent border-none outline-none px-3 text-sm font-medium text-slate-700"
              />
              <button
                onClick={handleSend}
                disabled={!input.trim() || isTyping}
                className="w-10 h-10 bg-indigo-600 text-white rounded-xl flex items-center justify-center hover:bg-indigo-700 transition-all active:scale-95 disabled:opacity-50"
              >
                <Send size={18} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AIAssistant;
