import apiClient from './client'

// 주문 상태 타입
export type OrderStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'DELIVERING' | 'COMPLETED'

// 주문 아이템 타입
export interface OrderItem {
  orderItemId: number
  menuId: number
  menuName: string
  menuDescription?: string
  menuImageUrl?: string
  quantity: number
  priceAtOrder: number
  itemTotalPrice: number
}

// 주문 목록 타입 (간략 정보)
export interface OrderListItem {
  orderId: number
  storeId: number
  storeName: string
  storeImageUrl?: string
  status: OrderStatus
  statusDisplayName: string
  totalPrice: number
  representativeMenuName: string
  totalMenuCount: number
  totalQuantity: number
  orderedAt: string
}

// 주문 상세 타입 (전체 정보)
export interface Order {
  orderId: number
  userId: number
  storeId: number
  storeName: string
  storeImageUrl?: string
  status: OrderStatus
  statusDisplayName?: string
  subtotalAmount: number
  deliveryFee: number
  totalPrice: number
  deliveryZipcode: string
  deliveryAddress1: string
  deliveryAddress2?: string
  fullDeliveryAddress?: string
  requests?: string
  paymentMethod: string
  paymentTransactionId?: string
  orderItems: OrderItem[]
  totalItemCount?: number
  totalQuantity?: number
  createdAt: string
  updatedAt: string
}

// 주문 목록 응답 타입
export interface OrderPageResponse {
  content: OrderListItem[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 주문 생성 요청 타입
export interface CreateOrderRequest {
  deliveryZipcode?: string
  deliveryAddress1: string
  deliveryAddress2?: string
  phoneNumber?: string
  requests?: string
  paymentMethod?: string
}

// 주문 생성 응답 타입
export interface CreateOrderResponse {
  orderId: number
  status: OrderStatus
  finalAmount: number
  message: string
}

// 주문 목록 조회
export const getOrders = async (page: number = 0, size: number = 20): Promise<OrderPageResponse> => {
  try {
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort: 'id,desc'
    })
    
    const response = await apiClient.get(`/api/orders?${queryParams}`)
    return response.data
  } catch (error) {
    console.error('주문 목록 조회 실패:', error)
    throw error
  }
}

// 주문 상세 조회
export const getOrder = async (orderId: number): Promise<Order> => {
  try {
    const response = await apiClient.get(`/api/orders/${orderId}`)
    return response.data
  } catch (error) {
    console.error('주문 상세 조회 실패:', error)
    throw error
  }
}

// 주문 생성
export const createOrder = async (request: CreateOrderRequest): Promise<CreateOrderResponse> => {
  try {
    const response = await apiClient.post('/api/orders', request)
    return response.data
  } catch (error) {
    console.error('주문 생성 실패:', error)
    throw error
  }
}