<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <!-- 메인 네비게이션 -->
  <nav class="nav-container">
    <div class="nav-menu">
      <button 
        class="nav-item"
        :class="{ active: $route.path === '/dashboard' }"
        @click="router.push('/dashboard')"
      >
        대시보드
      </button>
      <button 
        class="nav-item"
        :class="{ active: $route.path === '/stores' }"
        @click="router.push('/stores')"
      >
        가게 관리
      </button>
      <button 
        class="nav-item"
        :class="{ active: $route.path === '/menus' }"
        @click="router.push('/menus')"
      >
        메뉴 관리
      </button>
      <button 
        class="nav-item"
        :class="{ active: $route.path === '/orders' }"
        @click="router.push('/orders')"
      >
        주문 관리
      </button>
      <button 
        class="nav-item"
        :class="{ active: $route.path === '/profile' }"
        @click="router.push('/profile')"
      >
        프로필
      </button>
    </div>
  </nav>
</template>

<style scoped>
.nav-container {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 64px;
  z-index: 40;
}

.nav-menu {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  padding: 0 1rem;
  justify-content: space-between;
  width: 100%;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 12px;
  background: none;
  border: none;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
  flex: 1;
  white-space: nowrap;
}

/* 모바일 대응 */
@media (max-width: 640px) {
  .nav-menu {
    padding: 0 0.5rem;
  }
  
  .nav-item {
    padding: 16px 8px;
    font-size: 13px;
  }
}

.nav-item:hover {
  color: #3b82f6;
  background-color: #f8fafc;
}

.nav-item.active {
  color: #3b82f6;
  border-bottom-color: #3b82f6;
}
</style>