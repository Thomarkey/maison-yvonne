import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  // Bundle lands in the backend's resources, served from classpath:/static.
  build: { outDir: '../backend/build/frontend/static', emptyOutDir: true },
  server: { proxy: { '/api': 'http://localhost:8080' } },
})
