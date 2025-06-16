<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  try {
    await authStore.logout()
    // 로그아웃 후 바로 로그인 페이지로 이동
    router.push('/login')
  } catch (error) {
    console.error('로그아웃 실패:', error)
    // 에러가 발생해도 로그인 페이지로 이동
    router.push('/login')
  }
}
</script>

<template>
  <!-- 헤더 -->
  <header class="header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <div class="logo-text">
            <span class="logo-char c1">j</span>
            <span class="logo-char c2">e</span>
            <span class="logo-char c3">o</span>
            <span class="logo-char c4">n</span>
            <span class="logo-char c5">j</span>
            <span class="logo-char c6">u</span>
            <span class="logo-space"> </span>
            <span class="logo-char c1">e</span>
            <span class="logo-char c2">a</span>
            <span class="logo-char c3">t</span>
            <span class="logo-char c1">s</span>
          </div>
          <span class="owner-badge">사장님</span>
        </router-link>
      </div>
      <div class="header-right">
        <div class="user-info">
          <span class="user-name">{{ authStore.user?.nickname }}님</span>
          <button @click="handleLogout" class="btn-logout">로그아웃</button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: white;
  height: 64px;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
}

.logo-char.c1 { color: #8B4513; }
.logo-char.c2 { color: #8B4513; }
.logo-char.c3 { color: #8B4513; }
.logo-char.c4 { color: #FF4500; }
.logo-char.c5 { color: #FFD700; }
.logo-char.c6 { color: #5DADE2; }

.owner-badge {
  background: #3b82f6;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #374151;
  font-weight: 500;
}

.btn-logout {
  background: #ef4444;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-logout:hover {
  background: #dc2626;
}
</style>