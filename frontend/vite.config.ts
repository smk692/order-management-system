import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, '.', '');
    return {
      server: {
        port: 3000,
        host: '0.0.0.0',
        proxy: {
          '/api': {
            target: env.VITE_API_BASE_URL || 'http://localhost:3001',
            changeOrigin: true,
          },
        },
      },
      plugins: [react()],
      // REMOVED: process.env.API_KEY, process.env.GEMINI_API_KEY
      // API key는 이제 백엔드에서만 사용
      define: {
        __APP_ENV__: JSON.stringify(env.VITE_APP_ENV || 'development'),
      },
      resolve: {
        alias: {
          '@': path.resolve(__dirname, '.'),
        }
      }
    };
});
