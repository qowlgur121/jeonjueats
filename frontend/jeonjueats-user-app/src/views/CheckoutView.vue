<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getCart, updateCartItem, removeCartItem } from '@/api/cart'
import { createOrder, type CreateOrderRequest } from '@/api/orders'
import { getUserProfile } from '@/api/users'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

// 로딩 상태
const isLoading = ref(false)
const isProcessing = ref(false)
const updatingItems = ref<Set<number>>(new Set())

// 장바구니 데이터
const cartData = ref<any>(null)

// 배송 정보
const deliveryInfo = ref({
  zipcode: '',
  address: '',
  detailAddress: '',
  phoneNumber: '',
  deliveryRequest: ''
})

// 결제 수단
const paymentMethod = ref('card')

// 컴포넌트 마운트 시 장바구니 데이터 로드
onMounted(async () => {
  try {
    isLoading.value = true
    cartData.value = await getCart()
    
    // 장바구니가 비어있으면 장바구니 페이지로 리다이렉트
    if (cartData.value.isEmpty) {
      router.push('/cart')
    }
    
    // 사용자 정보 불러와서 기본값 설정
    try {
      const userProfile = await getUserProfile()
      console.log('CheckoutView: 사용자 프로필 로드됨:', userProfile)
      
      // 기본 전화번호와 주소 정보가 있으면 자동으로 설정
      if (userProfile.phoneNumber) {
        deliveryInfo.value.phoneNumber = userProfile.phoneNumber
      }
      if (userProfile.defaultZipcode) {
        deliveryInfo.value.zipcode = userProfile.defaultZipcode
      }
      if (userProfile.defaultAddress1) {
        deliveryInfo.value.address = userProfile.defaultAddress1
      }
      if (userProfile.defaultAddress2) {
        deliveryInfo.value.detailAddress = userProfile.defaultAddress2
      }
      
      console.log('CheckoutView: 기본 주소 및 전화번호 설정 완료:', deliveryInfo.value)
    } catch (error) {
      console.error('사용자 정보 로드 실패:', error)
      // 오류가 발생해도 주문은 진행할 수 있도록 함
    }
  } catch (error) {
    console.error('장바구니 로드 실패:', error)
    router.push('/cart')
  } finally {
    isLoading.value = false
  }
})

// 장바구니 상태 실시간 감시
watch(() => cartStore.isEmpty, (isEmpty) => {
  console.log('CheckoutView: 장바구니 상태 변경 감지, isEmpty:', isEmpty)
  
  // 장바구니가 비어있으면 홈으로 리다이렉트
  if (isEmpty) {
    console.log('CheckoutView: 장바구니가 비어있어서 홈으로 리다이렉트')
    router.push('/')
  }
}, { immediate: false })

// 계산된 값들
const cartItems = computed(() => cartData.value?.items || [])
const storeInfo = computed(() => ({
  id: cartData.value?.storeId,
  name: cartData.value?.storeName,
  storeImageUrl: cartData.value?.storeImageUrl
}))
const orderSummary = computed(() => ({
  subtotal: cartData.value?.totalPrice || 0,
  deliveryFee: cartData.value?.deliveryFee || 0,
  total: cartData.value?.finalPrice || 0
}))

// 주문 생성
const placeOrder = async () => {
  if (!validateForm()) return
  
  try {
    isProcessing.value = true
    
    const orderRequest: CreateOrderRequest = {
      deliveryZipcode: deliveryInfo.value.zipcode,
      deliveryAddress1: deliveryInfo.value.address,
      deliveryAddress2: deliveryInfo.value.detailAddress,
      phoneNumber: deliveryInfo.value.phoneNumber,
      requests: deliveryInfo.value.deliveryRequest || undefined,
      paymentMethod: paymentMethod.value
    }
    
    const order = await createOrder(orderRequest)
    
    // 장바구니 상태 새로고침 (백엔드에서 자동으로 비워짐)
    await cartStore.loadCart()
    
    // 주문 완료 페이지로 이동
    router.push('/orders')
    
  } catch (error) {
    console.error('주문 생성 실패:', error)
    alert('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.')
  } finally {
    isProcessing.value = false
  }
}

