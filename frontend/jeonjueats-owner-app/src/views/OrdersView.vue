<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import apiClient from '../api/client'
import * as storesApi from '../api/stores'
import type { Store } from '../api/stores'

interface OrderItem {
  orderItemId: number
  menuId: number
  menuName: string
  menuDescription?: string
  menuImageUrl?: string
  quantity: number
  priceAtOrder: number
  itemTotalPrice: number
}

interface Order {
  orderId: number
  userId: number
  userNickname?: string
  storeId?: number
  storeName?: string
  storeImageUrl?: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'DELIVERING' | 'COMPLETED'
  statusDisplayName: string
  subtotalAmount: number
  deliveryFee?: number
  deliveryFeeAtOrder?: number // 리스트용
  totalPrice: number
  deliveryZipcode?: string
  deliveryAddress1: string
  deliveryAddress2: string
  fullDeliveryAddress?: string
  requests?: string
  paymentMethod?: string
  paymentTransactionId?: string
  orderItems?: OrderItem[]
  totalItemCount?: number
  totalQuantity: number
  createdAt?: string | null // 상세용
  orderedAt?: string | null // 리스트용
  updatedAt?: string | null
  // 리스트용 필드들 (OwnerOrderListResponseDto에서만 사용)
  representativeMenuName?: string
  totalMenuCount?: number
}

