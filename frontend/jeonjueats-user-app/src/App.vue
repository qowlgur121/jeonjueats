<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import BottomNavigation from './components/BottomNavigation.vue'
import AuthModal from './components/AuthModal.vue'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const isAuthModalVisible = ref(false)

// 앱 시작시 인증 상태 초기화
onMounted(() => {
  authStore.initialize()
})

// 라우트 변경 감지하여 인증 모달 표시
watch(
  () => route.query.auth,
  (authQuery) => {
    if (authQuery === 'required') {
      isAuthModalVisible.value = true
    }
  },
  { immediate: true }
)

// 인증 모달 닫기
const closeAuthModal = () => {
  isAuthModalVisible.value = false
}
</script>

<template>
  <!-- 전주이츠 웹 애플리케이션 -->
  <div class="app">
    
    <!-- 헤더 -->
    <header class="header">
      <div class="header-container">
        <router-link to="/" class="logo">
          <!-- JeonjuEats 로고 -->
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
        </router-link>
      </div>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 하단 네비게이션 -->
    <BottomNavigation />

    <!-- 인증 모달 -->
    <AuthModal 
      v-model="isAuthModalVisible" 
      @update:modelValue="closeAuthModal"
    />

  </div>
</template>

<style scoped>
/* 전체 앱 컨테이너 - 진짜 100% 화면 */
.app {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
}

/* 헤더 - 쿠팡이츠 스타일로 더 심플하게 */
.header {
  width: 100%;
  background-color: white;
  border-bottom: 1px solid #e9ecef;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header-container {
  width: 100%;
  padding: 0 4rem;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  text-decoration: none;
}

/* JeonjuEats 컬러풀 로고 */
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

/* 메인 콘텐츠 - 100% 너비 */
.main {
  flex: 1;
  width: 100%;
  padding-bottom: 72px;
}


/* 반응형 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 2rem;
    height: 52px;
  }
  
  .logo-char {
    font-size: 20px;
  }
  
  .main {
    padding-bottom: 64px;
  }
}

/* 큰 화면에서 더 여유롭게 */
@media (min-width: 1200px) {
  .header-container {
    padding: 0 6rem;
  }
  
  .nav-container {
    padding: 0 6rem;
  }
}

@media (min-width: 1600px) {
  .header-container {
    padding: 0 8rem;
  }
  
  .nav-container {
    padding: 0 8rem;
  }
}
</style>

