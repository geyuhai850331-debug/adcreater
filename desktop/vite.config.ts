import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { mockApiServer } from './mock'

const enableMock = process.env.VITE_ENABLE_MOCK === 'true'

export default defineConfig({
  plugins: [
    vue(),
    // Use real backend by default. Enable mock only when explicitly requested.
    ...(enableMock ? [mockApiServer()] : []),
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
      },
      '/app-api': {
        target: 'http://localhost:48080',
        changeOrigin: true
      },
      '/admin-api': {
        target: 'http://localhost:48080',
        changeOrigin: true
      }
    }
  }
})
