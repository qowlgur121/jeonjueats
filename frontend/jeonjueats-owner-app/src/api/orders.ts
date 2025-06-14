import apiClient from './client'

// 주문 상품 타입
export interface OrderItem {
  id: number
  menuId: number
  menuName: string
  quantity: number
  price: number
}

// 주문 정보 타입
export interface Order {
  id: number
  storeId: number
  storeName: string
  userId: number
  userNickname: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'DELIVERING' | 'COMPLETED'
  totalAmount: number
  deliveryAddress: string
  phoneNumber: string
  items: OrderItem[]
  createdAt: string
  updatedAt: string
}

// 주문 상태 업데이트 요청 타입
export interface OrderStatusUpdateRequest {
  status: 'ACCEPTED' | 'REJECTED' | 'DELIVERING' | 'COMPLETED'
}

// 가게 주문 목록 조회
export const getStoreOrders = async (storeId: number, status?: string): Promise<Order[]> => {
  const params = status ? { status } : {}
  const response = await apiClient.get(`/api/owner/stores/${storeId}/orders`, { params })
  return response.data
}

// 주문 상태 업데이트
export const updateOrderStatus = async (storeId: number, orderId: number, data: OrderStatusUpdateRequest): Promise<Order> => {
  const response = await apiClient.patch(`/api/owner/stores/${storeId}/orders/${orderId}/status`, data)
  return response.data
}