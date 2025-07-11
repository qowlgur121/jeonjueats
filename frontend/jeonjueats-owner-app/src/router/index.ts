import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory('/owner/'),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignupView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stores',
      name: 'stores',
      component: () => import('../views/StoresView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/menus',
      name: 'menus',
      component: () => import('../views/MenusView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrdersView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/privacy-policy',
      name: 'privacy-policy',
      component: () => import('../views/PrivacyPolicyView.vue'),
      meta: { title: '개인정보 처리방침' }
    },
    {
      path: '/terms-of-service',
      name: 'terms-of-service',
      component: () => import('../views/TermsOfServiceView.vue'),
      meta: { title: '이용약관' }
    },
    {
      path: '/marketing-policy',
      name: 'marketing-policy',
      component: () => import('../views/MarketingPolicyView.vue'),
      meta: { title: '마케팅 정보 수신 동의' }
    }
  ],
})

// 라우터 가드
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  
  // 토큰이 있으면 유효성 검증
  if (authStore.token && !authStore.user) {
    await authStore.initialize()
  }
  
  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next('/login')
  }
  
  // 게스트만 접근 가능한 페이지 (로그인/회원가입)
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    return next('/dashboard')
  }
  
  next()
})

export default router
