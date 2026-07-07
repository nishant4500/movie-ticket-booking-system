import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'https://movie-ticket-booking-system-production-0651.up.railway.app',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
