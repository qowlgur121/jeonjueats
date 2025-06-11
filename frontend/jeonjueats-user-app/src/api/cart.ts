import apiClient from './client'

// 장바구니 아이템 타입 정의
export interface CartItem {
  id: number
  cartItemId: number
  menuId: number
  menuName: string
  menuDescription: string
  menuPrice: number
  menuImageUrl?: string
  quantity: number
  createdAt: string
  updatedAt: string
}

// 장바구니 응답 타입 (백엔드 CartResponseDto와 일치)
export interface CartResponse {
  cartId: number
  storeId?: number
  storeName?: string
  storeImageUrl?: string
  items: CartItem[]
  totalItemCount: number
  totalQuantity: number
  totalPrice: number
  deliveryFee: number
  finalPrice: number
  isEmpty: boolean
}

// 장바구니 아이템 추가 요청 타입
export interface AddCartItemRequest {
  menuId: number
  quantity: number
}

// 장바구니 조회
export const getCart = async (): Promise<CartResponse> => {
  try {
    const response = await apiClient.get('/api/carts')
    return response.data
  } catch (error) {
    console.error('장바구니 조회 실패:', error)
    throw error
  }
}

// 장바구니에 아이템 추가
export const addCartItem = async (request: AddCartItemRequest): Promise<CartItem> => {
  try {
    const response = await apiClient.post('/api/carts/items', request)
    return response.data
  } catch (error) {
    console.error('장바구니 아이템 추가 실패:', error)
    throw error
  }
}

// 장바구니 아이템 삭제
export const removeCartItem = async (cartItemId: number): Promise<void> => {
  try {
    await apiClient.delete(`/api/carts/items/${cartItemId}`)
  } catch (error) {
    console.error('장바구니 아이템 삭제 실패:', error)
    throw error
  }
}

// 장바구니 아이템 수량 업데이트 요청 타입
export interface UpdateCartItemRequest {
  quantity: number
}

// 장바구니 아이템 수량 업데이트
export const updateCartItem = async (cartItemId: number, request: UpdateCartItemRequest): Promise<CartItem> => {
  try {
    const response = await apiClient.put(`/api/carts/items/${cartItemId}`, request)
    return response.data
  } catch (error) {
    console.error('장바구니 아이템 수량 업데이트 실패:', error)
    throw error
  }
}

// 장바구니 전체 비우기
export const clearCart = async (): Promise<void> => {
  try {
    await apiClient.delete('/api/carts')
  } catch (error) {
    console.error('장바구니 비우기 실패:', error)
    throw error
  }
}