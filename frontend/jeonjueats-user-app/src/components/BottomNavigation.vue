<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthModal from './AuthModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showAuthModal = ref(false)

const navItems = [
  {
    name: '홈',
    icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6',
    route: '/',
    tab: 'home',
    requiresAuth: false
  },
  {
    name: '검색',
    icon: 'M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z',
    route: '/search',
    tab: 'search',
    requiresAuth: false
  },
  {
    name: '즐겨찾기',
    icon: 'M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z',
    route: '/wishlist',
    tab: 'wishlist',
    requiresAuth: true
  },
  {
    name: '주문내역',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
    route: '/orders',
    tab: 'orders',
    requiresAuth: true
  },
  {
    name: 'My 이츠',
    icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z',
    route: '/mypage',
    tab: 'mypage',
    requiresAuth: true
  }
]

const currentTab = computed(() => route.meta.tab)
const shouldShow = computed(() => !route.meta.hideBottomNav)

// 탭 클릭 핸들러
const handleNavClick = (item: typeof navItems[0], event: Event) => {
  event.preventDefault()
  
  console.log('네비게이션 클릭:', { 
    item: item.name, 
    requiresAuth: item.requiresAuth, 
    isAuthenticated: authStore.isAuthenticated,
    token: authStore.token,
    user: authStore.user
  })
  
  // 인증이 필요한 탭이고 로그인되지 않은 경우
  if (item.requiresAuth && !authStore.isAuthenticated) {
    console.log('인증 필요 - 모달 표시')
    showAuthModal.value = true
    return
  }
  
  console.log('라우터 이동:', item.route)
  // 인증된 경우나 인증이 필요없는 탭은 정상 이동
  router.push(item.route)
}

// URL 쿼리로 인증 모달 표시 체크
watch(() => route.query.auth, (authQuery) => {
  if (authQuery === 'required') {
    showAuthModal.value = true
  }
}, { immediate: true })
</script>

<template>
  <nav v-if="shouldShow" class="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 z-50">
    <div class="w-full">
      <div class="flex justify-around items-center px-4 py-2">
        <a
          v-for="item in navItems"
          :key="item.tab"
          href="#"
          @click="handleNavClick(item, $event)"
          :class="[
            'flex flex-col items-center py-2 px-4 text-sm transition-colors duration-200 min-w-0',
            currentTab === item.tab 
              ? 'text-primary-500' 
              : 'text-gray-400 hover:text-gray-600'
          ]"
        >
          <svg 
            :class="[
              'w-7 h-7 mb-1',
              currentTab === item.tab ? 'text-primary-500' : 'text-gray-400'
            ]" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path 
              stroke-linecap="round" 
              stroke-linejoin="round" 
              stroke-width="2" 
              :d="item.icon"
            />
          </svg>
          <span :class="{ 'font-medium': currentTab === item.tab }" class="text-xs">
            {{ item.name }}
          </span>
        </a>
      </div>
    </div>
  </nav>

  <!-- 인증 모달 -->
  <AuthModal v-model="showAuthModal" />

</template> 