import axios from 'axios'

// 백엔드 API 기본 설정
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터 - JWT 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token')
    console.log('API Client: JWT 토큰 확인:', token ? '토큰 존재' : '토큰 없음')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('API Client: Authorization 헤더 설정됨')
    }
    console.log('API Client: 요청:', config.method?.toUpperCase(), config.url)
    return config
  },
  (error) => {
    console.error('API 요청 오류:', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터 - 에러 처리 및 이미지 URL 처리
apiClient.interceptors.response.use(
  (response) => {
    // 이미지 URL이 상대 경로인 경우 절대 경로로 변환
    const processImageUrls = (data: any): any => {
      if (!data) return data
      
      if (Array.isArray(data)) {
        return data.map(item => processImageUrls(item))
      }
      
      if (typeof data === 'object') {
        const processed = { ...data }
        
        // 이미지 URL 필드들 처리
        const imageFields = ['storeImageUrl', 'menuImageUrl', 'imageUrl']
        imageFields.forEach(field => {
          if (processed[field] && processed[field].startsWith('/')) {
            // /images/로 시작하면 /api/images/로 변경
            if (processed[field].startsWith('/images/')) {
              processed[field] = processed[field].replace('/images/', '/api/images/')
            }
            processed[field] = `${apiClient.defaults.baseURL}${processed[field]}`
          }
        })
        
        // 중첩된 객체 처리
        Object.keys(processed).forEach(key => {
          if (typeof processed[key] === 'object') {
            processed[key] = processImageUrls(processed[key])
          }
        })
        
        return processed
      }
      
      return data
    }
    
    response.data = processImageUrls(response.data)
    return response
  },
  (error) => {
    // 401 에러 시 토큰 제거 및 로그인 페이지로 리다이렉트
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      
      // 로그인 페이지로 리다이렉트
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
      
      console.error('인증이 만료되었습니다. 로그인 페이지로 이동합니다.')
    }
    
    // 서버 에러 처리
    if (error.response?.status >= 500) {
      console.error('서버 에러가 발생했습니다')
    }
    
    console.error('API 응답 오류:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export default apiClient