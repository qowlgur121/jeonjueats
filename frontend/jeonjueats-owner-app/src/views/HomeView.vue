<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthModal from '../components/AuthModal.vue'

const router = useRouter()
const authStore = useAuthStore()
const showAuthModal = ref(false)

const handleGetStarted = () => {
  if (authStore.isAuthenticated) {
    router.push('/dashboard')
  } else {
    showAuthModal.value = true
  }
}
</script>

<template>
  <div class="home-page">
    <!-- 메인 컨테이너 -->
    <main class="main-container">
      <!-- 로고 섹션 -->
      <div class="logo-section">
        <div class="logo-text">
          <span class="logo-char j">j</span>
          <span class="logo-char e">e</span>
          <span class="logo-char o">o</span>
          <span class="logo-char n">n</span>
          <span class="logo-char j2">j</span>
          <span class="logo-char u">u</span>
          <span class="logo-space"> </span>
          <span class="logo-char e2">e</span>
          <span class="logo-char a">a</span>
          <span class="logo-char t">t</span>
          <span class="logo-char s">s</span>
        </div>
        <span class="owner-badge">사장님</span>
      </div>
      
      <!-- 타이틀 -->
      <h1 class="main-title">
        사장님을 위한<br>
        <span class="title-accent">비즈니스 관리 도구</span>
      </h1>
      
      <!-- 설명 -->
      <p class="main-description">
        온라인 주문부터 매출 관리까지<br>
        스마트한 가게 운영을 시작하세요
      </p>
      
      <!-- 기능 카드들 -->
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🏪</div>
          <h3 class="feature-title">가게 관리</h3>
          <p class="feature-desc">가게 정보 수정과<br>운영상태 관리</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📋</div>
          <h3 class="feature-title">메뉴 관리</h3>
          <p class="feature-desc">메뉴 등록/수정과<br>재고 상태 관리</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📦</div>
          <h3 class="feature-title">주문 관리</h3>
          <p class="feature-desc">실시간 주문 확인과<br>배달 상태 관리</p>
        </div>
      </div>

      <!-- 시작하기 버튼 -->
      <button @click="handleGetStarted" class="start-button">
        {{ authStore.isAuthenticated ? '대시보드로 이동' : '시작하기' }}
      </button>
    </main>

    <!-- 인증 모달 -->
    <AuthModal v-model="showAuthModal" />
  </div>
</template>

<style scoped>
/* 홈 페이지 - 유저 앱과 동일한 구조 */
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 메인 컨테이너 */
.main-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
  text-align: center;
  background-color: transparent;
}

/* 로고 섹션 */
.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
}

.logo-text {
  display: flex;
  align-items: center;
  gap: 1px;
}

.logo-char {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

/* 로고 색상 */
.logo-char.j { color: #8B4513; }
.logo-char.e { color: #8B4513; }
.logo-char.o { color: #8B4513; }
.logo-char.n { color: #FF4500; }
.logo-char.j2 { color: #FFD700; }
.logo-char.u { color: #32CD32; }
.logo-char.e2 { color: #8B4513; }
.logo-char.a { color: #8B4513; }
.logo-char.t { color: #8B4513; }
.logo-char.s { color: #8B4513; }

.logo-space {
  font-size: 32px;
  margin: 0 4px;
}

.owner-badge {
  background: linear-gradient(45deg, #ff6b35, #f7931e);
  color: white;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

/* 메인 타이틀 */
.main-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.2;
  color: #2c3e50;
}

.title-accent {
  background: linear-gradient(45deg, #ff6b35, #f7931e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 설명 */
.main-description {
  font-size: 18px;
  margin-bottom: 40px;
  line-height: 1.5;
  color: #6c757d;
}

/* 기능 카드 그리드 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  margin-bottom: 40px;
  width: 90%;
  max-width: 1200px;
}

.feature-card {
  background: white;
  padding: 32px 20px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  font-size: 40px;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #2c3e50;
}

.feature-desc {
  font-size: 14px;
  color: #6c757d;
  line-height: 1.4;
  margin: 0;
}

/* 시작하기 버튼 */
.start-button {
  padding: 16px 32px;
  background: linear-gradient(45deg, #ff6b35, #f7931e);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.3);
}

.start-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(255, 107, 53, 0.4);
}


/* 반응형 */
@media (max-width: 768px) {
  .main-container {
    width: 100%;
    height: 100vh;
    padding: 0;
  }
  
  .main-title {
    font-size: 32px;
  }
  
  .main-description {
    font-size: 16px;
    margin-bottom: 32px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
    margin-bottom: 32px;
    width: 100%;
  }
  
  .feature-card {
    padding: 24px 16px;
  }
  
  .start-button {
    padding: 14px 28px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .main-container {
    width: 100%;
    height: 100vh;
    padding: 0;
  }
  
  .logo-char {
    font-size: 28px;
  }
  
  .main-title {
    font-size: 28px;
  }
  
  .main-description {
    font-size: 15px;
  }
  
  .features-grid {
    width: 100%;
  }
  
  .feature-card {
    padding: 20px 12px;
  }
  
  .feature-icon {
    font-size: 36px;
  }
}
</style>