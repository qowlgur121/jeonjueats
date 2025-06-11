import apiClient from './client'

// 메뉴 타입 정의 (가게 상세에서 사용)
export interface Menu {
  id: number
  menuId: number
  name: string
  description: string
  price: number
  menuImageUrl?: string
  status: 'AVAILABLE' | 'SOLD_OUT'
  createdAt: string
  updatedAt: string
}

// 가게 타입 정의
export interface Store {
  id: number
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
  operatingHours: string
  status: 'OPEN' | 'CLOSED'
  averageRating?: number
  reviewCount?: number
  createdAt: string
  updatedAt: string
}

// 가게 상세 응답 타입 (백엔드 StoreDetailResponseDto와 일치)
export interface StoreDetailResponse {
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
  operatingHours: string
  status: 'OPEN' | 'CLOSED'
  averageRating?: number
  reviewCount?: number
  createdAt: string
  updatedAt: string
  menus: Menu[]
}

// 페이징 응답 타입
export interface StorePageResponse {
  content: Store[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 가게 목록 조회 파라미터
export interface StoreListParams {
  categoryId?: number
  page?: number
  size?: number
  sort?: string
  status?: string
}

// 가게 목록 조회
export const getStores = async (params: StoreListParams = {}): Promise<StorePageResponse> => {
  try {
    const { categoryId, page = 0, size = 20, sort = 'id,desc', status } = params
    
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort
    })
    
    if (categoryId) {
      queryParams.append('categoryId', categoryId.toString())
    }
    
    if (status) {
      queryParams.append('status', status)
    }
    
    const response = await apiClient.get(`/api/stores?${queryParams}`)
    return response.data
  } catch (error) {
    console.error('가게 목록 조회 실패:', error)
    throw error
  }
}

// 가게 상세 조회 (메뉴 포함)
export const getStore = async (storeId: number): Promise<StoreDetailResponse> => {
  try {
    const response = await apiClient.get(`/api/stores/${storeId}`)
    return response.data
  } catch (error) {
    console.error('가게 상세 조회 실패:', error)
    throw error
  }
}

// 가게 검색
export const searchStores = async (keyword: string, page: number = 0, size: number = 20): Promise<StorePageResponse> => {
  try {
    const queryParams = new URLSearchParams({
      keyword,
      page: page.toString(),
      size: size.toString()
    })
    
    const response = await apiClient.get(`/api/search?${queryParams}`)
    return response.data
  } catch (error) {
    console.error('가게 검색 실패:', error)
    throw error
  }
} 