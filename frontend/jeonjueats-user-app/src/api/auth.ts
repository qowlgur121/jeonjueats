import apiClient from './client'

export interface LoginRequest {
  email: string
  password: string
}

export interface SignupRequest {
  email: string
  password: string
  nickname: string
}

export interface User {
  id: number
  email: string
  nickname: string
  role: string
  createdAt: string
  updatedAt: string
}

export interface LoginResponse {
  message: string
  accessToken: string
  userId: number
  email: string
  nickname: string
  role: string
}

export interface SignupResponse {
  message: string
  user: User
}

// 로그인
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  try {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', credentials)
    return response.data
  } catch (error) {
    console.error('로그인 API 오류:', error)
    throw error
  }
}

// 회원가입
export const signup = async (userData: SignupRequest): Promise<SignupResponse> => {
  try {
    const response = await apiClient.post<SignupResponse>('/api/auth/signup', userData)
    return response.data
  } catch (error) {
    console.error('회원가입 API 오류:', error)
    throw error
  }
}

// 로그아웃
export const logout = async (): Promise<void> => {
  try {
    await apiClient.post('/api/auth/logout')
  } catch (error) {
    console.error('로그아웃 API 오류:', error)
    throw error
  }
}

// 사용자 정보 조회
export const getUserProfile = async (): Promise<User> => {
  try {
    const response = await apiClient.get<User>('/api/users/me')
    return response.data
  } catch (error) {
    console.error('사용자 정보 조회 API 오류:', error)
    throw error
  }
}

// 사용자 정보 수정
export const updateUserProfile = async (userData: Partial<User>): Promise<User> => {
  try {
    const response = await apiClient.put<User>('/api/users/me', userData)
    return response.data
  } catch (error) {
    console.error('사용자 정보 수정 API 오류:', error)
    throw error
  }
}