<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStore, type Store, type Menu, type StoreDetailResponse } from '../api/stores'
import { addCartItem, clearCart, type AddCartItemRequest } from '../api/cart'
import { toggleWishlist, getWishlistStatus } from '../api/wishlist'
import { useCartStore } from '../stores/cart'
import { useAuthStore } from '../stores/auth'
import { parseOperatingHours } from '../utils/operatingHours'
import { getStoreOperatingStatus } from '@/utils/storeStatus'
import MenuItemModal from '../components/MenuItemModal.vue'
import AlertModal from '../components/AlertModal.vue'

// 라우터
const route = useRoute()
const router = useRouter()

// 카트 스토어
const cartStore = useCartStore()

// 상태 관리
const store = ref<Store | null>(null)
const menus = ref<Menu[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const selectedCategory = ref<string>('전체')

// 가게 ID
const storeId = ref<number>(parseInt(route.params.id as string))

// 장바구니 상태
const isAddingToCart = ref(false)

// 찜 상태
const isWished = ref(false)
const isTogglingWish = ref(false)

// 메뉴 카테고리 (현재는 전체만 지원)
const menuCategories = computed(() => {
  return ['전체']
})

// 선택된 메뉴 아이템
const selectedMenuItem = ref<Menu | null>(null)
const isMenuModalVisible = ref(false)

// 컴포넌트 마운트 시 가게 정보 및 장바구니 로드
onMounted(async () => {
  await loadStore()
  await cartStore.loadCart()
  await loadWishlistStatus()
})

// 가게 정보 로드
const loadStore = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    console.log('StoreDetailView: 가게 정보 로드 시작, storeId:', storeId.value)
    const storeDetailData: StoreDetailResponse = await getStore(storeId.value)
    console.log('StoreDetailView: 가게 정보 로드 응답:', storeDetailData)
    
    // 백엔드 응답 구조에 맞게 수정: storeDetailData 자체가 가게 정보
    store.value = {
      id: storeDetailData.storeId,
      storeId: storeDetailData.storeId,
      name: storeDetailData.name,
      description: storeDetailData.description,
      zipcode: storeDetailData.zipcode,
      address1: storeDetailData.address1,
      address2: storeDetailData.address2,
      phoneNumber: storeDetailData.phoneNumber,
      storeImageUrl: storeDetailData.storeImageUrl,
      categoryId: storeDetailData.categoryId,
      minOrderAmount: storeDetailData.minOrderAmount,
      deliveryFee: storeDetailData.deliveryFee,
      operatingHours: storeDetailData.operatingHours,
      status: storeDetailData.status,
      averageRating: storeDetailData.averageRating,
      reviewCount: storeDetailData.reviewCount,
      createdAt: storeDetailData.createdAt,
      updatedAt: storeDetailData.updatedAt
    }
    menus.value = storeDetailData.menus || []
    
    console.log('StoreDetailView: 가게 정보 설정 완료:', store.value)
    console.log('StoreDetailView: 메뉴 목록 설정 완료:', menus.value.length, '개')
    
  } catch (err) {
    console.error('StoreDetailView: 가게 정보 로드 실패:', err)
    error.value = '가게 정보를 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 뒤로가기
const goBack = () => {
  router.back()
}

// 카테고리 선택
const selectCategory = (category: string) => {
  selectedCategory.value = category
}

// 필터된 메뉴 목록
const filteredMenuItems = computed(() => {
  if (selectedCategory.value === '전체') {
    return menus.value
  }
  // MVP에서는 단순하게 처리 (실제로는 메뉴 카테고리 필드가 있어야 함)
  return menus.value
})

// 메뉴 아이템 클릭
const handleMenuClick = (menuItem: Menu) => {
  // 우선순위 1: 영업 상태 체크 (영업중이 아니면 품절 여부와 관계없이 영업 안내)
  if (store.value) {
    const storeStatus = getStoreOperatingStatus(store.value)
    if (!storeStatus.isOpen) {
      alertMessage.value = `죄송합니다. ${store.value.name}은(는) 현재 영업중이 아닙니다.`
      showOperatingHoursAlert.value = true
      return
    }
  }
  
  // 우선순위 2: 영업중이지만 품절된 메뉴는 클릭 불가능
  if (menuItem.status === 'SOLD_OUT') {
    alertMessage.value = `죄송합니다. "${menuItem.name}"은(는) 현재 품절되었습니다.`
    showOperatingHoursAlert.value = true
    return
  }
  
  selectedMenuItem.value = menuItem
  isMenuModalVisible.value = true
}

// 영업시간 알림 상태
const showOperatingHoursAlert = ref(false)
const alertMessage = ref('')

// 장바구니에 추가
const addToCart = async (menuItem: Menu, options: any = {}) => {
  console.log('StoreDetailView: addToCart 함수 호출됨')
  console.log('StoreDetailView: 메뉴 아이템:', menuItem)
  console.log('StoreDetailView: 옵션:', options)
  console.log('StoreDetailView: 가게 정보:', store.value)
  
  // 로그인 체크
  const authStore = useAuthStore()
  if (!authStore.requireAuth()) {
    // 현재 페이지 경로를 저장하고 로그인 페이지로 이동
    authStore.setRedirectPath(route.fullPath)
    router.push('/login')
    return
  }

  // 영업 상태 체크
  if (store.value) {
    const storeStatus = getStoreOperatingStatus(store.value)
    if (!storeStatus.isOpen) {
      alertMessage.value = `죄송합니다. ${store.value.name}은(는) 현재 영업중이 아닙니다.`
      showOperatingHoursAlert.value = true
      return
    }
  }
  
  if (!store.value) {
    console.error('StoreDetailView: 가게 정보가 없습니다')
    return
  }
  
  const cartRequest: AddCartItemRequest = {
    menuId: menuItem.menuId,
    quantity: options.quantity || 1
  }
  
  try {
    isAddingToCart.value = true
    
    console.log('StoreDetailView: 장바구니 담기 요청:', cartRequest)
    const result = await addCartItem(cartRequest)
    console.log('StoreDetailView: 장바구니 담기 성공:', result)
    
    // 모달 닫기
    isMenuModalVisible.value = false
    
    // 장바구니 상태 업데이트
    await cartStore.loadCart()
    
  } catch (err: any) {
    console.error('장바구니 추가 실패:', err)
    console.error('에러 상세:', {
      status: err.response?.status,
      message: err.response?.data?.message,
      data: err.response?.data
    })
    
    // 500 에러에서 다른 가게 메뉴 메시지가 포함된 경우
    if (err.response?.status === 500 && err.response?.data?.message?.includes('다른 가게')) {
      const confirmClear = confirm('다른 가게의 메뉴가 장바구니에 있습니다.\n기존 장바구니를 비우고 새로 담으시겠습니까?')
      if (confirmClear) {
        try {
          console.log('장바구니 비우기 시작...')
          
          // 기존 장바구니 비우기
          await clearCart()
          console.log('기존 장바구니 비우기 완료')
          
          // 장바구니 상태 새로고침
          await cartStore.loadCart()
          console.log('장바구니 상태 새로고침 완료')
          
          // 다시 메뉴 추가
          console.log('새 메뉴 추가 시작...', cartRequest)
          const result = await addCartItem(cartRequest)
          console.log('새 메뉴 추가 성공:', result)
          
          // 모달 닫기 및 장바구니 상태 업데이트
          isMenuModalVisible.value = false
          await cartStore.loadCart()
          console.log('최종 장바구니 상태 업데이트 완료')
          
        } catch (clearErr: any) {
          console.error('장바구니 비우기 또는 재추가 실패:', clearErr)
          console.error('비우기/재추가 에러 상세:', {
            status: clearErr.response?.status,
            message: clearErr.response?.data?.message,
            data: clearErr.response?.data
          })
          alert(`장바구니 처리 중 오류가 발생했습니다.\n오류: ${clearErr.response?.data?.message || clearErr.message}`)
        }
      }
    } else {
      const errorMessage = err.response?.data?.message || err.message || '알 수 없는 오류'
      alert(`장바구니에 담는데 실패했습니다.\n오류: ${errorMessage}`)
    }
  } finally {
    isAddingToCart.value = false
  }
}

// 결제 페이지로 이동 (카트 보기 버튼)
const viewCart = () => {
  router.push('/order')
}

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 운영시간 파싱
const formattedOperatingHours = computed(() => {
  return store.value?.operatingHours ? parseOperatingHours(store.value.operatingHours) : '운영시간 미정'
})

// 운영 상태 (사장님 설정 + 운영시간 고려)
const operatingStatus = computed(() => {
  if (!store.value) return { isOpen: false, displayStatus: '운영시간 미정', statusClass: 'closed' }
  return getStoreOperatingStatus(store.value)
})

// 찜 상태 로드
const loadWishlistStatus = async () => {
  try {
    console.log('StoreDetailView: 찜 상태 로드 시작, storeId:', storeId.value)
    isWished.value = await getWishlistStatus(storeId.value)
    console.log('StoreDetailView: 찜 상태 로드 완료:', isWished.value)
  } catch (err: any) {
    console.error('StoreDetailView: 찜 상태 로드 실패:', err)
    console.error('에러 상세:', {
      status: err.response?.status,
      message: err.response?.data?.message,
      data: err.response?.data
    })
    // 로그인하지 않은 경우 등에서는 false로 설정
    isWished.value = false
  }
}

// 찜 토글
const toggleWish = async () => {
  if (isTogglingWish.value) return
  
  // 로그인 체크
  const authStore = useAuthStore()
  if (!authStore.requireAuth()) {
    // 현재 페이지 경로를 저장하고 로그인 페이지로 이동
    authStore.setRedirectPath(route.fullPath)
    router.push('/login')
    return
  }
  
  try {
    isTogglingWish.value = true
    console.log('StoreDetailView: 찜 토글 시작, 현재 상태:', isWished.value)
    
    const response = await toggleWishlist(storeId.value)
    console.log('StoreDetailView: 찜 토글 API 응답:', response)
    
    // API 응답에 따라 상태 업데이트 (백엔드에서 wished를 반환함)
    isWished.value = response.wished
    
    console.log('StoreDetailView: 찜 토글 완료, 새로운 상태:', isWished.value)
    
    // 사용자에게 피드백 제공
    if (response.wished) {
      console.log('찜 목록에 추가되었습니다:', store.value?.name || '이 가게')
    } else {
      console.log('찜 목록에서 제거되었습니다:', store.value?.name || '이 가게')
    }
    
  } catch (err: any) {
    console.error('StoreDetailView: 찜 토글 실패:', err)
    console.error('토글 에러 상세:', {
      status: err.response?.status,
      message: err.response?.data?.message,
      data: err.response?.data
    })
    
    if (err.response?.status === 401 || err.response?.status === 403) {
      alert('찜 기능을 사용하려면 로그인이 필요합니다.')
    } else {
      alert('찜 처리 중 오류가 발생했습니다.')
    }
  } finally {
    isTogglingWish.value = false
  }
}

</script>

<template>
  <!-- 가게 상세 페이지 -->
  <div class="store-detail">
    
    <!-- 가게 헤더 이미지 -->
    <section class="store-header">
      <div class="header-image-container">
        <img 
          v-if="store?.storeImageUrl" 
          :src="store.storeImageUrl" 
          :alt="store.name"
          class="header-image"
        >
        <div v-else class="header-image-placeholder">
          <span class="placeholder-emoji">🏪</span>
        </div>
        
        <!-- 네비게이션 버튼 -->
        <div class="header-nav">
          <button @click="goBack" class="nav-button">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
          </button>
          <div class="nav-actions">
            <button 
              @click="toggleWish" 
              :disabled="isTogglingWish"
              class="nav-button wishlist-button"
              :class="{ 'wished': isWished, 'loading': isTogglingWish }"
            >
              <svg viewBox="0 0 24 24" :fill="isWished ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 가게 정보 -->
    <section v-if="store" class="store-info">
      <div class="section-container">
        <div class="store-basic-info">
          <h1 class="store-name">{{ store.name }}</h1>
          <p class="store-description">{{ store.description }}</p>
          
          <div class="store-details">
            <div class="detail-item">
              <span class="detail-icon">🕒</span>
              <span class="detail-text">
                {{ formattedOperatingHours }}
                <span 
                  class="operating-status" 
                  :class="operatingStatus.statusClass"
                >
                  {{ operatingStatus.displayStatus }}
                </span>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-icon">🚚</span>
              <span class="detail-text">
                배달비 {{ store.deliveryFee === 0 ? '무료' : formatPrice(store.deliveryFee) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-icon">📦</span>
              <span class="detail-text">최소주문 {{ formatPrice(store.minOrderAmount) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 메뉴 카테고리 -->
    <section class="menu-categories">
      <div class="section-container">
        <div class="category-tabs">
          <button 
            v-for="category in menuCategories"
            :key="category"
            @click="selectCategory(category)"
            class="category-tab"
            :class="{ 'active': selectedCategory === category }"
          >
            {{ category }}
          </button>
        </div>
      </div>
    </section>

    <!-- 메뉴 목록 -->
    <section class="menu-list">
      <div class="section-container">
        <div v-if="filteredMenuItems.length > 0" class="menu-items">
          <div 
            v-for="menuItem in filteredMenuItems"
            :key="menuItem.menuId"
            class="menu-item"
            :class="{ 'sold-out': menuItem.status === 'SOLD_OUT' }"
            @click="handleMenuClick(menuItem)"
          >
            <div class="menu-image-container">
              <img 
                v-if="menuItem.menuImageUrl" 
                :src="menuItem.menuImageUrl" 
                :alt="menuItem.name"
                class="menu-image"
              >
              <div v-else class="menu-image-placeholder">
                <span class="placeholder-text">🍽️</span>
              </div>
              <!-- 품절 표시 -->
              <div v-if="menuItem.status === 'SOLD_OUT'" class="sold-out-badge">품절</div>
            </div>
            
            <div class="menu-content">
              <h3 class="menu-name">{{ menuItem.name }}</h3>
              <p class="menu-description">{{ menuItem.description }}</p>
              <div class="menu-price">
                <span class="current-price">{{ formatPrice(menuItem.price) }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 메뉴 없음 -->
        <div v-else class="no-menus">
          <div class="no-menus-icon">🍽️</div>
          <h3 class="no-menus-title">등록된 메뉴가 없습니다</h3>
          <p class="no-menus-description">곧 맛있는 메뉴로 찾아뵙겠습니다!</p>
        </div>
      </div>
    </section>

    <!-- 하단 고정 카트 바 -->
    <div v-if="!cartStore.isEmpty" class="cart-floating">
      <button @click="viewCart" class="cart-button">
        <div class="cart-content">
          <div class="cart-info">
            <span class="cart-count">{{ cartStore.itemCount }}개</span>
            <span class="cart-total">{{ formatPrice(cartStore.totalPrice) }}</span>
          </div>
          <span class="cart-text">카트 보기</span>
        </div>
      </button>
    </div>

    <!-- 메뉴 아이템 모달 -->
    <MenuItemModal 
      :menu-item="selectedMenuItem"
      :is-visible="isMenuModalVisible"
      @close="isMenuModalVisible = false"
      @add-to-cart="addToCart"
    />

    <!-- 영업시간 알림 모달 -->
    <AlertModal
      :is-visible="showOperatingHoursAlert"
      title="영업시간 안내"
      :message="alertMessage"
      type="warning"
      confirm-text="확인"
      @close="showOperatingHoursAlert = false"
    />

  </div>
</template>

<style scoped>
/* 가게 상세 컨테이너 */
.store-detail {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding-bottom: 100px; /* 플로팅 버튼을 위한 여백 */
}

/* 공통 섹션 컨테이너 */
.section-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

/* 가게 헤더 이미지 */
.store-header {
  position: relative;
  width: 100%;
  height: 300px;
}

.header-image-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.header-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
}

.placeholder-emoji {
  font-size: 80px;
  opacity: 0.6;
}

/* 헤더 네비게이션 */
.header-nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), transparent);
}

.nav-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #374151;
}

.nav-button:hover {
  background-color: rgba(255, 255, 255, 1);
  transform: scale(1.05);
}

.nav-button svg {
  width: 20px;
  height: 20px;
}

/* 찜 버튼 스타일 */
.wishlist-button {
  transition: all 0.3s ease;
  color: white;
}

.wishlist-button:hover {
  background-color: rgba(255, 255, 255, 1);
  color: #374151;
}

.wishlist-button.wished {
  background-color: rgba(255, 255, 255, 0.95);
  color: #ef4444 !important;
}

.wishlist-button.wished svg {
  fill: #ef4444 !important;
  stroke: #ef4444 !important;
}

.wishlist-button.loading {
  opacity: 0.6;
  pointer-events: none;
}

.wishlist-button:not(.wished) svg {
  fill: none !important;
  stroke: white !important;
}

.nav-actions {
  display: flex;
  gap: 8px;
}

/* 가게 정보 */
.store-info {
  background-color: white;
  padding: 2rem 0;
  border-bottom: 1px solid #f1f3f4;
}

.store-basic-info {
  text-align: center;
}

.store-name {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12px;
}

.store-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 16px;
}

