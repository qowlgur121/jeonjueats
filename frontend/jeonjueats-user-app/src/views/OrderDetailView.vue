<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrder, type Order } from '../api/orders'

const route = useRoute()
const router = useRouter()

const order = ref<Order | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// 컴포넌트 마운트 시 주문 상세 로드
onMounted(async () => {
  await loadOrderDetail()
})

// 주문 상세 로드
const loadOrderDetail = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const orderId = Number(route.params.id)
    if (isNaN(orderId)) {
      throw new Error('잘못된 주문 ID입니다.')
    }
    
    order.value = await getOrder(orderId)
    
  } catch (err) {
    console.error('주문 상세 로드 실패:', err)
    error.value = '주문 상세 정보를 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
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

const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 뒤로가기
const goBack = () => {
  router.back()
}

// 재주문
const reorder = () => {
  if (order.value) {
    router.push(`/stores/${order.value.storeId}`)
  }
}
</script>

<template>
  <div class="order-detail-page">
    
    <!-- 헤더 -->
    <header class="detail-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
        </svg>
      </button>
      <h1 class="header-title">주문 상세</h1>
    </header>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">주문 상세 정보를 불러오는 중...</p>
    </div>
    
    <!-- 에러 상태 -->
    <div v-else-if="error" class="error-container">
      <p class="error-text">{{ error }}</p>
      <button @click="loadOrderDetail" class="retry-btn">다시 시도</button>
    </div>
    
    <!-- 주문 상세 내용 -->
    <div v-else-if="order" class="detail-content">
      
      <!-- 주문 상태 섹션 -->
      <section class="status-section">
        <div class="status-header">
          <span :class="getStatusColor(order.status)" class="status-badge">
            {{ getStatusText(order.status) }}
          </span>
          <span class="order-id">#{{ order.orderId.toString().padStart(6, '0') }}</span>
        </div>
        <p class="order-date">{{ formatDate(order.createdAt) }}</p>
      </section>

      <!-- 가게 정보 섹션 -->
      <section class="store-section">
        <h2 class="section-title">주문 매장</h2>
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
            <button @click="reorder" class="reorder-btn">재주문하기</button>
          </div>
        </div>
      </section>

      <!-- 주문 메뉴 섹션 -->
      <section class="menu-section">
        <h2 class="section-title">주문 메뉴</h2>
        <div class="menu-list">
          <div 
            v-for="item in order.orderItems" 
            :key="item.orderItemId"
            class="menu-item"
          >
            <div class="item-info">
              <h4 class="item-name">{{ item.menuName }}</h4>
              <p v-if="item.menuDescription" class="item-description">{{ item.menuDescription }}</p>
            </div>
            <div class="item-details">
              <span class="item-quantity">{{ item.quantity }}개</span>
              <span class="item-price">{{ formatPrice(item.itemTotalPrice) }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 배송 정보 섹션 -->
      <section class="delivery-section">
        <h2 class="section-title">배송 정보</h2>
        <div class="delivery-info">
          <div class="info-row">
            <span class="info-label">배송 주소</span>
            <span class="info-value">{{ order.fullDeliveryAddress || `${order.deliveryAddress1} ${order.deliveryAddress2 || ''}` }}</span>
          </div>
          <div v-if="order.requests" class="info-row">
            <span class="info-label">요청사항</span>
            <span class="info-value">{{ order.requests }}</span>
          </div>
        </div>
      </section>

      <!-- 결제 정보 섹션 -->
      <section class="payment-section">
        <h2 class="section-title">결제 정보</h2>
        <div class="payment-summary">
          <div class="summary-row">
            <span class="summary-label">상품금액</span>
            <span class="summary-value">{{ formatPrice(order.subtotalAmount) }}</span>
          </div>
          
          <div class="summary-row">
            <span class="summary-label">배달비</span>
            <span class="summary-value">{{ formatPrice(order.deliveryFee) }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row total-row">
            <span class="summary-label">총 결제금액</span>
            <span class="summary-value total-value">{{ formatPrice(order.totalPrice) }}</span>
          </div>
          
          <div class="payment-method">
            <span class="method-label">결제수단</span>
            <span class="method-value">{{ order.paymentMethod }}</span>
          </div>
        </div>
      </section>

    </div>

  </div>
</template>

<style scoped>
/* 주문 상세 페이지 컨테이너 */
.order-detail-page {
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 헤더 */
.detail-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.back-button:hover {
  background-color: #f3f4f6;
}

.back-button svg {
  width: 24px;
  height: 24px;
  color: #374151;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

/* 로딩 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem;
  border-radius: 12px;
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

/* 에러 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem;
  border-radius: 12px;
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

/* 컨텐츠 */
.detail-content {
  padding: 16px;
}

/* 섹션 공통 */
section {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 16px;
}

/* 주문 상태 섹션 */
.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.status-badge {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
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

.order-id {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

.order-date {
  font-size: 14px;
  color: #6b7280;
}

/* 가게 정보 섹션 */
.store-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.store-image {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  object-fit: cover;
}

.store-image-placeholder {
  width: 60px;
  height: 60px;
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
  margin-bottom: 8px;
}

.reorder-btn {
  padding: 8px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.reorder-btn:hover {
  background-color: #2563eb;
}

/* 메뉴 섹션 */
.menu-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.menu-item:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.item-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.4;
}

.item-details {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.item-quantity {
  font-size: 14px;
  color: #6b7280;
}

.item-price {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

/* 배송 정보 섹션 */
.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #374151;
  text-align: right;
  line-height: 1.4;
}

/* 결제 정보 섹션 */
.payment-summary {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-label {
  font-size: 14px;
  color: #6b7280;
}

.summary-value {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.summary-divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 12px 0;
}

.total-row .summary-label {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.total-value {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f6;
}

.payment-method {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.method-label {
  font-size: 14px;
  color: #6b7280;
}

.method-value {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 반응형 */
@media (max-width: 768px) {
  .detail-content {
    padding: 12px;
  }
  
  section {
    padding: 16px;
  }
  
  .store-info {
    gap: 12px;
  }
  
  .info-row {
    flex-direction: column;
    gap: 4px;
  }
  
  .info-value {
    text-align: left;
  }
}
</style>