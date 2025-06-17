import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '홈', tab: 'home' }
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/SearchView.vue'),
      meta: { title: '검색', tab: 'search' }
    },
    {
      path: '/stores/:id',
      name: 'store-detail',
      component: () => import('../views/StoreDetailView.vue'),
      meta: { title: '가게 상세' }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue'),
      meta: { title: '장바구니', requiresAuth: true }
    },
    {
      path: '/order',
      name: 'checkout',
      component: () => import('../views/CheckoutView.vue'),
      meta: { title: '주문하기', requiresAuth: true }
    },
    {
      path: '/wishlist',
      name: 'wishlist',
      component: () => import('../views/WishlistView.vue'),
      meta: { title: '찜', tab: 'wishlist', requiresAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrdersView.vue'),
      meta: { title: '주문내역', tab: 'orders', requiresAuth: true }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/OrderDetailView.vue'),
      meta: { title: '주문 상세', requiresAuth: true }
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/MyPageView.vue'),
      meta: { title: 'My 이츠', tab: 'mypage', requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '로그인' },
      beforeEnter: (to, from, next) => {
        const authStore = useAuthStore()
        // 이미 로그인된 사용자는 홈으로 리다이렉트
        if (authStore.isAuthenticated) {
          next({ name: 'home' })
        } else {
          next()
        }
      }
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignupView.vue'),
      meta: { title: '회원가입' }
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
  ]
})

// 라우터 가드
router.beforeEach((to, from, next) => {
  // 페이지 제목 설정
  document.title = to.meta.title ? `${to.meta.title} - 전주이츠` : '전주이츠'
  
  // 인증이 필요한 페이지 체크
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      // 리다이렉트 경로 저장
      authStore.setRedirectPath(to.fullPath)
      // 로그인 페이지로 이동
      next({ name: 'login' })
      return
    }
  }
  
  next()
})

export default router
