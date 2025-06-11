<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const currentStep = ref(1)
const isLoading = ref(false)
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: '',
  passwordConfirm: '',
  nickname: '',
  phoneNumber: '',
  zipcode: '',
  address1: '',
  address2: '',
  agreeTerms: false,
  agreePrivacy: false,
  agreeMarketing: false
})

// 전체 동의 체크 상태
const allAgreed = computed({
  get: () => form.agreeTerms && form.agreePrivacy && form.agreeMarketing,
  set: (value) => {
    form.agreeTerms = value
    form.agreePrivacy = value
    form.agreeMarketing = value
  }
})

// 이메일 유효성 검사
const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(form.email)
})

// 비밀번호 유효성 검사
const isPasswordValid = computed(() => {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/
  return passwordRegex.test(form.password)
})

// 다음 단계로
const nextStep = () => {
  if (currentStep.value === 1) {
    if (!form.email) {
      errorMessage.value = '이메일을 입력해주세요.'
      return
    }
    if (!isEmailValid.value) {
      errorMessage.value = '올바른 이메일 형식이 아닙니다.'
      return
    }
    errorMessage.value = ''
    currentStep.value = 2
  } else if (currentStep.value === 2) {
    if (!form.password) {
      errorMessage.value = '비밀번호를 입력해주세요.'
      return
    }
    if (!isPasswordValid.value) {
      errorMessage.value = '비밀번호는 8자 이상이며 영문자, 숫자, 특수문자를 포함해야 합니다.'
      return
    }
    if (form.password !== form.passwordConfirm) {
      errorMessage.value = '비밀번호가 일치하지 않습니다.'
      return
    }
    if (!form.nickname) {
      errorMessage.value = '닉네임을 입력해주세요.'
      return
    }
    if (!form.phoneNumber) {
      errorMessage.value = '전화번호를 입력해주세요.'
      return
    }
    errorMessage.value = ''
    currentStep.value = 3
  } else if (currentStep.value === 3) {
    // 주소는 선택사항이므로 바로 다음 단계로
    errorMessage.value = ''
    currentStep.value = 4
  }
}

// 회원가입 처리
const handleSignup = async () => {
  if (!form.agreeTerms || !form.agreePrivacy) {
    errorMessage.value = '필수 약관에 동의해주세요.'
    return
  }

  try {
    isLoading.value = true
    errorMessage.value = ''
    
    await authStore.signup({
      email: form.email,
      password: form.password,
      nickname: form.nickname,
      phoneNumber: form.phoneNumber,
      zipcode: form.zipcode,
      address1: form.address1,
      address2: form.address2,
      role: 'ROLE_USER'
    })
    
    // 회원가입 성공 시 로그인 페이지로 이동
    router.push('/login')
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--
    errorMessage.value = ''
  } else {
    router.back()
  }
}

// 카카오 주소 API
const searchAddress = () => {
  new (window as any).daum.Postcode({
    oncomplete: function(data: any) {
      let fullAddress = data.address;
      let extraAddress = '';

      if(data.userSelectedType === 'R'){
        if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
          extraAddress += data.bname;
        }
        if(data.buildingName !== '' && data.apartment === 'Y'){
          extraAddress += (extraAddress !== '' ? ', ' + data.buildingName : data.buildingName);
        }
        if(extraAddress !== ''){
          extraAddress = ' (' + extraAddress + ')';
        }
        fullAddress += extraAddress;
      }

      form.zipcode = data.zonecode;
      form.address1 = fullAddress;
      
      // 상세 주소 입력 필드로 포커스 이동
      setTimeout(() => {
        const detailAddressInput = document.querySelector('input[placeholder*="상세 주소"]') as HTMLInputElement;
        if (detailAddressInput) {
          detailAddressInput.focus();
        }
      }, 100);
    },
    width: '100%',
    height: '100%',
    maxSuggestItems: 10
  }).open();
}

</script>

