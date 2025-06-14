import apiClient from './client'

// 카테고리 타입
export interface Category {
  id: number
  name: string
}

// 카테고리 목록 조회 (가게 등록/수정시 필요)
export const getCategories = async (): Promise<Category[]> => {
  const response = await apiClient.get('/api/categories')
  return response.data
}