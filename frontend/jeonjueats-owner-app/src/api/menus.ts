import apiClient from './client'

// 메뉴 정보 타입
export interface Menu {
  id: number
  name: string
  description: string
  price: number
  status: 'AVAILABLE' | 'SOLD_OUT'
  menuImageUrl?: string
  storeId: number
  storeName: string
  createdAt: string
  updatedAt: string
}

// 메뉴 생성 요청 타입
export interface MenuCreateRequest {
  name: string
  description: string
  price: number
  menuImageUrl?: string
}

// 메뉴 수정 요청 타입
export interface MenuUpdateRequest {
  name?: string
  description?: string
  price?: number
  menuImageUrl?: string
}

// 가게 메뉴 목록 조회
export const getStoreMenus = async (storeId: number): Promise<Menu[]> => {
  const response = await apiClient.get(`/api/owner/stores/${storeId}/menus`)
  return response.data
}

// 메뉴 생성
export const createMenu = async (storeId: number, data: MenuCreateRequest): Promise<Menu> => {
  const response = await apiClient.post(`/api/owner/stores/${storeId}/menus`, data)
  return response.data
}

// 메뉴 수정
export const updateMenu = async (storeId: number, menuId: number, data: MenuUpdateRequest): Promise<Menu> => {
  const response = await apiClient.put(`/api/owner/stores/${storeId}/menus/${menuId}`, data)
  return response.data
}

// 메뉴 삭제
export const deleteMenu = async (storeId: number, menuId: number): Promise<void> => {
  const response = await apiClient.delete(`/api/owner/stores/${storeId}/menus/${menuId}`)
  return response.data
}

// 메뉴 판매 상태 토글
export const toggleMenuAvailability = async (storeId: number, menuId: number): Promise<Menu> => {
  const response = await apiClient.post(`/api/owner/stores/${storeId}/menus/${menuId}/toggle-availability`)
  return response.data
}