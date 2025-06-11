<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, removeCartItem, clearCart, type CartResponse, type CartItem } from '../api/cart'

// 라우터
const router = useRouter()

// 상태 관리
const cartData = ref<CartResponse | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// 컴포넌트 마운트 시 장바구니 로드
onMounted(async () => {
  await loadCart()
})

// 장바구니 로드
const loadCart = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    console.log('CartView: 장바구니 로드 시작')
    const response = await getCart()
    console.log('CartView: 장바구니 로드 응답:', response)
    cartData.value = response
    
    console.log('CartView: 장바구니 데이터 설정 완료:', cartData.value)
    console.log('CartView: 아이템 개수:', cartData.value?.items?.length || 0)
    console.log('CartView: 빈 장바구니 여부:', cartData.value?.isEmpty)
    
  } catch (err) {
    console.error('CartView: 장바구니 로드 실패:', err)
    error.value = '장바구니를 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 뒤로가기
const goBack = () => {
  router.back()
}

// 수량 증가 (API 연동 필요)
const increaseQuantity = async (item: CartItem) => {
  // TODO: 수량 업데이트 API 호출
  console.log('수량 증가:', item.cartItemId)
}

// 수량 감소 (API 연동 필요)
const decreaseQuantity = async (item: CartItem) => {
  // TODO: 수량 업데이트 API 호출
  console.log('수량 감소:', item.cartItemId)
}

// 아이템 삭제
const removeItem = async (cartItemId: number) => {
  try {
    await removeCartItem(cartItemId)
    await loadCart() // 삭제 후 장바구니 다시 로드
  } catch (err) {
    console.error('아이템 삭제 실패:', err)
    alert('아이템 삭제에 실패했습니다.')
  }
}

// 장바구니 전체 비우기
const clearAllItems = async () => {
  if (!confirm('장바구니를 전체 비우시겠습니까?')) return
  
  try {
    await clearCart()
    await loadCart() // 비우기 후 장바구니 다시 로드
  } catch (err) {
    console.error('장바구니 비우기 실패:', err)
    alert('장바구니 비우기에 실패했습니다.')
  }
}

// 주문하기
const proceedToOrder = () => {
  router.push('/order')
}

// 계산된 값들
const cartItems = computed(() => cartData.value?.items || [])
const storeInfo = computed(() => cartData.value?.store)
const orderSummary = computed(() => cartData.value?.orderSummary)
const isEmpty = computed(() => cartData.value?.isEmpty ?? true)

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}
</script>

<template>
  <!-- 장바구니 페이지 -->
  <div class="cart-page">
    
    <!-- 헤더 -->
    <section class="cart-header">
      <div class="section-container">
        <div class="header-content">
          <button @click="goBack" class="back-button">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
          </button>
          <h1 class="page-title">장바구니</h1>
          <div class="header-spacer"></div>
        </div>
      </div>
    </section>

    <!-- 가게 정보 -->
    <section v-if="storeInfo" class="store-section">
      <div class="section-container">
        <div class="store-info">
          <h2 class="store-name">{{ storeInfo.name }}</h2>
          <p class="delivery-info">
            배달비 {{ formatPrice(storeInfo.deliveryFee) }}
          </p>
        </div>
      </div>
    </section>

    <!-- 장바구니 아이템 목록 -->
    <section class="cart-items-section">
      <div class="section-container">
        
        <!-- 로딩 상태 -->
        <div v-if="isLoading" class="loading-state">
          <div class="loading-spinner"></div>
          <p class="loading-text">장바구니를 불러오는 중...</p>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="error-state">
          <div class="error-icon">⚠️</div>
          <h3 class="error-title">오류가 발생했습니다</h3>
          <p class="error-description">{{ error }}</p>
          <button @click="loadCart" class="retry-button">다시 시도</button>
        </div>

        <!-- 빈 장바구니 -->
        <div v-else-if="isEmpty" class="empty-cart">
          <div class="empty-icon">🛒</div>
          <h3 class="empty-title">장바구니가 비어있어요</h3>
          <p class="empty-description">맛있는 메뉴를 담아보세요!</p>
          <button @click="goBack" class="go-back-button">
            메뉴 보러가기
          </button>
        </div>

        <!-- 장바구니 아이템들 -->
        <div v-else class="cart-items">
          <div class="cart-header">
            <h3 class="cart-title">담은 메뉴</h3>
            <button @click="clearAllItems" class="clear-all-button">전체삭제</button>
          </div>
          
          <div 
            v-for="item in cartItems"
            :key="item.cartItemId"
            class="cart-item"
          >
            <!-- 아이템 이미지 -->
            <div class="item-image-container">
              <img 
                v-if="item.menuImageUrl" 
                :src="item.menuImageUrl" 
                :alt="item.menuName"
                class="item-image"
              >
              <div v-else class="item-image-placeholder">
                <span class="placeholder-emoji">🍽️</span>
              </div>
            </div>
            
            <!-- 아이템 정보 -->
            <div class="item-content">
              <div class="item-header">
                <h3 class="item-name">{{ item.menuName }}</h3>
                <button @click="removeItem(item.cartItemId)" class="remove-button">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                  </svg>
                </button>
              </div>
              
              <p class="item-description">{{ item.menuDescription }}</p>
              
              <div class="item-footer">
                <div class="item-price">
                  <span class="current-price">{{ formatPrice(item.menuPrice) }}</span>
                </div>
                
                <div class="quantity-controls">
                  <button 
                    @click="decreaseQuantity(item)"
                    :disabled="item.quantity <= 1"
                    class="quantity-button"
                  >
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13H5v-2h14v2z"/>
                    </svg>
                  </button>
                  <span class="quantity-display">{{ item.quantity }}</span>
                  <button 
                    @click="increaseQuantity(item)"
                    class="quantity-button"
                  >
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 주문 요약 및 결제 -->
    <section v-if="!isEmpty && orderSummary" class="order-summary-section">
      <div class="section-container">
        
        <!-- 주문 요약 -->
        <div class="order-summary">
          <h3 class="summary-title">주문 요약</h3>
          
          <div class="summary-row">
            <span class="summary-label">상품금액</span>
            <span class="summary-value">{{ formatPrice(orderSummary.subtotal) }}</span>
          </div>
          
          <div class="summary-row">
            <span class="summary-label">배달비</span>
            <span class="summary-value">{{ formatPrice(orderSummary.deliveryFee) }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row total-row">
            <span class="summary-label">총 결제금액</span>
            <span class="summary-value total-value">{{ formatPrice(orderSummary.total) }}</span>
          </div>
          
          <!-- 최소주문금액 체크 -->
          <div v-if="!orderSummary.canOrder && storeInfo" class="min-order-warning">
            <span class="warning-icon">⚠️</span>
            <span class="warning-text">
              최소주문금액 {{ formatPrice(storeInfo.minOrderAmount) }}에 
              {{ formatPrice(orderSummary.minOrderAmountShortage) }} 부족합니다
            </span>
          </div>
        </div>
        
        <!-- 주문하기 버튼 -->
        <button 
          @click="proceedToOrder"
          :disabled="!orderSummary.canOrder"
          class="order-button"
          :class="{ 'disabled': !orderSummary.canOrder }"
        >
          <span class="button-text">
            {{ orderSummary.canOrder ? '주문하기' : `${formatPrice(orderSummary.minOrderAmountShortage)} 더 담아주세요` }}
          </span>
          <span v-if="orderSummary.canOrder" class="button-price">{{ formatPrice(orderSummary.total) }}</span>
        </button>
        
      </div>
    </section>

  </div>
</template>

<style scoped>
/* 장바구니 페이지 컨테이너 */
.cart-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 120px; /* 주문 버튼을 위한 여백 */
}

