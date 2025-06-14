import apiClient from './client'

// 가게 정보 타입
export interface Store {
  id: number
  name: string
  description: string
  phoneNumber: string
  address: string
  categoryId: number
  categoryName: string
  minOrderAmount: number
  deliveryFee: number
  status: 'OPEN' | 'CLOSED'
  storeImageUrl?: string
  ownerId: number
  ownerNickname: string
  createdAt: string
  updatedAt: string
}

// 가게 생성 요청 타입
export interface StoreCreateRequest {
  name: string
  description: string
  phoneNumber: string
  address: string
  categoryId: number
  minOrderAmount: number
  deliveryFee: number
  storeImageUrl?: string
}

// 가게 수정 요청 타입
export interface StoreUpdateRequest {
  name?: string
  description?: string
  phoneNumber?: string
  address?: string
  categoryId?: number
  minOrderAmount?: number
  deliveryFee?: number
  storeImageUrl?: string
}

// 사장님 가게 목록 조회
export const getOwnerStores = async (): Promise<Store[]> => {
  const response = await apiClient.get('/api/owner/stores')
  return response.data
}

// 가게 생성
export const createStore = async (data: StoreCreateRequest): Promise<Store> => {
  const response = await apiClient.post('/api/owner/stores', data)
  return response.data
}

// 가게 정보 수정
export const updateStore = async (storeId: number, data: StoreUpdateRequest): Promise<Store> => {
  const response = await apiClient.put(`/api/owner/stores/${storeId}`, data)
  return response.data
}

// 가게 운영 상태 토글
export const toggleStoreOperation = async (storeId: number): Promise<Store> => {
  const response = await apiClient.post(`/api/owner/stores/${storeId}/toggle-operation`)
  return response.data
}