<template>
  <div class="signup-page">
    <!-- 헤더 -->
    <header class="signup-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
        </svg>
      </button>
      <h1 class="header-title">회원가입</h1>
    </header>

    <!-- 진행 상태 바 -->
    <div class="progress-bar">
      <div class="progress-fill" :style="{ width: `${(currentStep / 4) * 100}%` }"></div>
    </div>

    <div class="signup-container">
      <!-- Step 1: 이메일 입력 -->
      <div v-if="currentStep === 1" class="step-content">
        <h2 class="step-title">이메일을 입력해주세요</h2>
        <p class="step-description">로그인 시 사용할 이메일입니다</p>

        <div class="form-group">
          <input
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            class="form-input"
            :class="{ 'error': errorMessage && !isEmailValid }"
            @keyup.enter="nextStep"
          >
          <p v-if="form.email && !isEmailValid" class="field-error">
            올바른 이메일 형식이 아닙니다
          </p>
        </div>

        <button
          @click="nextStep"
          class="primary-button"
          :disabled="!form.email"
        >
          다음
        </button>
      </div>

      <!-- Step 2: 비밀번호 및 닉네임 -->
      <div v-else-if="currentStep === 2" class="step-content">
        <h2 class="step-title">정보를 입력해주세요</h2>

        <!-- 비밀번호 -->
        <div class="form-group">
          <label class="form-label">비밀번호</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="8자 이상, 영문+숫자+특수문자 포함"
            class="form-input"
            :class="{ 'error': errorMessage && !isPasswordValid }"
          >
          <p v-if="form.password && !isPasswordValid" class="field-error">
            비밀번호는 8자 이상이며 영문자, 숫자, 특수문자를 포함해야 합니다
          </p>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="form-group">
          <label class="form-label">비밀번호 확인</label>
          <input
            v-model="form.passwordConfirm"
            type="password"
            placeholder="비밀번호를 다시 입력해주세요"
            class="form-input"
            :class="{ 'error': form.passwordConfirm && form.password !== form.passwordConfirm }"
          >
          <p v-if="form.passwordConfirm && form.password !== form.passwordConfirm" class="field-error">
            비밀번호가 일치하지 않습니다
          </p>
        </div>

        <!-- 닉네임 -->
        <div class="form-group">
          <label class="form-label">닉네임</label>
          <input
            v-model="form.nickname"
            type="text"
            placeholder="닉네임을 입력해주세요"
            class="form-input"
          >
        </div>

        <!-- 전화번호 -->
        <div class="form-group">
          <label class="form-label">전화번호</label>
          <input
            v-model="form.phoneNumber"
            type="tel"
            placeholder="010-1234-5678"
            class="form-input"
            @keyup.enter="nextStep"
          >
        </div>

        <button
          @click="nextStep"
          class="primary-button"
          :disabled="!form.password || !form.passwordConfirm || !form.nickname || !form.phoneNumber"
        >
          다음
        </button>
      </div>

      <!-- Step 3: 주소 입력 (선택) -->
      <div v-else-if="currentStep === 3" class="step-content">
        <h2 class="step-title">주소를 입력해주세요</h2>
        <p class="step-description">배달 주문 시 사용할 기본 주소입니다 (선택사항)</p>

        <!-- 우편번호 -->
        <div class="form-group">
          <label class="form-label">우편번호</label>
          <input
            v-model="form.zipcode"
            type="text"
            placeholder="우편번호"
            class="form-input"
            readonly
          >
        </div>

        <!-- 주소 -->
        <div class="form-group">
          <label class="form-label">주소</label>
          <div class="address-input-group">
            <input
              v-model="form.address1"
              type="text"
              placeholder="주소를 검색해주세요"
              class="form-input"
              readonly
              @click="searchAddress"
            >
            <button @click="searchAddress" type="button" class="search-button">
              주소 검색
            </button>
          </div>
        </div>

        <!-- 상세주소 -->
        <div class="form-group">
          <label class="form-label">상세주소</label>
          <input
            v-model="form.address2"
            type="text"
            placeholder="상세 주소를 입력해주세요 (선택)"
            class="form-input"
            @keyup.enter="nextStep"
          >
        </div>

        <button
          @click="nextStep"
          class="primary-button"
        >
          다음
        </button>
      </div>

      <!-- Step 4: 약관 동의 -->
      <div v-else-if="currentStep === 4" class="step-content">
        <h2 class="step-title">약관에 동의해주세요</h2>

        <!-- 전체 동의 -->
        <div class="agree-all">
          <label class="checkbox-container">
            <input
              v-model="allAgreed"
              type="checkbox"
              class="checkbox"
            >
            <span class="checkbox-label">전체 동의</span>
          </label>
        </div>

        <div class="divider"></div>

        <!-- 개별 약관 -->
        <div class="agree-list">
          <label class="checkbox-container">
            <input
              v-model="form.agreeTerms"
              type="checkbox"
              class="checkbox"
            >
            <span class="checkbox-label">
              <span class="required">[필수]</span> 이용약관 동의
            </span>
            <a href="/terms-of-service" target="_blank" class="link-detail">보기</a>
          </label>

          <label class="checkbox-container">
            <input
              v-model="form.agreePrivacy"
              type="checkbox"
              class="checkbox"
            >
            <span class="checkbox-label">
              <span class="required">[필수]</span> 개인정보 수집 및 이용 동의
            </span>
            <a href="/privacy-policy" target="_blank" class="link-detail">보기</a>
          </label>

          <label class="checkbox-container">
            <input
              v-model="form.agreeMarketing"
              type="checkbox"
              class="checkbox"
            >
            <span class="checkbox-label">
              <span class="optional">[선택]</span> 마케팅 정보 수신 동의
            </span>
            <span class="link-detail" style="color: #9ca3af; cursor: default;">-</span>
          </label>
        </div>

        <button
          @click="handleSignup"
          class="primary-button"
          :disabled="!form.agreeTerms || !form.agreePrivacy || isLoading"
        >
          {{ isLoading ? '가입 중...' : '가입완료' }}
        </button>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.signup-page {
  min-height: 100vh;
  background-color: white;
  display: flex;
  flex-direction: column;
}

