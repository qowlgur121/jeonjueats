<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
  rememberMe: false
})

const showPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  console.log('로그인 함수 호출됨', { email: form.email, password: form.password })
  
  // 입력값 검증
  if (!form.email) {
    errorMessage.value = '아이디(이메일)를 입력해주세요.'
    return
  }
  
  if (!form.password) {
    errorMessage.value = '비밀번호를 입력해주세요.'
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''
    
    console.log('authStore.login 호출 시작')
    await authStore.login({
      email: form.email,
      password: form.password
    })
    
    console.log('로그인 성공!')
    // 로그인 성공 시 대시보드로 이동
    const redirectTo = router.currentRoute.value.query.redirect as string
    router.push(redirectTo || '/dashboard')
  } catch (error: any) {
    console.error('로그인 에러:', error)
    if (error.message === '사장님 계정만 로그인할 수 있습니다.') {
      errorMessage.value = '사장님 계정만 로그인할 수 있습니다.'
    } else {
      errorMessage.value = error.response?.data?.message || '로그인에 실패했습니다.'
    }
  } finally {
    isLoading.value = false
  }
}

const goToSignup = () => {
  router.push('/signup')
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="login-page">
    <header class="login-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
        </svg>
      </button>
      <h1 class="header-title">사장님 로그인</h1>
    </header>

    <div class="logo-section">

    </div>

    <form @submit.prevent="handleLogin" class="login-form">
      <div class="form-group">
        <label for="email" class="form-label">아이디</label>
        <div class="input-wrapper">
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            class="form-input"
            :class="{ 'error': errorMessage && !form.email }"
          >
          <button
            v-if="form.email"
            type="button"
            @click="form.email = ''"
            class="clear-button"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="form-group">
        <label for="password" class="form-label">비밀번호</label>
        <div class="input-wrapper">
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="비밀번호를 입력하세요"
            class="form-input"
            :class="{ 'error': errorMessage && !form.password }"
          >
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="toggle-password"
          >
            <svg v-if="showPassword" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/>
            </svg>
          </button>
        </div>
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <div class="checkbox-group">
        <input
          id="remember"
          v-model="form.rememberMe"
          type="checkbox"
          class="checkbox"
        >
        <label for="remember" class="checkbox-label">자동로그인</label>
      </div>

      <button
        type="submit"
        class="login-button"
        :disabled="isLoading"
      >
        {{ isLoading ? '로그인 중...' : '로그인' }}
      </button>

      <button
        type="button"
        @click="goToSignup"
        class="signup-button"
      >
        회원가입
      </button>
    </form>
  </div>
</template>

<style scoped>
.login-page {
  width: 100%;
  min-height: 100vh;
  background-color: white;
  display: flex;
  flex-direction: column;
}

.login-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  border-bottom: 1px solid #f1f3f4;
}

.back-button {
  position: absolute;
  left: 16px;
  background: none;
  border: none;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.back-button:hover {
  background-color: #f8f9fa;
}

.back-button svg {
  width: 24px;
  height: 24px;
  color: #374151;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.logo-section {
  padding: 40px 20px 20px;
  text-align: center;
}

.login-form {
  flex: 1;
  padding: 0 20px 40px;
  max-width: 440px;
  width: 100%;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 16px;
  padding-right: 48px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.2s;
  background-color: white;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #ef4444;
}

.form-input::placeholder {
  color: #9ca3af;
}

.clear-button,
.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.clear-button:hover,
.toggle-password:hover {
  background-color: #f3f4f6;
}

.clear-button svg,
.toggle-password svg {
  width: 20px;
  height: 20px;
  color: #6b7280;
}

.error-message {
  color: #ef4444;
  font-size: 14px;
  margin-top: -12px;
  margin-bottom: 20px;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.checkbox {
  width: 20px;
  height: 20px;
  accent-color: #3b82f6;
  cursor: pointer;
}

.checkbox-label {
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  user-select: none;
}

.login-button {
  width: 100%;
  padding: 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 12px;
}

.login-button:hover:not(:disabled) {
  background-color: #2563eb;
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.signup-button {
  width: 100%;
  padding: 16px;
  background-color: white;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.signup-button:hover {
  background-color: #f9fafb;
  border-color: #d1d5db;
}

@media (max-width: 480px) {
  .logo {
    font-size: 32px;
  }
  
  .form-input {
    padding: 14px;
    font-size: 15px;
  }
}
</style>