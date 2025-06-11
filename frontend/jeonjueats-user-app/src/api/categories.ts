import apiClient from './client'

// 카테고리 타입 정의
export interface Category {
  id: number
  name: string
  description?: string
}

// 카테고리 목록 조회
export const getCategories = async (): Promise<Category[]> => {
  try {
    const response = await apiClient.get('/api/categories')
    return response.data
  } catch (error) {
    console.error('카테고리 목록 조회 실패:', error)
    throw error
  }
} 