// 폼 유효성 검사
const validateForm = (): boolean => {
  if (!deliveryInfo.value.address) {
    alert('배송 주소를 입력해주세요.')
    return false
  }
  
  if (!deliveryInfo.value.phoneNumber) {
    alert('전화번호를 입력해주세요.')
    return false
  }
  
  // 전화번호 형식 검사
  const phoneRegexes = [
    /^010-\d{4}-\d{4}$/,           // 휴대폰 (010-1234-5678)
    /^01[1-9]-\d{3,4}-\d{4}$/,     // 기타 휴대폰 (011~019)
    /^02-\d{3}-\d{4}$/,            // 서울 (02-123-4567)
    /^02-\d{4}-\d{4}$/,            // 서울 (02-1234-5678)
    /^0[3-6]\d-\d{3}-\d{4}$/,      // 지역번호 3자리 (031-123-4567)
    /^0[3-6]\d-\d{4}-\d{4}$/,      // 지역번호 4자리 (031-1234-5678)
    /^070-\d{3}-\d{4}$/,           // 인터넷전화 3자리 (070-123-4567)
    /^070-\d{4}-\d{4}$/,           // 인터넷전화 4자리 (070-1234-5678)
    /^050\d-\d{3}-\d{4}$/,         // 평생번호 3자리 (050X-123-4567)
    /^050\d-\d{4}-\d{4}$/          // 평생번호 4자리 (050X-1234-5678)
  ]
  
  const isValidPhone = phoneRegexes.some(regex => regex.test(deliveryInfo.value.phoneNumber))
  if (!isValidPhone) {
    alert('올바른 전화번호 형식이 아닙니다.\n예: 010-1234-5678, 02-123-4567, 031-123-4567')
    return false
  }
  
  return true
}

// 카카오 주소 검색
const searchAddress = () => {
  new (window as any).daum.Postcode({
    oncomplete: function(data: any) {
      // 도로명 주소를 기본으로 사용
      let fullAddress = data.address;
      let extraAddress = '';

      // 도로명 주소인 경우 건물명 추가
      if(data.userSelectedType === 'R'){
        if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
          extraAddress += data.bname;
        }
        if(data.buildingName !== '' && data.apartment === 'Y'){
          extraAddress += (extraAddress !== '' ? ', ' + data.buildingName : data.buildingName);
        }
        if(extraAddress !== ''){
          extraAddress = ' (' + extraAddress + ')';
        }
        fullAddress += extraAddress;
      }

      // 주소 정보 입력
      deliveryInfo.value.zipcode = data.zonecode; // 우편번호 저장
      deliveryInfo.value.address = fullAddress;
      
      // 상세 주소 입력 필드로 포커스 이동
      setTimeout(() => {
        const detailAddressInput = document.querySelector('input[placeholder="상세 주소를 입력하세요"]') as HTMLInputElement;
        if (detailAddressInput) {
          detailAddressInput.focus();
        }
      }, 100);
    },
    width: '100%',
    height: '100%',
    maxSuggestItems: 10
  }).open();
}

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 뒤로가기
const goBack = () => {
  router.back()
}

// 수량 증가
const increaseQuantity = async (item: any) => {
  const cartItemId = item.cartItemId
  updatingItems.value.add(cartItemId)
  
  try {
    await updateCartItem(cartItemId, { quantity: item.quantity + 1 })
    // 장바구니 다시 로드
    cartData.value = await getCart()
    // 전역 카트 스토어 업데이트
    await cartStore.loadCart()
  } catch (error) {
    console.error('수량 증가 실패:', error)
    alert('수량 변경에 실패했습니다.')
  } finally {
    updatingItems.value.delete(cartItemId)
  }
}

// 수량 감소
const decreaseQuantity = async (item: any) => {
  const cartItemId = item.cartItemId
  
  if (item.quantity <= 1) {
    // 수량이 1이면 삭제 확인
    if (confirm('이 메뉴를 장바구니에서 삭제하시겠습니까?')) {
      await removeItem(cartItemId)
    }
    return
  }
  
  updatingItems.value.add(cartItemId)
  
  try {
    await updateCartItem(cartItemId, { quantity: item.quantity - 1 })
    // 장바구니 다시 로드
    cartData.value = await getCart()
    // 전역 카트 스토어 업데이트
    await cartStore.loadCart()
    
    // 장바구니가 비어있으면 홈으로 리다이렉트
    if (cartData.value.isEmpty || cartData.value.totalItemCount === 0) {
      console.log('CheckoutView: 수량 감소 후 장바구니가 비어있어서 홈으로 리다이렉트')
      router.push('/')
    }
  } catch (error) {
    console.error('수량 감소 실패:', error)
    alert('수량 변경에 실패했습니다.')
  } finally {
    updatingItems.value.delete(cartItemId)
  }
}

