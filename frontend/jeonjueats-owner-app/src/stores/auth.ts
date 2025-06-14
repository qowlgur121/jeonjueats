import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '../api/client'
import * as authApi from '../api/auth'

interface User {
  id: number
  email: string
  nickname: string
  role: string
  createdAt: string
  updatedAt: string
}

interface LoginRequest {
  email: string
  password: string
}

interface SignupRequest {
  email: string
  password: string
  nickname: string
  phoneNumber?: string
  zipcode?: string
  address1?: string
  address2?: string
  role: 'ROLE_OWNER' // 사장님 앱은 항상 ROLE_OWNER
}

export const useAuthStore = defineStore('auth', () => {
  // 상태 관리
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoading = ref(false)

  // 계산된 속성
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isOwner = computed(() => user.value?.role === 'ROLE_OWNER')

  // 토큰을 localStorage에서 로드
  const loadTokenFromStorage = () => {
    try {
      const storedToken = localStorage.getItem('auth_token')
      const storedUser = localStorage.getItem('auth_user')
      
      if (storedToken && storedUser) {
        token.value = storedToken
        user.value = JSON.parse(storedUser)
        // API 클라이언트에 토큰 설정
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
      }
    } catch (error) {
      console.error('localStorage 데이터 파싱 오류:', error)
      // 잘못된 데이터 제거
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
    }
  }

  // 토큰을 localStorage에 저장
  const saveTokenToStorage = (authToken: string, userData: User) => {
    localStorage.setItem('auth_token', authToken)
    localStorage.setItem('auth_user', JSON.stringify(userData))
    token.value = authToken
    user.value = userData
    // API 클라이언트에 토큰 설정
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${authToken}`
  }

  // 토큰을 localStorage에서 제거
  const removeTokenFromStorage = () => {
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
    token.value = null
    user.value = null
    // API 클라이언트에서 토큰 제거
    delete apiClient.defaults.headers.common['Authorization']
  }

  // 로그인
  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      isLoading.value = true
      
      console.log('API 로그인 요청 시작:', credentials)
      const response = await authApi.login(credentials)
      console.log('API 로그인 응답:', response)
      
      // 사장님 권한 체크
      if (response.role !== 'ROLE_OWNER') {
        throw new Error('사장님 계정만 로그인할 수 있습니다.')
      }
      
      // 백엔드 응답 구조에 맞춰 user 객체 생성
      const userData: User = {
        id: response.userId,
        email: response.email,
        nickname: response.nickname,
        role: response.role,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      
      console.log('토큰 저장 전 상태:', { token: token.value, user: user.value, isAuthenticated: isAuthenticated.value })
      
      saveTokenToStorage(response.accessToken, userData)
      
      console.log('토큰 저장 후 상태:', { token: token.value, user: user.value, isAuthenticated: isAuthenticated.value })
    } catch (error) {
      console.error('로그인 실패:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 회원가입
  const signup = async (userData: SignupRequest): Promise<void> => {
    try {
      isLoading.value = true
      
      // 사장님 역할로 강제 설정
      userData.role = 'ROLE_OWNER'
      
      await authApi.signup(userData)
      
      // 회원가입 후 자동 로그인
      await login({
        email: userData.email,
        password: userData.password
      })
    } catch (error) {
      console.error('회원가입 실패:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 로그아웃
  const logout = async (): Promise<void> => {
    try {
      // 서버에 로그아웃 요청 (토큰이 있는 경우)
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('로그아웃 요청 실패:', error)
      // 서버 요청이 실패해도 로컬 상태는 정리
    } finally {
      removeTokenFromStorage()
    }
  }

  // 사용자 정보 조회
  const fetchUser = async (): Promise<void> => {
    try {
      const userData = await authApi.getUserProfile()
      user.value = userData
      
      // 사용자 정보를 localStorage에도 업데이트
      if (token.value) {
        localStorage.setItem('auth_user', JSON.stringify(userData))
      }
    } catch (error) {
      console.error('사용자 정보 조회 실패:', error)
      // 토큰이 유효하지 않은 경우 로그아웃 처리
      removeTokenFromStorage()
      throw error
    }
  }

  // 토큰 유효성 검사
  const validateToken = async (): Promise<boolean> => {
    if (!token.value) return false
    
    try {
      await fetchUser()
      return true
    } catch (error) {
      removeTokenFromStorage()
      return false
    }
  }

  // 사용자 정보 업데이트 (프로필 수정 후 호출)
  const updateUser = (userData: User): void => {
    user.value = userData
    // localStorage에도 업데이트
    if (token.value) {
      localStorage.setItem('auth_user', JSON.stringify(userData))
    }
  }

  // 초기화 (앱 시작 시 호출)
  const initialize = async (): Promise<void> => {
    loadTokenFromStorage()
    
    if (token.value) {
      await validateToken()
    }
  }

  return {
    // 상태 관리
    user,
    token,
    isLoading,
    // 계산된 속성
    isAuthenticated,
    isOwner,
    // 기능들
    login,
    signup,
    logout,
    fetchUser,
    validateToken,
    updateUser,
    initialize
  }
})