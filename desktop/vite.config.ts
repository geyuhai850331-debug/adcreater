import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { mockApiServer } from './mock'

export default defineConfig({
  plugins: [
    vue(),
    // Mock API server: intercepts /api/* requests when backend is not running.
    // Set env VITE_DISABLE_MOCK=true to bypass mock and use the real backend.
    mockApiServer(),
  ],
  base: './',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:48080',
        changeOrigin: true
      }
    }
  }
})