// 아이템 삭제
const removeItem = async (cartItemId: number) => {
  updatingItems.value.add(cartItemId)
  
  try {
    await removeCartItem(cartItemId)
    // 장바구니 다시 로드
    cartData.value = await getCart()
    // 전역 카트 스토어 업데이트
    await cartStore.loadCart()
    
    // 장바구니가 비어있으면 홈으로 리다이렉트
    if (cartData.value.isEmpty) {
      router.push('/')
    }
  } catch (error) {
    console.error('아이템 삭제 실패:', error)
    alert('아이템 삭제에 실패했습니다.')
  } finally {
    updatingItems.value.delete(cartItemId)
  }
}

// 전화번호 포맷팅 (자동 하이픈 추가)
const formatPhoneNumber = (value: string) => {
  // 숫자만 추출
  const numbers = value.replace(/[^\d]/g, '')
  
  // 길이 제한 (최대 11자리)
  const limitedNumbers = numbers.slice(0, 11)
  
  // 포맷팅
  if (limitedNumbers.length <= 2) {
    return limitedNumbers
  }
  
  // 서울 지역번호 (02)
  if (limitedNumbers.startsWith('02')) {
    if (limitedNumbers.length <= 2) {
      return limitedNumbers
    } else if (limitedNumbers.length <= 5) {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2)}`
    } else if (limitedNumbers.length <= 9) {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2, 5)}-${limitedNumbers.slice(5)}`
    } else {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2, 6)}-${limitedNumbers.slice(6)}`
    }
  }
  
  // 휴대폰 및 기타 지역번호 (010, 031, 032 등)
  if (limitedNumbers.length <= 3) {
    return limitedNumbers
  } else if (limitedNumbers.length <= 6) {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3)}`
  } else if (limitedNumbers.length <= 10) {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3, 6)}-${limitedNumbers.slice(6)}`
  } else {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3, 7)}-${limitedNumbers.slice(7)}`
  }
}

// 전화번호 입력 핸들러
const handlePhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const formatted = formatPhoneNumber(target.value)
  deliveryInfo.value.phoneNumber = formatted
  
  // 커서 위치 조정 (Vue의 양방향 바인딩 때문에 필요)
  setTimeout(() => {
    target.value = formatted
  }, 0)
}
</script>

