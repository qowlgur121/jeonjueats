import apiClient from './client'

// 회원가입 요청 타입 (사장님 전용)
export interface SignupRequest {
  email: string
  password: string
  nickname: string
  phoneNumber?: string
  zipcode?: string
  address1?: string
  address2?: string
  role: 'ROLE_OWNER' // 사장님 앱은 항상 ROLE_OWNER
}

// 로그인 요청 타입
export interface LoginRequest {
  email: string
  password: string
}

// 로그인 응답 타입
export interface LoginResponse {
  accessToken: string
  userId: number
  email: string
  nickname: string
  role: string
}

// 사용자 프로필 타입
export interface UserProfile {
  id: number
  email: string
  nickname: string
  phoneNumber?: string
  zipcode?: string
  address1?: string
  address2?: string
  role: string
  createdAt: string
  updatedAt: string
}

// 회원가입
export const signup = async (data: SignupRequest): Promise<void> => {
  const response = await apiClient.post('/api/auth/signup', data)
  return response.data
}

// 로그인
export const login = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post('/api/auth/login', data)
  return response.data
}

// 로그아웃
export const logout = async (): Promise<void> => {
  const response = await apiClient.post('/api/auth/logout')
  return response.data
}

// 사용자 프로필 조회
export const getUserProfile = async (): Promise<UserProfile> => {
  const response = await apiClient.get('/api/users/me')
  return response.data
}

// 사용자 프로필 업데이트
export const updateUserProfile = async (data: Partial<UserProfile>): Promise<UserProfile> => {
  const response = await apiClient.put('/api/users/me', data)
  return response.data
}