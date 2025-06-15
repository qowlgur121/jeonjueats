<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as storesApi from '../api/stores'
import apiClient from '../api/client'

interface Store {
  storeId: number
  name: string
  categoryId: number
  categoryName: string
  status: string
  storeImageUrl: string | null
  description: string
  address1: string
  address2: string
  zipcode: string
  phoneNumber: string
  minOrderAmount: number
  deliveryFee: number
  averageRating: number
  reviewCount: number
  createdAt: string
  updatedAt: string
}

interface DashboardStats {
  todayOrders: number
  todaySales: number
  weeklyOrders: number
  weeklySales: number
  pendingOrders: number
  completedOrders: number
}

const router = useRouter()

const stores = ref<Store[]>([])
const selectedStore = ref<Store | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')
const dashboardStats = ref<DashboardStats>({
  todayOrders: 0,
  todaySales: 0,
  weeklyOrders: 0,
  weeklySales: 0,
  pendingOrders: 0,
  completedOrders: 0
})

const loadStores = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const response = await storesApi.getOwnerStores()
    stores.value = response
    
    if (stores.value.length > 0) {
      selectedStore.value = stores.value[0]
    }
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

const loadDashboardStats = async (storeId: number) => {
  try {
    const today = new Date()
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - 7)
    
    // 전체 주문 조회 (최근 주 단위)
    const ordersResponse = await apiClient.get(`/api/owner/stores/${storeId}/orders`, {
      params: {
        size: 100 // 최근 100건 조회
      }
    })
    
    const orders = ordersResponse.data.content || []
    
    // 오늘 날짜 기준으로 필터링
    const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate())
    const weekStart = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
    
    let todayOrders = 0
    let todaySales = 0
    let weeklyOrders = 0
    let weeklySales = 0
    let pendingOrders = 0
    let completedOrders = 0
    
    orders.forEach((order: any) => {
      const orderDate = new Date(order.createdAt)
      const totalAmount = order.totalAmount || 0
      
      // 주간 통계
      if (orderDate >= weekStart) {
        weeklyOrders++
        weeklySales += totalAmount
      }
      
      // 오늘 통계
      if (orderDate >= todayStart) {
        todayOrders++
        todaySales += totalAmount
      }
      
      // 주문 상태별 통계
      if (order.status === 'PENDING' || order.status === 'ACCEPTED') {
        pendingOrders++
      } else if (order.status === 'COMPLETED') {
        completedOrders++
      }
    })
    
    dashboardStats.value = {
      todayOrders,
      todaySales,
      weeklyOrders,
      weeklySales,
      pendingOrders,
      completedOrders
    }
  } catch (error: any) {
    console.error('대시보드 통계 조회 실패:', error)
    // 에러 시 기본값 유지
  }
}

const selectStore = (store: Store) => {
  selectedStore.value = store
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

const formatNumber = (num: number) => {
  return new Intl.NumberFormat('ko-KR').format(num)
}



// 선택된 가게가 변경될 때마다 통계 다시 로드
watch(selectedStore, (newStore) => {
  if (newStore?.storeId) {
    loadDashboardStats(newStore.storeId)
  }
}, { immediate: false })

onMounted(async () => {
  await loadStores()
  // 첫 번째 가게가 선택되면 자동으로 통계 로드
  if (selectedStore.value?.storeId) {
    await loadDashboardStats(selectedStore.value.storeId)
  }
})
</script>

<template>
  <div class="dashboard-page">
    <!-- 메인 콘텐츠 -->
    <main class="main-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>가게 정보를 불러오는 중...</p>
      </div>

      <div v-else-if="errorMessage" class="error-state">
        <p>{{ errorMessage }}</p>
        <button @click="loadStores" class="btn-retry">다시 시도</button>
      </div>

      <div v-else-if="stores.length === 0" class="empty-state">
        <div class="empty-content">
          <h3>등록된 가게가 없습니다</h3>
          <p>첫 번째 가게를 등록하고 운영을 시작해보세요</p>
          <button @click="router.push('/stores')" class="btn-primary">가게 등록하기</button>
        </div>
      </div>

      <div v-else class="dashboard-content">
        <!-- 가게 선택기 -->
        <div class="store-selector">
          <h2 class="section-title">가게 선택</h2>
          <div class="store-select-grid">
            <button
              v-for="store in stores"
              :key="store.storeId"
              @click="selectStore(store)"
              class="store-select-item"
              :class="{ active: selectedStore?.storeId === store.storeId }"
            >
              <div class="store-image">
                <img v-if="store.storeImageUrl" :src="store.storeImageUrl" :alt="store.name">
                <div v-else class="store-placeholder">🏪</div>
              </div>
              <div class="store-info">
                <h4>{{ store.name }}</h4>
                <p>{{ store.categoryName }}</p>
                <span class="status" :class="store.status.toLowerCase()">
                  {{ store.status === 'OPEN' ? '영업중' : '영업종료' }}
                </span>
              </div>
            </button>
          </div>
        </div>

        <!-- 선택된 가게의 대시보드 -->
        <div v-if="selectedStore" class="dashboard-main">
          <!-- 매출 요약 -->
          <section class="revenue-section">
            <h2 class="section-title">매출 현황</h2>
            <div class="revenue-cards">
              <div class="revenue-card">
                <div class="revenue-label">오늘 매출</div>
                <div class="revenue-amount">{{ formatCurrency(dashboardStats.todaySales) }}</div>
                <div class="revenue-orders">{{ dashboardStats.todayOrders }}건 주문</div>
              </div>
              <div class="revenue-card">
                <div class="revenue-label">이번 주 매출</div>
                <div class="revenue-amount">{{ formatCurrency(dashboardStats.weeklySales) }}</div>
                <div class="revenue-orders">{{ dashboardStats.weeklyOrders }}건 주문</div>
              </div>
            </div>
          </section>

          <!-- 주문 현황 -->
          <section class="orders-section">
            <h2 class="section-title">주문 현황</h2>
            <div class="orders-summary">
              <div class="order-status-card pending">
                <div class="status-number">{{ dashboardStats.pendingOrders }}</div>
                <div class="status-label">처리 대기</div>
              </div>
              <div class="order-status-card completed">
                <div class="status-number">{{ dashboardStats.completedOrders }}</div>
                <div class="status-label">완료된 주문</div>
              </div>
            </div>
          </section>

          <!-- 빠른 액션 -->
          <section class="quick-actions">
            <h2 class="section-title">빠른 작업</h2>
            <div class="action-grid">
              <button @click="router.push('/menus')" class="action-btn">
                <span class="action-icon">📋</span>
                <span class="action-text">메뉴 관리</span>
              </button>
              <button @click="router.push('/orders')" class="action-btn">
                <span class="action-icon">📦</span>
                <span class="action-text">주문 관리</span>
              </button>
              <button @click="router.push('/stores')" class="action-btn">
                <span class="action-icon">⚙️</span>
                <span class="action-text">가게 설정</span>
              </button>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 전체 페이지 */
.dashboard-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}