.star {
  font-size: 16px;
}

.rating {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.review-count {
  font-size: 14px;
  color: #6b7280;
}

.store-description {
  font-size: 16px;
  color: #6b7280;
  margin-bottom: 24px;
  line-height: 1.5;
}

.store-details {
  display: flex;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
}

.detail-icon {
  font-size: 16px;
}

/* 운영 상태 */
.operating-status {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.operating-status.open {
  background-color: #dcfce7;
  color: #166534;
}

.operating-status.closed {
  background-color: #fee2e2;
  color: #dc2626;
}

/* 메뉴 카테고리 */
.menu-categories {
  background-color: white;
  padding: 1rem 0;
  border-bottom: 1px solid #f1f3f4;
  position: sticky;
  top: 56px;
  z-index: 40;
}

.category-tabs {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.category-tab {
  flex-shrink: 0;
  padding: 12px 20px;
  border: 1px solid #e5e7eb;
  border-radius: 25px;
  background-color: white;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-tab:hover {
  border-color: #d1d5db;
  background-color: #f9fafb;
}

.category-tab.active {
  background-color: #374151;
  border-color: #374151;
  color: white;
}

/* 메뉴 목록 */
.menu-list {
  background-color: white;
  padding: 2rem 0;
}

.menu-items {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.menu-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: #f9fafb;
}

.menu-item.sold-out {
  opacity: 0.6;
  cursor: not-allowed;
}

.menu-item.sold-out:hover {
  background-color: transparent;
}

.menu-image-container {
  position: relative;
  flex-shrink: 0;
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
}

.menu-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.menu-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
}

.placeholder-text {
  font-size: 36px;
  opacity: 0.6;
}

.sold-out-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 8px;
  background-color: #6b7280;
  color: white;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.menu-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.menu-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.4;
  margin-bottom: auto;
}

.menu-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.original-price {
  font-size: 14px;
  color: #9ca3af;
  text-decoration: line-through;
}

.current-price {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* 메뉴 없음 상태 */
.no-menus {
  text-align: center;
  padding: 4rem 2rem;
}

.no-menus-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.no-menus-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.no-menus-description {
  font-size: 14px;
  color: #6b7280;
}

/* 하단 고정 카트 바 */
.cart-floating {
  position: fixed;
  bottom: 80px; /* 하단 네비게이션 바(56px) + 여백(24px) */
  left: 0;
  right: 0;
  padding: 0 1.5rem;
  z-index: 50;
}

.cart-button {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.3);
  font-weight: 600;
}

.cart-button:hover {
  background-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(59, 130, 246, 0.4);
}

.cart-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.cart-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-count {
  font-size: 14px;
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 12px;
}

.cart-total {
  font-size: 16px;
  font-weight: 700;
}

.cart-text {
  font-size: 16px;
  font-weight: 600;
}


/* 반응형 */
@media (max-width: 768px) {
  .store-details {
    flex-direction: column;
    gap: 1rem;
  }
  
  .detail-item {
    justify-content: center;
  }
  
  .menu-item {
    flex-direction: column;
    text-align: center;
  }
  
  .menu-image-container {
    width: 100%;
    height: 200px;
  }
  
  .menu-content {
    text-align: left;
    margin-top: 1rem;
  }
}
</style>