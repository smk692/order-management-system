import { useEffect, useCallback } from 'react';
import { useUIStore } from '../stores';

export const useKeyboardShortcuts = () => {
  const { setGlobalSearchOpen } = useUIStore();

  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if ((event.metaKey || event.ctrlKey) && event.key === 'k') {
      event.preventDefault();
      setGlobalSearchOpen(true);
    }
  }, [setGlobalSearchOpen]);

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);
};
