import apiClient from './client'

// 이미지 업로드 응답 타입
export interface ImageUploadResponse {
  imageUrl: string
}

// 이미지 업로드
export const uploadImage = async (file: File): Promise<ImageUploadResponse> => {
  const formData = new FormData()
  formData.append('image', file)
  
  const response = await apiClient.post('/api/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}