<template>
  <div class="checkout-page">
    
    <!-- 헤더 -->
    <header class="checkout-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
        </svg>
      </button>
      <h1 class="header-title">주문하기</h1>
    </header>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner"></div>
    </div>

    <!-- 결제 컨텐츠 -->
    <div v-else-if="cartData" class="checkout-content">
      
      <!-- 배송 정보 섹션 -->
      <section class="delivery-section">
        <h2 class="section-title">배송 정보</h2>
        
        <div class="form-group">
          <label class="form-label">배송 주소</label>
          <div class="address-input-group">
            <input 
              v-model="deliveryInfo.address"
              type="text"
              placeholder="주소를 입력하세요"
              class="form-input"
              readonly
              @click="searchAddress"
            >
            <button @click="searchAddress" class="search-button">
              주소 검색
            </button>
          </div>
          <input 
            v-model="deliveryInfo.detailAddress"
            type="text"
            placeholder="상세 주소를 입력하세요"
            class="form-input"
          >
        </div>
        
        <div class="form-group">
          <label class="form-label">전화번호</label>
          <input 
            :value="deliveryInfo.phoneNumber"
            @input="handlePhoneInput"
            type="tel"
            placeholder="010-1234-5678"
            class="form-input"
            maxlength="13"
          >
        </div>
        
        <div class="form-group">
          <label class="form-label">배송 요청사항</label>
          <textarea 
            v-model="deliveryInfo.deliveryRequest"
            placeholder="배송 시 요청사항을 입력하세요"
            class="form-textarea"
            rows="3"
          ></textarea>
        </div>
      </section>

      <!-- 주문 상품 섹션 -->
      <section class="order-items-section">
        <h2 class="section-title">주문 상품</h2>
        
        <div class="store-info">
          <h3 class="store-name">{{ storeInfo?.name }}</h3>
        </div>
        
        <div class="order-items">
          <div 
            v-for="item in cartItems"
            :key="item.cartItemId"
            class="order-item"
          >
            <div class="item-info">
              <h4 class="item-name">{{ item.menuName }}</h4>
              <div class="item-controls">
                <div class="quantity-controls">
                  <button 
                    @click="decreaseQuantity(item)"
                    :disabled="updatingItems.has(item.cartItemId)"
                    class="quantity-button"
                    :class="{ 'delete-button': item.quantity === 1 }"
                  >
                    <svg v-if="item.quantity > 1" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13H5v-2h14v2z"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                  </button>
                  <span class="quantity-display">{{ item.quantity }}</span>
                  <button 
                    @click="increaseQuantity(item)"
                    :disabled="updatingItems.has(item.cartItemId)"
                    class="quantity-button"
                  >
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                    </svg>
                  </button>
                </div>
                <div class="item-price">
                  {{ formatPrice(item.menuPrice * item.quantity) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 결제 수단 섹션 -->
      <section class="payment-section">
        <h2 class="section-title">결제 수단</h2>
        
        <div class="payment-methods">
          <label class="payment-option">
            <input 
              v-model="paymentMethod"
              type="radio"
              value="card"
              class="payment-radio"
            >
            <span class="payment-label">신용/체크카드</span>
          </label>
          
          <label class="payment-option">
            <input 
              v-model="paymentMethod"
              type="radio"
              value="cash"
              class="payment-radio"
            >
            <span class="payment-label">현금</span>
          </label>
        </div>
      </section>

      <!-- 결제 금액 섹션 -->
      <section class="payment-summary-section">
        <h2 class="section-title">결제 금액</h2>
        
        <div class="payment-summary">
          <div class="summary-row">
            <span class="summary-label">상품금액</span>
            <span class="summary-value">{{ formatPrice(orderSummary?.subtotal || 0) }}</span>
          </div>
          
          <div class="summary-row">
            <span class="summary-label">배달비</span>
            <span class="summary-value">{{ formatPrice(orderSummary?.deliveryFee || 0) }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row total-row">
            <span class="summary-label">총 결제금액</span>
            <span class="summary-value total-value">{{ formatPrice(orderSummary?.total || 0) }}</span>
          </div>
        </div>
      </section>

    </div>

    <!-- 결제하기 버튼 -->
    <div v-if="!isLoading && cartData" class="checkout-footer">
      <button 
        @click="placeOrder"
        :disabled="isProcessing"
        class="place-order-button"
        :class="{ 'processing': isProcessing }"
      >
        <span v-if="!isProcessing" class="button-text">
          {{ formatPrice(orderSummary?.total || 0) }} 결제하기
        </span>
        <span v-else class="button-text">처리 중...</span>
      </button>
    </div>

  </div>
</template>

<style scoped>
/* 결제 페이지 컨테이너 */
.checkout-page {
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 100px;
}

/* 헤더 */
.checkout-header {
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
  justify-content: center;
  align-items: center;
  height: 400px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 결제 컨텐츠 */
.checkout-content {
  padding: 20px 16px;
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

/* 폼 요소 */
.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  resize: vertical;
  transition: border-color 0.2s;
}

.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
}

/* 주소 입력 */
.address-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.search-button {
  flex-shrink: 0;
  padding: 12px 20px;
  background-color: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-button:hover {
  background-color: #e5e7eb;
}

/* 주문 상품 */
.store-info {
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 16px;
}

.store-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info {
  width: 100%;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.item-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quantity-button {
  width: 28px;
  height: 28px;
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

.quantity-button.delete-button {
  border-color: #ef4444;
  color: #ef4444;
}

.quantity-button.delete-button:hover:not(:disabled) {
  background-color: #fef2f2;
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

.item-price {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

/* 결제 수단 */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-option:has(:checked) {
  border-color: #3b82f6;
  background-color: #eff6ff;
}

.payment-radio {
  margin-right: 12px;
  accent-color: #3b82f6;
}

.payment-label {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
}

/* 결제 금액 */
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

/* 결제하기 버튼 */
.checkout-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #e5e7eb;
  padding: 16px;
  z-index: 100;
}

.place-order-button {
  width: 100%;
  padding: 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.place-order-button:hover:not(:disabled) {
  background-color: #2563eb;
}

.place-order-button:disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
}

.place-order-button.processing {
  background-color: #9ca3af;
}

.button-text {
  display: block;
}

/* 반응형 */
@media (min-width: 768px) {
  .checkout-content {
    max-width: 600px;
    margin: 0 auto;
    padding: 24px;
  }
}
</style>