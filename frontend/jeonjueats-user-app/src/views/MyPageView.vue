<script setup lang="ts">
import { ref } from 'vue'

const user = ref({
  name: '김전주',
  email: 'jeonju@example.com',
  phone: '010-1234-5678',
  address: '전주시 완산구 한옥마을',
  joinDate: '2024-01-01'
})

const stats = ref({
  points: 15500,
  coupons: 3,
  orders: 42,
  reviews: 12
})

const menuItems = [
  { name: '프로필 수정', icon: '👤', route: '/profile', description: '개인정보 변경' },
  { name: '주문내역', icon: '📋', route: '/orders', description: '주문 및 배송 현황' },
  { name: '즐겨찾기', icon: '❤️', route: '/wishlist', description: '관심 매장 관리' },
  { name: '쿠폰함', icon: '🎫', route: '/coupons', description: '할인쿠폰 확인' },
  { name: '이벤트', icon: '🎉', route: '/events', description: '진행중인 혜택' },
  { name: '고객센터', icon: '💬', route: '/support', description: '문의 및 신고' },
  { name: '설정', icon: '⚙️', route: '/settings', description: '알림 및 환경설정' }
]

const logout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('로그아웃')
    // 로그아웃 로직 추가
  }
}
</script>

<template>
  <!-- My이츠 페이지 -->
  <div class="mypage">
    
    <!-- 헤더 섹션 -->
    <section class="header-section">
      <div class="section-container">
        <div class="header-content">
          <h1 class="page-title">My이츠</h1>
          <p class="page-subtitle">나의 배달 라이프를 관리해보세요</p>
        </div>
      </div>
    </section>

    <!-- 프로필 컨텐츠 -->
    <div class="profile-content">
      
      <!-- 사용자 프로필 섹션 -->
      <section class="profile-section">
        <div class="section-container">
          <div class="profile-card">
            
            <!-- 프로필 정보 -->
            <div class="profile-info">
              <div class="profile-avatar">
                <span class="avatar-emoji">👤</span>
              </div>
              <div class="profile-details">
                <h2 class="user-name">{{ user.name }}</h2>
                <p class="user-email">{{ user.email }}</p>
                <p class="user-info">{{ user.phone }} · {{ user.address }}</p>
                <p class="join-date">{{ user.joinDate }} 가입</p>
              </div>
              <button class="edit-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
                </svg>
              </button>
            </div>

            <!-- 통계 정보 -->
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon">💰</div>
                <div class="stat-details">
                  <div class="stat-value">{{ stats.points.toLocaleString() }}원</div>
                  <div class="stat-label">적립금</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">🎫</div>
                <div class="stat-details">
                  <div class="stat-value">{{ stats.coupons }}장</div>
                  <div class="stat-label">쿠폰</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">📦</div>
                <div class="stat-details">
                  <div class="stat-value">{{ stats.orders }}회</div>
                  <div class="stat-label">주문</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">⭐</div>
                <div class="stat-details">
                  <div class="stat-value">{{ stats.reviews }}개</div>
                  <div class="stat-label">리뷰</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 메뉴 섹션 -->
      <section class="menu-section">
        <div class="section-container">
          <div class="menu-card">
            <h3 class="menu-title">서비스 메뉴</h3>
            
            <div class="menu-list">
              <router-link 
                v-for="(item, index) in menuItems" 
                :key="item.name"
                :to="item.route"
                class="menu-item"
              >
                <div class="menu-icon">{{ item.icon }}</div>
                <div class="menu-content">
                  <div class="menu-name">{{ item.name }}</div>
                  <div class="menu-description">{{ item.description }}</div>
                </div>
                <div class="menu-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- 계정 관리 섹션 -->
      <section class="account-section">
        <div class="section-container">
          <div class="account-card">
            <button @click="logout" class="logout-btn">
              <div class="logout-icon">🚪</div>
              <span>로그아웃</span>
            </button>
            
            <div class="app-info">
              <p class="app-version">JeonjuEats v1.0.0</p>
              <p class="app-copyright">© 2024 JeonjuEats. All rights reserved.</p>
            </div>
          </div>
        </div>
      </section>
      
    </div>

  </div>
</template>

<style scoped>
/* My이츠 페이지 컨테이너 */
.mypage {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 공통 스타일 */
.section-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 4rem;
}

/* 헤더 섹션 */
.header-section {
  width: 100%;
  background-color: white;
  padding: 2.5rem 0;
  border-bottom: 1px solid #f1f3f4;
}

.header-content {
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: #6b7280;
}

/* 프로필 컨텐츠 */
.profile-content {
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 프로필 섹션 */
.profile-section {
  background-color: white;
}

.profile-card {
  padding: 2rem 0;
}

/* 프로필 정보 */
.profile-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #f3f4f6;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #e5e7eb;
}

.avatar-emoji {
  font-size: 32px;
}

.profile-details {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-email {
  font-size: 16px;
  color: #374151;
  margin-bottom: 4px;
}

.user-info {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.join-date {
  font-size: 12px;
  color: #9ca3af;
}

.edit-btn {
  width: 40px;
  height: 40px;
  background-color: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-btn:hover {
  background-color: #f1f3f4;
  border-color: #d1d5db;
}

.edit-btn svg {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

/* 통계 그리드 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background-color: #f1f3f4;
  transform: translateY(-1px);
}

.stat-icon {
  font-size: 20px;
  width: 32px;
  text-align: center;
}

.stat-details {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

/* 메뉴 섹션 */
.menu-section {
  background-color: white;
}

.menu-card {
  padding: 2rem 0;
}

.menu-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.5rem;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #f3f4f6;
  text-decoration: none;
  transition: all 0.2s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background-color: #f8f9fa;
  margin: 0 -1rem;
  padding: 1rem;
  border-radius: 8px;
}

.menu-icon {
  width: 40px;
  height: 40px;
  background-color: #f8f9fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.menu-content {
  flex: 1;
}

.menu-name {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.menu-description {
  font-size: 14px;
  color: #6b7280;
}

.menu-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-arrow svg {
  width: 16px;
  height: 16px;
  color: #9ca3af;
}

/* 계정 관리 섹션 */
.account-section {
  background-color: white;
}

.account-card {
  padding: 2rem 0;
  text-align: center;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  max-width: 300px;
  margin: 0 auto 2rem;
  padding: 12px 24px;
  background-color: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background-color: #f1f3f4;
  border-color: #d1d5db;
}

.logout-icon {
  font-size: 16px;
}

.app-info {
  padding-top: 2rem;
  border-top: 1px solid #f3f4f6;
}

.app-version {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.app-copyright {
  font-size: 12px;
  color: #9ca3af;
}

/* 반응형 */
@media (max-width: 768px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .profile-info {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .edit-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
  }
  
  .page-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 