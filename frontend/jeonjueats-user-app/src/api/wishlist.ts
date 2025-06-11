import apiClient from './client'

// 찜 응답 타입 (백엔드와 일치)
export interface WishlistResponseDto {
  wishlistId: number
  wishedAt: string
  store: {
    storeId: number
    storeName: string
    storeImageUrl?: string
    minOrderAmount: number
    deliveryTip: number
    storeStatus: string
    description: string
  }
}

// 찜 토글 응답 타입 (백엔드 실제 응답과 일치)
export interface WishlistToggleResponseDto {
  storeId: number
  wished: boolean
  message: string
}

// 찜 목록 페이징 응답 타입
export interface WishlistPageResponse {
  content: WishlistResponseDto[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 찜 상태 토글 (찜하기/해제)
export const toggleWishlist = async (storeId: number): Promise<WishlistToggleResponseDto> => {
  try {
    const response = await apiClient.post(`/api/wishes/stores/${storeId}/toggle`)
    return response.data
  } catch (error) {
    console.error('찜 토글 실패:', error)
    throw error
  }
}

// 찜 목록 조회
export const getWishlists = async (page: number = 0, size: number = 10): Promise<WishlistPageResponse> => {
  try {
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })
    
    const response = await apiClient.get(`/api/wishes/stores?${queryParams}`)
    return response.data
  } catch (error) {
    console.error('찜 목록 조회 실패:', error)
    throw error
  }
}

// 특정 가게의 찜 여부 확인
export const getWishlistStatus = async (storeId: number): Promise<boolean> => {
  try {
    const response = await apiClient.get(`/api/wishes/stores/${storeId}/status`)
    return response.data
  } catch (error) {
    console.error('찜 상태 확인 실패:', error)
    throw error
  }
}