/* 헤더 */
.signup-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background-color: white;
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

/* 진행 상태 바 */
.progress-bar {
  height: 3px;
  background-color: #f3f4f6;
  position: relative;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #3b82f6;
  transition: width 0.3s ease;
}

/* 컨테이너 */
.signup-container {
  flex: 1;
  padding: 32px 20px;
  max-width: 440px;
  width: 100%;
  margin: 0 auto;
}

.step-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.step-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 32px;
}


/* 폼 그룹 */
.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  font-size: 14px;
  color: #374151;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 16px;
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

.field-error {
  font-size: 13px;
  color: #ef4444;
  margin-top: 4px;
}

/* 주소 입력 그룹 스타일 */
.address-input-group {
  display: flex;
  gap: 8px;
}

.search-button {
  flex-shrink: 0;
  padding: 16px 20px;
  background-color: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-button:hover {
  background-color: #e5e7eb;
}

/* 약관 동의 */
.agree-all {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.divider {
  height: 1px;
  background-color: #e5e7eb;
  margin: 16px 0;
}

.agree-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
}

.checkbox {
  width: 20px;
  height: 20px;
  accent-color: #3b82f6;
  cursor: pointer;
}

.checkbox-label {
  flex: 1;
  font-size: 14px;
  color: #374151;
  user-select: none;
}

.required {
  color: #ef4444;
  font-weight: 500;
}

.optional {
  color: #6b7280;
  font-weight: 500;
}

.link-detail {
  font-size: 13px;
  color: #6b7280;
  text-decoration: underline;
}

/* 버튼 */
.primary-button {
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
  margin-top: 32px;
}

.primary-button:hover:not(:disabled) {
  background-color: #2563eb;
}

.primary-button:disabled {
  background-color: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 에러 메시지 */
.error-message {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  margin-top: 16px;
  text-align: center;
}

/* 반응형 */
@media (max-width: 480px) {
  .step-title {
    font-size: 20px;
  }
  
  .form-input {
    padding: 14px;
    font-size: 15px;
  }
}
</style>