<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, type OrderListItem } from '../api/orders'

const router = useRouter()

const orders = ref<OrderListItem[]>([])
const filteredOrders = ref<OrderListItem[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const sortBy = ref('latest')

// 컴포넌트 마운트 시 주문 목록 로드
onMounted(async () => {
  await loadOrders()
})

// 주문 목록 로드
const loadOrders = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const response = await getOrders()
    orders.value = response.content
    applySort()
    
  } catch (err) {
    console.error('주문 목록 로드 실패:', err)
    error.value = '주문 목록을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 정렬 적용
const applySort = () => {
  let sorted = [...orders.value]
  
  switch (sortBy.value) {
    case 'latest':
      sorted.sort((a, b) => new Date(b.orderedAt).getTime() - new Date(a.orderedAt).getTime())
      break
    case 'oldest':
      sorted.sort((a, b) => new Date(a.orderedAt).getTime() - new Date(b.orderedAt).getTime())
      break
    case 'price':
      sorted.sort((a, b) => b.totalPrice - a.totalPrice)
      break
  }
  
  filteredOrders.value = sorted
}

// 정렬 옵션 변경 감지
watch(sortBy, () => {
  applySort()
})

// 재주문
const reorder = (storeId: number) => {
  router.push(`/stores/${storeId}`)
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'COMPLETED':
      return 'status-delivered'
    case 'DELIVERING':
      return 'status-preparing'
    case 'ACCEPTED':
      return 'status-accepted'
    case 'PENDING':
      return 'status-pending'
    case 'REJECTED':
      return 'status-cancelled'
    default:
      return 'status-pending'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '주문 확인중'
    case 'ACCEPTED':
      return '주문 수락'
    case 'REJECTED':
      return '주문 거절'
    case 'DELIVERING':
      return '배달중'
    case 'COMPLETED':
      return '배달완료'
    default:
      return status
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
</script>

<template>
  <!-- 주문내역 페이지 -->
  <div class="orders-page">
    
    <!-- 헤더 섹션 -->
    <section class="header-section">
      <div class="section-container">
        <div class="header-content">
          <h1 class="page-title">주문내역</h1>
          <p class="page-subtitle">지금까지 주문한 맛집들을 확인해보세요</p>
        </div>
      </div>
    </section>

    <!-- 주문 목록 컨텐츠 -->
    <div class="orders-content">
      
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">주문 내역을 불러오는 중...</p>
      </div>
      
      <!-- 에러 상태 -->
      <div v-else-if="error" class="error-container">
        <p class="error-text">{{ error }}</p>
        <button @click="loadOrders" class="retry-btn">다시 시도</button>
      </div>
      
      <!-- 주문 목록이 있을 때 -->
      <section v-else-if="orders.length > 0" class="orders-section">
        <div class="section-container">
          
          <!-- 통계 정보 -->
          <div class="stats-info">
            <h2 class="stats-title">
              총 <span class="stats-number">{{ orders.length }}건</span> 주문
            </h2>
            <select v-model="sortBy" class="sort-select">
              <option value="latest">최신순</option>
              <option value="oldest">오래된순</option>
              <option value="price">금액순</option>
            </select>
          </div>
          
          <!-- 주문 목록 -->
          <div class="orders-list">
            <div 
              v-for="order in filteredOrders" 
              :key="order.orderId"
              class="order-card"
            >
              <!-- 주문 헤더 -->
              <div class="order-header">
                <div class="order-status">
                  <span :class="getStatusColor(order.status)" class="status-badge">
                    {{ getStatusText(order.status) }}
                  </span>
                  <span class="order-date">
                    {{ formatDate(order.orderedAt) }}
                  </span>
                </div>
                <span class="order-id">#{{ order.orderId.toString().padStart(6, '0') }}</span>
              </div>

              <!-- 매장 정보 -->
              <div class="store-info">
                <img 
                  v-if="order.storeImageUrl" 
                  :src="order.storeImageUrl" 
                  :alt="order.storeName"
                  class="store-image"
                >
                <div v-else class="store-image-placeholder">🍽️</div>
                <div class="store-details">
                  <h3 class="store-name">{{ order.storeName }}</h3>
                  <p class="menu-summary">
                    <span class="main-menu">{{ order.representativeMenuName }}</span>
                  </p>
                </div>
              </div>

              <!-- 주문 상세 -->
              <div class="order-details">
                <div class="menu-summary-info">
                  <span class="menu-count-badge">
                    총 {{ order.totalMenuCount }}개 메뉴 · {{ order.totalQuantity }}개
                  </span>
                </div>
              </div>

              <!-- 주문 액션 -->
              <div class="order-actions">
                <div class="total-amount">
                  <span class="amount-label">총 결제금액</span>
                  <span class="amount-value">{{ order.totalPrice.toLocaleString() }}원</span>
                </div>
                <div class="action-buttons">
                  <button @click="reorder(order.storeId)" class="action-btn secondary">재주문</button>
                  <router-link :to="`/orders/${order.orderId}`" class="action-btn primary">
                    상세보기
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 빈 상태 -->
      <section v-else class="empty-section">
        <div class="section-container">
          <div class="empty-content">
            <div class="empty-icon">📝</div>
            <h3 class="empty-title">아직 주문 내역이 없어요</h3>
            <p class="empty-description">맛있는 음식을 주문하고<br>첫 주문 내역을 만들어보세요</p>
            <router-link to="/" class="explore-btn">
              주문하러 가기
            </router-link>
          </div>
        </div>
      </section>
      
    </div>

  </div>
</template>

<style scoped>
/* 주문내역 페이지 컨테이너 */
.orders-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 로딩 컨테이너 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 16px;
  color: #6b7280;
}

/* 에러 컨테이너 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem 0;
}

.error-text {
  font-size: 16px;
  color: #ef4444;
  margin-bottom: 1rem;
}

.retry-btn {
  padding: 8px 20px;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background-color: #1f2937;
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

/* 주문 목록 컨텐츠 */
.orders-content {
  padding-top: 1rem;
}

/* 주문 목록 섹션 */
.orders-section {
  background-color: white;
  margin: 1rem 0;
  padding: 2rem 0;
}

/* 통계 정보 */
.stats-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.stats-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.stats-number {
  color: #3b82f6;
  font-weight: 700;
}

.sort-select {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  outline: none;
}

.sort-select:focus {
  border-color: #374151;
}

/* 주문 목록 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 주문 카드 */
.order-card {
  background-color: white;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  padding: 1.5rem;
  transition: all 0.2s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border-color: #e5e7eb;
}

/* 주문 헤더 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f3f4f6;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-delivered {
  background-color: #d1fae5;
  color: #065f46;
}

.status-preparing {
  background-color: #fef3c7;
  color: #92400e;
}

.status-accepted {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-pending {
  background-color: #f3f4f6;
  color: #374151;
}

.status-cancelled {
  background-color: #fee2e2;
  color: #991b1b;
}

.order-date {
  font-size: 14px;
  color: #6b7280;
}

.order-id {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

/* 매장 정보 */
.store-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.store-image {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  object-fit: cover;
}

.store-image-placeholder {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.store-details {
  flex: 1;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.menu-summary {
  font-size: 14px;
  color: #6b7280;
}

.main-menu {
  color: #374151;
  font-weight: 500;
}

.menu-count {
  color: #9ca3af;
}

/* 주문 상세 */
.order-details {
  margin-bottom: 1.5rem;
}

.menu-summary-info {
  display: flex;
  justify-content: center;
}

.menu-count-badge {
  background-color: #f8f9fa;
  color: #374151;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
}

/* 주문 액션 */
.order-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.total-amount {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.amount-label {
  font-size: 12px;
  color: #9ca3af;
}

.amount-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.action-btn.primary {
  background-color: #374151;
  color: white;
}

.action-btn.primary:hover {
  background-color: #1f2937;
}

.action-btn.secondary {
  background-color: #f8f9fa;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.action-btn.secondary:hover {
  background-color: #f1f3f4;
  border-color: #d1d5db;
}

/* 빈 상태 섹션 */
.empty-section {
  background-color: white;
  margin: 1rem 0;
  padding: 4rem 0;
}

.empty-content {
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.empty-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.explore-btn {
  display: inline-block;
  background-color: #374151;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.explore-btn:hover {
  background-color: #1f2937;
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 768px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .order-actions {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .stats-info {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style> 