/* 메인 콘텐츠 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
}

/* 상태 */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #dc2626;
}

.btn-retry {
  padding: 8px 16px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 1rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.empty-content {
  text-align: center;
}

.empty-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-content p {
  color: #6b7280;
  margin-bottom: 24px;
}

.btn-primary {
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

.btn-primary:hover {
  background: #2563eb;
}

/* 대시보드 콘텐츠 */
.dashboard-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
  align-items: start;
}

/* 가게 선택기 */
.store-selector {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  position: sticky;
  top: 150px;
  max-height: calc(100vh - 160px);
  overflow-y: auto;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.store-select-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.store-select-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: none;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  width: 100%;
}

.store-select-item:hover {
  border-color: #d1d5db;
  background: #f9fafb;
}

.store-select-item.active {
  border-color: #3b82f6;
  background: #eff6ff;
}

.store-image {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-placeholder {
  font-size: 20px;
  color: #d1d5db;
}

.store-info h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.store-info p {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0 0 4px 0;
}

.status {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}

.status.open {
  background: #d1fae5;
  color: #065f46;
}

.status.closed {
  background: #fee2e2;
  color: #991b1b;
}

/* 대시보드 메인 */
.dashboard-main {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 매출 섹션 */
.revenue-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
}

.revenue-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.revenue-card {
  padding: 1.5rem;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.revenue-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.revenue-amount {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.revenue-orders {
  font-size: 12px;
  color: #64748b;
}

/* 주문 섹션 */
.orders-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
}

.orders-summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.order-status-card {
  text-align: center;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.order-status-card.pending {
  background: #fef3c7;
  border-color: #fbbf24;
}

.order-status-card.completed {
  background: #d1fae5;
  border-color: #10b981;
}

.status-number {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.status-label {
  font-size: 14px;
  color: #6b7280;
}

/* 빠른 액션 */
.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 1.5rem 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.action-icon {
  font-size: 24px;
}

.action-text {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 반응형 */
@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .store-selector {
    position: static;
  }
  
  .store-select-grid {
    flex-direction: row;
    overflow-x: auto;
    gap: 12px;
    padding-bottom: 8px;
  }
  
  .store-select-item {
    min-width: 200px;
  }
}

@media (max-width: 768px) {
  .header-container,
  .nav-container,
  .main-content {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .nav-menu {
    overflow-x: auto;
  }
  
  .nav-item {
    white-space: nowrap;
    padding: 12px 16px;
  }
  
  .revenue-cards,
  .orders-summary {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .header-container {
    height: 56px;
  }
  
  .logo-char {
    font-size: 20px;
  }
  
  .user-name {
    display: none;
  }
  
  .store-select-grid {
    flex-direction: column;
  }
  
  .store-select-item {
    min-width: auto;
  }
}
</style>