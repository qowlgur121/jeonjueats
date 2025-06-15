import apiClient from './client'

// 사용자 프로필 타입 정의
export interface UserProfile {
  userId: number
  email: string
  nickname: string
  phoneNumber?: string
  role: string
  defaultZipcode?: string
  defaultAddress1?: string
  defaultAddress2?: string
}

// 사용자 정보 수정 요청 타입
export interface UserUpdateRequest {
  nickname?: string
  phoneNumber?: string
  defaultZipcode?: string
  defaultAddress1?: string
  defaultAddress2?: string
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}

// 사용자 정보 조회
export const getUserProfile = async (): Promise<UserProfile> => {
  try {
    console.log('Users API: 사용자 정보 조회 요청')
    const response = await apiClient.get('/api/users/me')
    console.log('Users API: 사용자 정보 조회 성공:', response.data)
    return response.data
  } catch (error) {
    console.error('Users API: 사용자 정보 조회 실패:', error)
    throw error
  }
}

// 사용자 정보 수정
export const updateUserProfile = async (data: UserUpdateRequest): Promise<UserProfile> => {
  try {
    console.log('Users API: 사용자 정보 수정 요청:', data)
    const response = await apiClient.put('/api/users/me', data)
    console.log('Users API: 사용자 정보 수정 성공:', response.data)
    return response.data
  } catch (error) {
    console.error('Users API: 사용자 정보 수정 실패:', error)
    throw error
  }
}