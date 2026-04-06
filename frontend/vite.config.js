import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/users': 'http://localhost:8080',
      '/services': 'http://localhost:8080',
      '/bookings': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080',
    },
  },
})