/* 공통 섹션 컨테이너 */
.section-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* 헤더 */
.cart-header {
  background-color: white;
  padding: 1rem 0;
  border-bottom: 1px solid #f1f3f4;
  position: sticky;
  top: 0;
  z-index: 40;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
  transition: all 0.2s ease;
  color: #374151;
}

.back-button:hover {
  background-color: #f3f4f6;
}

.back-button svg {
  width: 24px;
  height: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.header-spacer {
  width: 40px;
}

/* 가게 섹션 */
.store-section {
  background-color: white;
  padding: 1.5rem 0;
  border-bottom: 1px solid #f1f3f4;
}

.store-info {
  text-align: center;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.delivery-info {
  font-size: 14px;
  color: #6b7280;
}

/* 장바구니 아이템 섹션 */
.cart-items-section {
  background-color: white;
  padding: 1.5rem 0;
}

/* 빈 장바구니 */
.empty-cart {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 2rem;
}

.go-back-button {
  padding: 12px 24px;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.go-back-button:hover {
  background-color: #1f2937;
}

/* 장바구니 아이템들 */
.cart-items {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.cart-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid #f1f3f4;
}

.item-image-container {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
}

.placeholder-emoji {
  font-size: 32px;
  opacity: 0.6;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  margin-right: 8px;
}

.remove-button {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #f3f4f6;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
  flex-shrink: 0;
}

.remove-button:hover {
  background-color: #e5e7eb;
  color: #374151;
}

.remove-button svg {
  width: 16px;
  height: 16px;
}

.item-options {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
  line-height: 1.4;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.item-price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.original-price {
  font-size: 13px;
  color: #9ca3af;
  text-decoration: line-through;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #374151;
}

.quantity-button:hover:not(:disabled) {
  border-color: #374151;
  background-color: #f9fafb;
}

.quantity-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-button svg {
  width: 16px;
  height: 16px;
}

.quantity-display {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  min-width: 24px;
  text-align: center;
}

/* 주문 요약 섹션 */
.order-summary-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #e5e7eb;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.order-summary {
  padding: 1.5rem;
  border-bottom: 1px solid #f1f3f4;
}

.summary-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #6b7280;
}

.summary-value {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.summary-divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 1rem 0;
}

.total-row {
  margin-bottom: 0;
}

.total-row .summary-label {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.total-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.min-order-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 1rem;
  padding: 12px;
  background-color: #fef3cd;
  border-radius: 8px;
}

.warning-icon {
  font-size: 16px;
}

.warning-text {
  font-size: 13px;
  color: #92400e;
  font-weight: 500;
}

.order-button {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  display: block;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  margin: 1rem 1.5rem;
}

.order-button:hover:not(.disabled) {
  background-color: #1f2937;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-button.disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.button-text {
  font-weight: 600;
}

.button-price {
  font-weight: 700;
}

/* 반응형 */
@media (max-width: 768px) {
  .cart-item {
    padding: 0.75rem;
  }
  
  .item-image-container {
    width: 70px;
    height: 70px;
  }
  
  .item-name {
    font-size: 15px;
  }
  
  .quantity-controls {
    gap: 8px;
  }
  
  .quantity-button {
    width: 28px;
    height: 28px;
  }
  
  .quantity-button svg {
    width: 14px;
    height: 14px;
  }
}
</style>