// 상태 관리
const stores = ref<Store[]>([])
const selectedStoreId = ref<number | null>(null)
const orders = ref<Order[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const statusFilter = ref<string>('ALL')
const selectedOrder = ref<Order | null>(null)
const showDetailModal = ref(false)

// 주문 상태별 색상 및 스타일
const getStatusStyle = (status: string) => {
  switch (status) {
    case 'PENDING':
      return { background: '#fff7e6', color: '#fa8c16', border: '#ffd591' }
    case 'ACCEPTED':
      return { background: '#e6f7ff', color: '#1890ff', border: '#91d5ff' }
    case 'REJECTED':
      return { background: '#fff1f0', color: '#f5222d', border: '#ffccc7' }
    case 'DELIVERING':
      return { background: '#f6ffed', color: '#52c41a', border: '#b7eb8f' }
    case 'COMPLETED':
      return { background: '#f9f9f9', color: '#595959', border: '#d9d9d9' }
    default:
      return { background: '#f0f0f0', color: '#000', border: '#d9d9d9' }
  }
}

// 상태별 필터링된 주문 목록
const filteredOrders = computed(() => {
  if (statusFilter.value === 'ALL') {
    return orders.value
  }
  return orders.value.filter(order => order.status === statusFilter.value)
})

// 상태별 주문 개수
const statusCounts = computed(() => {
  const counts: Record<string, number> = {
    ALL: orders.value.length,
    PENDING: 0,
    ACCEPTED: 0,
    DELIVERING: 0,
    COMPLETED: 0,
    REJECTED: 0
  }
  
  orders.value.forEach(order => {
    counts[order.status]++
  })
  
  return counts
})

// 초기 데이터 로드
const loadStores = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const data = await storesApi.getOwnerStores()
    stores.value = data
    
    // 첫 번째 가게 자동 선택
    if (data.length > 0) {
      selectedStoreId.value = data[0].storeId
    }
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 주문 목록 로드
const loadOrders = async () => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const params: any = {
      page: 0,
      size: 50,
      sort: 'createdAt,desc'
    }
    
    // 상태 필터 적용
    if (statusFilter.value !== 'ALL') {
      params.status = statusFilter.value
    }
    
    const response = await apiClient.get(`/api/owner/stores/${selectedStoreId.value}/orders`, { params })
    orders.value = response.data.content || []
  } catch (error: any) {
    console.error('주문 목록 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '주문 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 주문 상세 조회
const viewOrderDetail = async (order: Order) => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const response = await apiClient.get(`/api/owner/stores/${selectedStoreId.value}/orders/${order.orderId}`)
    console.log('주문 상세 조회 응답:', response.data)
    selectedOrder.value = response.data
    showDetailModal.value = true
  } catch (error: any) {
    console.error('주문 상세 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '주문 상세 정보를 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 주문 상태 변경
const updateOrderStatus = async (orderId: number, newStatus: string) => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    await apiClient.put(`/api/owner/stores/${selectedStoreId.value}/orders/${orderId}/status`, {
      newStatus: newStatus
    })
    
    // 주문 목록 새로고침
    await loadOrders()
    
    // 상세 모달이 열려있다면 닫기
    if (showDetailModal.value) {
      showDetailModal.value = false
      selectedOrder.value = null
    }
  } catch (error: any) {
    console.error('주문 상태 변경 실패:', error)
    errorMessage.value = error.response?.data?.message || '주문 상태 변경에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 날짜 포맷팅
const formatDate = (dateString: string | null | undefined) => {
  if (!dateString) return '-'
  
  const date = new Date(dateString)
  
  // 유효한 날짜인지 확인
  if (isNaN(date.getTime())) {
    console.warn('Invalid date:', dateString)
    return '-'
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}시간 전`
  
  return new Intl.DateTimeFormat('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  }).format(date)
}

// 금액 포맷팅
const formatCurrency = (amount: number | null | undefined) => {
  if (typeof amount !== 'number') return '₩0'
  
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

// 가게 변경 시 주문 목록 리로드
watch(selectedStoreId, () => {
  if (selectedStoreId.value) {
    loadOrders()
  }
})

// 상태 필터 변경 시 주문 목록 리로드
watch(statusFilter, () => {
  loadOrders()
})



// 컴포넌트 마운트 시 초기 로드
onMounted(async () => {
  await loadStores()
})
</script>

<template>
  <div class="orders-container">
    <!-- 메인 컨텐츠 -->
    <main class="main-content">
      <div class="page-header">
        <div class="page-title">
          <h1>주문 관리</h1>
          <p class="page-subtitle">실시간 주문 현황을 확인하고 관리하세요</p>
        </div>
        
        <!-- 가게 선택 드롭다운 -->
        <div class="store-selector">
          <select v-model="selectedStoreId" class="store-dropdown">
            <option v-for="store in stores" :key="store.storeId" :value="store.storeId">
              {{ store.name }}
            </option>
          </select>
        </div>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <!-- 주문 상태 필터 탭 -->
      <div class="status-tabs">
        <button 
          v-for="(count, status) in statusCounts" 
          :key="status"
          @click="statusFilter = status"
          :class="['status-tab', { active: statusFilter === status }]"
        >
          <span class="tab-label">
            {{ status === 'ALL' ? '전체' :
               status === 'PENDING' ? '대기 중' :
               status === 'ACCEPTED' ? '수락됨' :
               status === 'DELIVERING' ? '배달 중' :
               status === 'COMPLETED' ? '완료' :
               status === 'REJECTED' ? '거절됨' : status }}
          </span>
          <span class="tab-count">{{ count }}</span>
        </button>
      </div>

      <!-- 주문 목록 -->
      <div v-if="isLoading" class="loading">
        주문 목록을 불러오는 중...
      </div>
      
      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <p>{{ statusFilter === 'ALL' ? '주문이 없습니다.' : '해당 상태의 주문이 없습니다.' }}</p>
      </div>
      
      <div v-else class="orders-grid">
        <div 
          v-for="order in filteredOrders" 
          :key="order.orderId" 
          class="order-card"
          :style="{ borderColor: getStatusStyle(order.status).border }"
        >
          <!-- 주문 헤더 -->
          <div class="order-header">
            <div class="order-info">
              <h3 class="order-number">#{{ order.orderId }}</h3>
              <span class="order-time">{{ formatDate(order.orderedAt) }}</span>
            </div>
            <div 
              class="order-status"
              :style="{
                backgroundColor: getStatusStyle(order.status).background,
                color: getStatusStyle(order.status).color
              }"
            >
              {{ order.statusDisplayName }}
            </div>
          </div>

          <!-- 주문 내용 -->
          <div class="order-content">
            <div class="customer-info">
              <strong>{{ order.userNickname }}</strong>
            </div>
            <div class="menu-info">
              <span class="menu-name">{{ order.representativeMenuName }}</span>
              <span v-if="order.totalMenuCount && order.totalMenuCount > 1" class="menu-count">
                외 {{ order.totalMenuCount - 1 }}개
              </span>
              <span class="total-quantity">(총 {{ order.totalQuantity }}개)</span>
            </div>
            <div class="address-info">
              📍 {{ order.deliveryAddress1 }} {{ order.deliveryAddress2 }}
            </div>
            <div v-if="order.requests" class="request-info">
              💬 {{ order.requests }}
            </div>
          </div>

          <!-- 주문 금액 -->
          <div class="order-price">
            <span class="price-label">결제 금액</span>
            <span class="price-amount">{{ formatCurrency(order.totalPrice) }}</span>
          </div>

          <!-- 주문 액션 -->
          <div class="order-actions">
            <button @click="viewOrderDetail(order)" class="btn-detail">
              상세보기
            </button>
            
            <!-- 상태별 액션 버튼 -->
            <template v-if="order.status === 'PENDING'">
              <button @click="updateOrderStatus(order.orderId, 'ACCEPTED')" class="btn-accept">
                주문 수락
              </button>
              <button @click="updateOrderStatus(order.orderId, 'REJECTED')" class="btn-reject">
                주문 거절
              </button>
            </template>
            
            <template v-else-if="order.status === 'ACCEPTED'">
              <button @click="updateOrderStatus(order.orderId, 'DELIVERING')" class="btn-delivering">
                배달 시작
              </button>
            </template>
            
            <template v-else-if="order.status === 'DELIVERING'">
              <button @click="updateOrderStatus(order.orderId, 'COMPLETED')" class="btn-complete">
                배달 완료
              </button>
            </template>
          </div>
        </div>
      </div>
    </main>

    <!-- 주문 상세 모달 -->
    <div v-if="showDetailModal && selectedOrder" class="modal-overlay" @click="showDetailModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>주문 상세 정보</h3>
          <button @click="showDetailModal = false" class="modal-close">✕</button>
        </div>
        
        <div class="modal-body">
          <div class="detail-section">
            <h4>주문 정보</h4>
            <div class="detail-row">
              <span class="detail-label">주문 번호</span>
              <span class="detail-value">#{{ selectedOrder?.orderId || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">주문 시간</span>
              <span class="detail-value">{{ formatDate(selectedOrder?.createdAt) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">주문 상태</span>
              <span 
                class="detail-status"
                :style="{
                  backgroundColor: getStatusStyle(selectedOrder?.status || '').background,
                  color: getStatusStyle(selectedOrder?.status || '').color,
                  padding: '4px 12px',
                  borderRadius: '4px',
                  fontSize: '14px'
                }"
              >
                {{ selectedOrder?.statusDisplayName || '-' }}
              </span>
            </div>
          </div>

          <div class="detail-section">
            <h4>고객 정보</h4>
            <div class="detail-row">
              <span class="detail-label">주문자</span>
              <span class="detail-value">{{ selectedOrder?.userNickname || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">배달 주소</span>
              <span class="detail-value">
                {{ selectedOrder?.deliveryAddress1 || '' }} {{ selectedOrder?.deliveryAddress2 || '' }}
              </span>
            </div>
            <div v-if="selectedOrder?.requests" class="detail-row">
              <span class="detail-label">요청 사항</span>
              <span class="detail-value">{{ selectedOrder.requests }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4>주문 메뉴</h4>
            <div v-if="selectedOrder?.orderItems" class="order-items">
              <div v-for="item in selectedOrder.orderItems" :key="item.orderItemId" class="item-row">
                <span class="item-name">{{ item.menuName }}</span>
                <span class="item-quantity">{{ item.quantity }}개</span>
                <span class="item-price">{{ formatCurrency(item.itemTotalPrice) }}</span>
              </div>
            </div>
            <div v-else class="menu-summary">
              <div class="detail-row">
                <span class="detail-label">대표 메뉴</span>
                <span class="detail-value">{{ selectedOrder?.representativeMenuName || '-' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">총 메뉴 수</span>
                <span class="detail-value">{{ selectedOrder?.totalItemCount || 0 }}개</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">총 수량</span>
                <span class="detail-value">{{ selectedOrder?.totalQuantity || 0 }}개</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>결제 정보</h4>
            <div class="detail-row">
              <span class="detail-label">결제 방법</span>
              <span class="detail-value">{{ selectedOrder?.paymentMethod || '정보 없음' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">메뉴 금액</span>
              <span class="detail-value">{{ formatCurrency(selectedOrder?.subtotalAmount || 0) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">배달비</span>
              <span class="detail-value">{{ formatCurrency(selectedOrder?.deliveryFee || selectedOrder?.deliveryFeeAtOrder || 0) }}</span>
            </div>
            <div class="detail-row total">
              <span class="detail-label">총 결제 금액</span>
              <span class="detail-value">{{ formatCurrency(selectedOrder?.totalPrice || 0) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-container {
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 헤더 스타일 */
.header {
  background: white;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 2rem;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
}

.logo-char {
  transition: color 0.3s ease;
}

/* jeonju eats 색상 */
.c1 { color: #8B4513; }
.c2 { color: #8B4513; }
.c3 { color: #8B4513; }
.c4 { color: #FF4500; }
.c5 { color: #FFD700; }
.c6 { color: #32CD32; }

.logo-space {
  font-size: 24px;
  margin: 0 4px;
}

.owner-badge {
  background: #3b82f6;
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.btn-logout {
  padding: 8px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}


/* 메인 컨텐츠 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.page-title h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
}

.store-selector {
  position: relative;
}

.store-dropdown {
  padding: 0.75rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  font-size: 0.875rem;
  min-width: 200px;
  cursor: pointer;
}

/* 에러 메시지 */
.error-message {
  background-color: #fee;
  color: #c00;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

/* 상태 필터 탭 */
.status-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  background-color: white;
  padding: 1rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  color: #6b7280;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.status-tab:hover {
  background-color: #f9fafb;
}

.status-tab.active {
  background-color: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.tab-label {
  font-weight: 500;
}

.tab-count {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-tab.active .tab-count {
  background-color: rgba(255, 255, 255, 0.3);
}

/* 로딩 상태 */
.loading {
  text-align: center;
  padding: 4rem;
  color: #6b7280;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 4rem;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.3;
}

.empty-state p {
  color: #6b7280;
  margin: 0;
}

/* 주문 그리드 */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

/* 주문 카드 */
.order-card {
  background-color: white;
  border-radius: 12px;
  border: 2px solid;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: all 0.2s;
}

.order-card:hover {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-info {
  flex: 1;
}

.order-number {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.25rem 0;
}

.order-time {
  font-size: 0.75rem;
  color: #6b7280;
}

.order-status {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
}

.order-content {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f3f4f6;
}

.customer-info {
  margin-bottom: 0.5rem;
}

.customer-info strong {
  color: #1f2937;
}

.menu-info {
  color: #374151;
  margin-bottom: 0.5rem;
}

.menu-name {
  font-weight: 500;
}

.menu-count {
  color: #6b7280;
  font-size: 0.875rem;
}

.total-quantity {
  color: #6b7280;
  font-size: 0.875rem;
}

.address-info {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.request-info {
  font-size: 0.875rem;
  color: #f59e0b;
  background-color: #fef3c7;
  padding: 0.5rem;
  border-radius: 6px;
  margin-top: 0.5rem;
}

.order-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.price-label {
  color: #6b7280;
  font-size: 0.875rem;
}

.price-amount {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
}

.order-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.order-actions button {
  flex: 1;
  min-width: 100px;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-detail {
  background-color: #f3f4f6;
  color: #374151;
}

.btn-detail:hover {
  background-color: #e5e7eb;
}

.btn-accept {
  background-color: #3b82f6;
  color: white;
}

.btn-accept:hover {
  background-color: #2563eb;
}

.btn-reject {
  background-color: #ef4444;
  color: white;
}

.btn-reject:hover {
  background-color: #dc2626;
}

.btn-delivering {
  background-color: #10b981;
  color: white;
}

.btn-delivering:hover {
  background-color: #059669;
}

.btn-complete {
  background-color: #6b7280;
  color: white;
}

.btn-complete:hover {
  background-color: #4b5563;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
}

.modal-close:hover {
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.detail-section {
  margin-bottom: 1.5rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 1rem 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.detail-row.total {
  border-top: 2px solid #e5e7eb;
  padding-top: 1rem;
  margin-top: 0.5rem;
  font-weight: 600;
}

.detail-label {
  color: #6b7280;
  font-size: 0.875rem;
}

.detail-value {
  color: #1f2937;
  font-size: 0.875rem;
  text-align: right;
}

.order-items {
  background-color: #f9fafb;
  padding: 1rem;
  border-radius: 8px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.item-row:not(:last-child) {
  border-bottom: 1px solid #e5e7eb;
}

.item-name {
  flex: 1;
  color: #1f2937;
}

.item-quantity {
  color: #6b7280;
  margin: 0 1rem;
}

.item-price {
  color: #1f2937;
  font-weight: 500;
}

.menu-summary {
  background-color: #f9fafb;
  padding: 1rem;
  border-radius: 8px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .nav-menu {
    padding: 0 1rem;
    gap: 0.5rem;
    overflow-x: auto;
  }
  
  .nav-item {
    padding: 1rem 0.75rem;
    white-space: nowrap;
  }
  
  .page-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .status-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
  }
  
  .status-tab {
    white-space: nowrap;
  }
  
  .orders-grid {
    grid-template-columns: 1fr;
  }
  
  .order-actions {
    flex-direction: column;
  }
  
  .order-actions button {
    width: 100%;
  }
}
</style>