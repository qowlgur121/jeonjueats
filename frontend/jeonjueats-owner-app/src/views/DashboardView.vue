<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as storesApi from '../api/stores'

const router = useRouter()
const authStore = useAuthStore()

const stores = ref<any[]>([])
const isLoading = ref(false)
const errorMessage = ref('')

const loadStores = async () => {
  try {
    isLoading.value = true
    stores.value = await storesApi.getOwnerStores()
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/')
  } catch (error) {
    console.error('로그아웃 실패:', error)
  }
}

const goToStores = () => {
  router.push('/stores')
}

const goToStoreMenus = (storeId: number) => {
  router.push(`/stores/${storeId}/menus`)
}

const goToStoreOrders = (storeId: number) => {
  router.push(`/stores/${storeId}/orders`)
}

onMounted(() => {
  loadStores()
})
</script>

<template>
  <div class="dashboard-page">
    <!-- 헤더 -->
    <header class="dashboard-header">
      <div class="container">
        <div class="header-left">
          <div class="logo">전주이츠 사장님</div>
        </div>
        <div class="header-right">
          <div class="user-info">
            <span>{{ authStore.user?.nickname }}님</span>
          </div>
          <button @click="handleLogout" class="btn-logout">
            로그아웃
          </button>
        </div>
      </div>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="main-content">
      <div class="container">
        <!-- 환영 섹션 -->
        <section class="welcome-section">
          <h1 class="welcome-title">
            안녕하세요, {{ authStore.user?.nickname }} 사장님!
          </h1>
          <p class="welcome-description">
            오늘도 좋은 하루 되세요. 가게 운영을 도와드리겠습니다.
          </p>
        </section>

        <!-- 빠른 액션 -->
        <section class="quick-actions">
          <div class="action-cards">
            <div class="action-card" @click="goToStores">
              <div class="card-icon">🏪</div>
              <h3 class="card-title">가게 관리</h3>
              <p class="card-description">가게 정보 수정, 운영상태 변경</p>
            </div>
            
            <div class="action-card" @click="router.push('/profile')">
              <div class="card-icon">👤</div>
              <h3 class="card-title">프로필</h3>
              <p class="card-description">사장님 정보 관리</p>
            </div>
          </div>
        </section>

        <!-- 내 가게 목록 -->
        <section class="stores-section">
          <div class="section-header">
            <h2 class="section-title">내 가게</h2>
            <button @click="goToStores" class="btn-add-store">
              가게 추가
            </button>
          </div>

          <div v-if="isLoading" class="loading">
            가게 정보를 불러오는 중...
          </div>

          <div v-else-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <div v-else-if="stores.length === 0" class="empty-state">
            <div class="empty-icon">🏪</div>
            <h3>등록된 가게가 없습니다</h3>
            <p>첫 번째 가게를 등록해보세요!</p>
            <button @click="goToStores" class="btn-empty-action">
              가게 등록하기
            </button>
          </div>

          <div v-else class="stores-grid">
            <div 
              v-for="store in stores" 
              :key="store.id" 
              class="store-card"
            >
              <div class="store-image">
                <img 
                  v-if="store.storeImageUrl" 
                  :src="store.storeImageUrl" 
                  :alt="store.name"
                >
                <div v-else class="store-placeholder">🏪</div>
              </div>
              
              <div class="store-info">
                <div class="store-header">
                  <h3 class="store-name">{{ store.name }}</h3>
                  <span 
                    class="store-status"
                    :class="{ 'open': store.status === 'OPEN', 'closed': store.status === 'CLOSED' }"
                  >
                    {{ store.status === 'OPEN' ? '영업중' : '영업종료' }}
                  </span>
                </div>
                
                <p class="store-category">{{ store.categoryName }}</p>
                <p class="store-description">{{ store.description }}</p>
                
                <div class="store-actions">
                  <button 
                    @click="goToStoreMenus(store.id)"
                    class="btn-action"
                  >
                    메뉴 관리
                  </button>
                  <button 
                    @click="goToStoreOrders(store.id)"
                    class="btn-action"
                  >
                    주문 관리
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  background-color: #f8fafc;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 헤더 */
.dashboard-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 0;
}

.dashboard-header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1e40af;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info span {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.btn-logout {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

/* 메인 콘텐츠 */
.main-content {
  padding: 32px 0;
}

/* 환영 섹션 */
.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.welcome-description {
  font-size: 16px;
  color: #64748b;
}

/* 빠른 액션 */
.quick-actions {
  margin-bottom: 48px;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e2e8f0;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.card-description {
  font-size: 14px;
  color: #64748b;
}

/* 가게 섹션 */
.stores-section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.btn-add-store {
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-add-store:hover {
  background: #2563eb;
}

/* 로딩 & 에러 */
.loading,
.error-message {
  text-align: center;
  padding: 40px;
  color: #64748b;
}

.error-message {
  color: #dc2626;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}

.btn-empty-action {
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-empty-action:hover {
  background: #2563eb;
}

/* 가게 그리드 */
.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.store-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.store-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.store-image {
  height: 160px;
  overflow: hidden;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-placeholder {
  font-size: 48px;
  color: #cbd5e1;
}

.store-info {
  padding: 20px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.store-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.store-status.open {
  background: #dcfce7;
  color: #166534;
}

.store-status.closed {
  background: #fee2e2;
  color: #991b1b;
}

.store-category {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.store-description {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
  line-height: 1.5;
}

.store-actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  flex: 1;
  padding: 8px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action:hover {
  background: #e2e8f0;
  color: #475569;
}

/* 반응형 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 24px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stores-grid {
    grid-template-columns: 1fr;
  }
}
</style>