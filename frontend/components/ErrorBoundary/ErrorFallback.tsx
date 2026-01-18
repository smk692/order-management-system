import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface Props {
  error?: Error;
  onRetry?: () => void;
}

const ErrorFallback: React.FC<Props> = ({ error, onRetry }) => (
  <div className="flex flex-col items-center justify-center min-h-[400px] p-8 text-center">
    <div className="w-16 h-16 bg-rose-50 rounded-full flex items-center justify-center mb-6">
      <AlertTriangle className="w-8 h-8 text-rose-500" />
    </div>
    <h2 className="text-xl font-bold text-slate-900 mb-2">오류가 발생했습니다</h2>
    <p className="text-slate-500 mb-6 max-w-md">{error?.message || '예기치 않은 오류'}</p>
    {onRetry && (
      <button onClick={onRetry} className="flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-xl font-bold">
        <RefreshCw size={18} /> 다시 시도
      </button>
    )}
  </div>
);

export default ErrorFallback;
