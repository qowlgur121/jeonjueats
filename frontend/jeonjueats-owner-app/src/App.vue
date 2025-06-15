<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { onMounted, computed } from 'vue'
import { useAuthStore } from './stores/auth'
import AppHeader from '@/components/AppHeader.vue'
import NavigationBar from '@/components/NavigationBar.vue'

const authStore = useAuthStore()
const route = useRoute()

// 헤더와 네비게이션을 표시할 페이지 판단
// 로그인, 회원가입, 약관 페이지에서는 헤더/네비게이션 숨김
const showHeaderAndNav = computed(() => {
  const protectedRoutes = ['dashboard', 'stores', 'menus', 'orders', 'profile']
  return protectedRoutes.includes(route.name as string)
})

onMounted(async () => {
  await authStore.initialize()
})
</script>

<template>
  <!-- 전주이츠 사장님 앱 -->
  <div class="app">
    <!-- 헤더와 네비게이션을 최상위에 고정 -->
    <!-- 인증이 필요한 페이지에서만 표시됨 -->
    <AppHeader v-if="showHeaderAndNav" />
    <NavigationBar v-if="showHeaderAndNav" />
    
    <!-- 페이지 컨텐츠 영역 -->
    <!-- showHeaderAndNav가 true일 때만 margin-top 적용 -->
    <div :class="{ 'main-content': showHeaderAndNav }">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.app {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 헤더(64px) + 네비게이션(64px) = 128px 만큼 여백 추가 */
.main-content {
}
</style>
