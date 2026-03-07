import React from 'react';
import { Loader2 } from 'lucide-react';

const ViewLoader: React.FC = () => (
  <div className="flex flex-col items-center justify-center min-h-[400px]">
    <Loader2 className="w-12 h-12 text-indigo-600 animate-spin mb-4" />
    <p className="text-sm font-bold text-slate-400 uppercase tracking-widest">Loading...</p>
  </div>
);

export default ViewLoader;
