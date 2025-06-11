import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, type CartResponse } from '../api/cart'

export const useCartStore = defineStore('cart', () => {
  // 상태 관리
  const cartData = ref<CartResponse | null>(null)
  const isLoading = ref(false)

  // 계산된 속성
  const isEmpty = computed(() => {
    if (!cartData.value) return true
    
    // 아이템 개수로 직접 판단 (더 안전한 방법)
    const hasNoItems = cartData.value.totalItemCount === 0
    const emptyItemsArray = cartData.value.items.length === 0
    const backendEmptyFlag = cartData.value.isEmpty
    
    // 백엔드 플래그와 실제 데이터 불일치 감지
    if (hasNoItems !== backendEmptyFlag) {
      console.warn('CartStore: isEmpty 불일치 감지', {
        hasNoItems,
        backendEmptyFlag,
        totalItemCount: cartData.value.totalItemCount,
        itemsLength: cartData.value.items.length
      })
    }
    
    // 실제 아이템 데이터 기준으로 판단
    return hasNoItems || emptyItemsArray
  })
  const itemCount = computed(() => cartData.value?.totalItemCount || 0)
  const totalPrice = computed(() => cartData.value?.finalPrice || 0)
  const storeInfo = computed(() => cartData.value ? {
    storeId: cartData.value.storeId,
    name: cartData.value.storeName,
    storeImageUrl: cartData.value.storeImageUrl,
    deliveryFee: cartData.value.deliveryFee
  } : null)

  // 장바구니 로드
  const loadCart = async (): Promise<void> => {
    try {
      isLoading.value = true
      console.log('CartStore: 장바구니 로드 시작')
      
      const response = await getCart()
      console.log('CartStore: 장바구니 로드 응답:', response)
      
      cartData.value = response
      
      console.log('CartStore: 장바구니 상태 업데이트 완료')
      console.log('CartStore: 백엔드 isEmpty 값:', response.isEmpty)
      console.log('CartStore: 아이템 수:', itemCount.value)
      console.log('CartStore: 총 금액:', totalPrice.value)
      console.log('CartStore: 계산된 isEmpty:', isEmpty.value)
      
    } catch (error) {
      console.error('CartStore: 장바구니 로드 실패:', error)
      // 에러 시 빈 장바구니로 초기화
      cartData.value = null
    } finally {
      isLoading.value = false
    }
  }

  // 장바구니 초기화 (로그아웃 시 등)
  const clearCartData = (): void => {
    cartData.value = null
  }

  return {
    // 상태
    cartData,
    isLoading,
    // 계산된 속성  
    isEmpty,
    itemCount,
    totalPrice,
    storeInfo,
    // 메서드
    loadCart,
    clearCartData
  }
})