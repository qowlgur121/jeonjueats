import apiClient from './client'

// 가게 정보 타입 (백엔드 StoreResponseDto와 일치)
export interface Store {
  storeId: number
  name: string
  description: string
  zipcode: string
  address1: string
  address2?: string
  phoneNumber: string
  storeImageUrl?: string
  categoryId: number
  minOrderAmount: number
  deliveryFee: number
  status: 'OPEN' | 'CLOSED'
  averageRating: number
  reviewCount: number
  operatingHours?: string
  createdAt: string
  updatedAt: string
}

// 가게 생성 요청 타입 (백엔드 StoreCreateRequestDto와 일치)
export interface StoreCreateRequest {
  name: string
  zipcode: string
  address1: string
  address2?: string
  phoneNumber: string
  description?: string
  storeImageUrl?: string
  categoryId: number
  minOrderAmount: number
  deliveryFee: number
  operatingHours?: string
}

// 가게 수정 요청 타입 (백엔드 StoreUpdateRequestDto와 일치)
export interface StoreUpdateRequest {
  name?: string
  zipcode?: string
  address1?: string
  address2?: string
  phoneNumber?: string
  description?: string
  storeImageUrl?: string
  categoryId?: number
  minOrderAmount?: number
  deliveryFee?: number
  operatingHours?: string
}

// 페이징 응답 타입
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 사장님 가게 목록 조회 (페이징 처리)
export const getOwnerStores = async (page: number = 0, size: number = 20): Promise<Store[]> => {
  const response = await apiClient.get<PageResponse<Store>>('/api/owner/stores', {
    params: { page, size }
  })
  return response.data.content // 페이징 응답에서 content 배열만 반환
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

// 가게 삭제
export const deleteStore = async (storeId: number): Promise<void> => {
  await apiClient.delete(`/api/owner/stores/${storeId}`)
}