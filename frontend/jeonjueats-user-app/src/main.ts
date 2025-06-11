import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Auth store 초기화
const authStore = useAuthStore()
authStore.initialize().then(() => {
  console.log('Auth store 초기화 완료')
  console.log('현재 로그인 상태:', authStore.isAuthenticated)
  console.log('현재 사용자:', authStore.user)
})

app.mount('#app')
