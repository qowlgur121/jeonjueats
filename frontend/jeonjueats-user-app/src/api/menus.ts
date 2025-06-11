import apiClient from './client'

// 메뉴 타입 정의
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

// 메뉴 목록 응답 타입
export interface MenuPageResponse {
  content: Menu[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 특정 가게의 메뉴 목록 조회
export const getMenusByStore = async (storeId: number): Promise<Menu[]> => {
  try {
    const response = await apiClient.get(`/api/stores/${storeId}`)
    // 가게 상세 API가 메뉴도 함께 반환한다고 가정
    return response.data.menus || []
  } catch (error) {
    console.error('메뉴 목록 조회 실패:', error)
    throw error
  }
}

// 메뉴 상세 조회
export const getMenu = async (menuId: number): Promise<Menu> => {
  try {
    const response = await apiClient.get(`/api/menus/${menuId}`)
    return response.data
  } catch (error) {
    console.error('메뉴 상세 조회 실패:', error)
    throw error
  }
}