<script setup lang="ts">
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
            <span class="logo-char c1">J</span>
            <span class="logo-char c2">e</span>
            <span class="logo-char c3">o</span>
            <span class="logo-char c4">n</span>
            <span class="logo-char c5">j</span>
            <span class="logo-char c6">u</span>
            <span class="logo-char c1">E</span>
            <span class="logo-char c2">a</span>
            <span class="logo-char c3">t</span>
            <span class="logo-char c4">s</span>
          </div>
        </router-link>
      </div>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="main">
      <router-view />
    </main>

    <!-- 하단 네비게이션 -->
    <nav class="bottom-nav">
      <div class="nav-container">
        
        <!-- 홈 -->
        <router-link to="/" class="nav-item">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
            </svg>
          </div>
          <span class="nav-text">홈</span>
        </router-link>

        <!-- 검색 -->
        <router-link to="/search" class="nav-item">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
            </svg>
          </div>
          <span class="nav-text">검색</span>
        </router-link>

        <!-- 즐겨찾기 -->
        <router-link to="/wishlist" class="nav-item">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </div>
          <span class="nav-text">즐겨찾기</span>
        </router-link>

        <!-- 주문내역 -->
        <router-link to="/orders" class="nav-item">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
          </div>
          <span class="nav-text">주문내역</span>
        </router-link>

        <!-- My이츠 -->
        <router-link to="/myeats" class="nav-item">
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </div>
          <span class="nav-text">My이츠</span>
        </router-link>

      </div>
    </nav>

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
  display: flex;
  align-items: center;
  gap: 1px;
}

.logo-char {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

/* JeonjuEats 색상 (갈색, 빨강, 노랑, 연두, 초록, 갈색) */
.c1 { color: #8B4513; } /* 갈색 */
.c2 { color: #FF0000; } /* 빨강 */
.c3 { color: #FFD700; } /* 노랑 */
.c4 { color: #9ACD32; } /* 연두 */
.c5 { color: #32CD32; } /* 초록 */
.c6 { color: #8B4513; } /* 갈색 */

/* 메인 콘텐츠 - 100% 너비 */
.main {
  flex: 1;
  width: 100%;
  padding-bottom: 72px;
}

/* 하단 네비게이션 - 100% 너비 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  background-color: white;
  border-top: 1px solid #e9ecef;
  z-index: 100;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.1);
}

.nav-container {
  width: 100%;
  padding: 0 4rem;
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 72px;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #9ca3af;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
  min-width: 60px;
}

.nav-item:hover {
  color: #374151;
  background-color: #f9fafb;
}

.nav-item.router-link-active {
  color: #374151;
}

.nav-icon {
  width: 24px;
  height: 24px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
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
  
  .nav-container {
    padding: 0 2rem;
    height: 64px;
  }
  
  .nav-item {
    padding: 6px 12px;
    min-width: 50px;
  }
  
  .nav-icon {
    width: 20px;
    height: 20px;
  }
  
  .nav-text {
    font-size: 11px;
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

