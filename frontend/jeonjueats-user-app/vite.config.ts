import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/jeonjueats\.me\/api\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              networkTimeoutSeconds: 10,
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          }
        ]
      },
      manifest: {
        id: 'com.jeonjueats.app',
        name: '전주이츠 - 전주 맛집 배달',
        short_name: '전주이츠',
        description: '전주 지역 맛집과 고객을 연결하는 배달 주문 플랫폼',
        theme_color: '#00D0FF',
        background_color: '#ffffff',
        display: 'standalone',
        start_url: '/',
        scope: '/',
        orientation: 'portrait',
        lang: 'ko',
        categories: ['food', 'shopping'],
        icons: [
          {
            src: '/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          },
          {
            src: '/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable'
          }
        ],
        screenshots: [
          {
            src: '/screenshots/home.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '전주이츠 홈 화면'
          },
          {
            src: '/screenshots/store-detail.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '가게 상세 페이지'
          },
          {
            src: '/screenshots/cart.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '장바구니'
          },
          {
            src: '/screenshots/order.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '주문 화면'
          },
          {
            src: '/screenshots/mypage.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '마이페이지'
          },
          {
            src: '/screenshots/order-history.png',
            sizes: '1080x1920',
            type: 'image/png',
            form_factor: 'narrow',
            label: '주문 내역'
          }
        ]
      }
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    watch: {
      usePolling: true
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  optimizeDeps: {
    force: true
  },
  preview: {
    host: '0.0.0.0',
    port: 3000
  }
})
