<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import apiClient from '../api/client'

interface UserProfile {
  userId: number
  email: string
  nickname: string
  phoneNumber: string | null
  defaultAddress1: string | null
  defaultAddress2: string | null
  defaultZipcode: string | null
  role: string
}

interface ProfileUpdateForm {
  nickname: string
  phoneNumber: string
  defaultAddress1: string
  defaultAddress2: string
  defaultZipcode: string
  currentPassword: string
  newPassword: string
  confirmPassword: string
}


const userProfile = ref<UserProfile | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const profileForm = reactive<ProfileUpdateForm>({
  nickname: '',
  phoneNumber: '',
  defaultAddress1: '',
  defaultAddress2: '',
  defaultZipcode: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showAddressModal = ref(false)
const isEditingProfile = ref(false)



const formatPhoneNumber = (value: string): string => {
  const numbers = value.replace(/[^\d]/g, '')
  
  if (numbers.startsWith('02')) {
    if (numbers.length <= 2) return numbers
    if (numbers.length <= 6) return `${numbers.slice(0, 2)}-${numbers.slice(2)}`
    return `${numbers.slice(0, 2)}-${numbers.slice(2, 6)}-${numbers.slice(6, 10)}`
  } else {
    if (numbers.length <= 3) return numbers
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`
  }
}

const handlePhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const cursorPosition = target.selectionStart
  const oldValue = target.value
  const newValue = formatPhoneNumber(target.value)
  
  profileForm.phoneNumber = newValue
  
  if (oldValue !== newValue) {
    target.value = newValue
    
    const oldLength = oldValue.length
    const newLength = newValue.length
    const diff = newLength - oldLength
    
    if (cursorPosition !== null) {
      const newCursorPosition = cursorPosition + diff
      setTimeout(() => {
        target.setSelectionRange(newCursorPosition, newCursorPosition)
      }, 0)
    }
  }
}

const openAddressModal = () => {
  showAddressModal.value = true
  
  setTimeout(() => {
    const script = document.createElement('script')
    script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'
    script.onload = () => {
      new (window as any).daum.Postcode({
        oncomplete: function(data: any) {
          profileForm.defaultZipcode = data.zonecode
          profileForm.defaultAddress1 = data.address
          showAddressModal.value = false
        }
      }).open()
    }
    document.head.appendChild(script)
  }, 100)
}

const loadUserProfile = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const response = await apiClient.get('/api/users/me')
    userProfile.value = response.data
    
    // 폼에 현재 값 설정
    profileForm.nickname = response.data.nickname
    profileForm.phoneNumber = response.data.phoneNumber || ''
    profileForm.defaultAddress1 = response.data.defaultAddress1 || ''
    profileForm.defaultAddress2 = response.data.defaultAddress2 || ''
    profileForm.defaultZipcode = response.data.defaultZipcode || ''
    
  } catch (error: any) {
    console.error('프로필 로드 에러:', error)
    errorMessage.value = error.response?.data?.message || '프로필을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const updateProfile = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    successMessage.value = ''
    
    if (!profileForm.nickname.trim()) {
      errorMessage.value = '닉네임을 입력해주세요.'
      return
    }
    
    if (!profileForm.phoneNumber.trim()) {
      errorMessage.value = '전화번호를 입력해주세요.'
      return
    }

    // 비밀번호 변경이 요청된 경우 검증
    if (profileForm.newPassword || profileForm.currentPassword || profileForm.confirmPassword) {
      if (!profileForm.currentPassword) {
        errorMessage.value = '현재 비밀번호를 입력해주세요.'
        return
      }
      
      if (!profileForm.newPassword) {
        errorMessage.value = '새 비밀번호를 입력해주세요.'
        return
      }
      
      if (profileForm.newPassword !== profileForm.confirmPassword) {
        errorMessage.value = '새 비밀번호가 일치하지 않습니다.'
        return
      }
      
      if (profileForm.newPassword.length < 8) {
        errorMessage.value = '새 비밀번호는 8자 이상이어야 합니다.'
        return
      }
      
      const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/
      if (!passwordRegex.test(profileForm.newPassword)) {
        errorMessage.value = '새 비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.'
        return
      }
    }
    
    await apiClient.put('/api/users/me', profileForm)
    
    successMessage.value = '프로필이 성공적으로 업데이트되었습니다.'
    isEditingProfile.value = false
    
    // 비밀번호 필드 초기화
    profileForm.currentPassword = ''
    profileForm.newPassword = ''
    profileForm.confirmPassword = ''
    
    // 프로필 다시 로드
    await loadUserProfile()
    
    // 성공 메시지 3초 후 제거
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    
  } catch (error: any) {
    console.error('프로필 업데이트 에러:', error)
    errorMessage.value = error.response?.data?.message || '프로필 업데이트에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}


const cancelEdit = () => {
  isEditingProfile.value = false
  if (userProfile.value) {
    profileForm.nickname = userProfile.value.nickname
    profileForm.phoneNumber = userProfile.value.phoneNumber || ''
    profileForm.defaultAddress1 = userProfile.value.defaultAddress1 || ''
    profileForm.defaultAddress2 = userProfile.value.defaultAddress2 || ''
    profileForm.defaultZipcode = userProfile.value.defaultZipcode || ''
  }
  // 비밀번호 필드 초기화
  profileForm.currentPassword = ''
  profileForm.newPassword = ''
  profileForm.confirmPassword = ''
  errorMessage.value = ''
}

onMounted(() => {
  loadUserProfile()
})
</script>

<template>
  <div class="profile-page">
    <!-- 메인 콘텐츠 -->
    <main class="main-content">
        <div class="page-header">
          <h1 class="page-title">프로필 관리</h1>
          <p class="page-description">사장님 계정 정보를 관리하세요</p>
        </div>

        <!-- 성공/에러 메시지 -->
        <div v-if="successMessage" class="alert alert-success">
          {{ successMessage }}
        </div>
        
        <div v-if="errorMessage" class="alert alert-error">
          {{ errorMessage }}
        </div>

        <!-- 로딩 상태 -->
        <div v-if="isLoading && !userProfile" class="loading-state">
          <div class="loading-spinner"></div>
          <p>프로필 정보를 불러오는 중...</p>
        </div>

        <!-- 프로필 정보 -->
        <div v-else-if="userProfile" class="profile-content">
          <!-- 기본 정보 카드 -->
          <div class="profile-card">
            <div class="card-header">
              <h2>기본 정보</h2>
              <button v-if="!isEditingProfile" @click="isEditingProfile = true" class="btn-edit">
                수정하기
              </button>
              <div v-else class="edit-actions">
                <button @click="updateProfile" :disabled="isLoading" class="btn-save">
                  저장
                </button>
                <button @click="cancelEdit" class="btn-cancel">
                  취소
                </button>
              </div>
            </div>

            <div class="card-content">
              <div class="info-grid">
                <!-- 이메일 (읽기 전용) -->
                <div class="info-item">
                  <label class="info-label">이메일</label>
                  <div class="info-value readonly">{{ userProfile.email }}</div>
                </div>

                <!-- 닉네임 -->
                <div class="info-item">
                  <label class="info-label">닉네임</label>
                  <input 
                    v-if="isEditingProfile"
                    v-model="profileForm.nickname"
                    type="text"
                    class="form-input"
                    placeholder="닉네임을 입력하세요"
                  />
                  <div v-else class="info-value">{{ userProfile.nickname }}</div>
                </div>

                <!-- 전화번호 -->
                <div class="info-item">
                  <label class="info-label">전화번호</label>
                  <input 
                    v-if="isEditingProfile"
                    v-model="profileForm.phoneNumber"
                    @input="handlePhoneInput"
                    type="text"
                    class="form-input"
                    placeholder="전화번호를 입력하세요"
                  />
                  <div v-else class="info-value">{{ userProfile.phoneNumber || '등록된 전화번호가 없습니다' }}</div>
                </div>

                <!-- 주소 -->
                <div class="info-item address-item">
                  <label class="info-label">주소</label>
                  <div v-if="isEditingProfile" class="address-form">
                    <button @click="openAddressModal" type="button" class="btn-address full-width">
                      주소 검색
                    </button>
                    <input 
                      v-if="profileForm.defaultAddress1"
                      v-model="profileForm.defaultAddress1"
                      type="text"
                      readonly
                      class="form-input"
                      placeholder="기본 주소"
                    />
                    <input 
                      v-if="profileForm.defaultAddress1"
                      v-model="profileForm.defaultAddress2"
                      type="text"
                      class="form-input"
                      placeholder="상세 주소를 입력하세요"
                    />
                  </div>
                  <div v-else class="info-value">
                    <div v-if="userProfile.defaultAddress1" class="address-display">
                      <div>({{ userProfile.defaultZipcode }}) {{ userProfile.defaultAddress1 }}</div>
                      <div v-if="userProfile.defaultAddress2">{{ userProfile.defaultAddress2 }}</div>
                    </div>
                    <div v-else class="address-display">
                      등록된 주소가 없습니다
                    </div>
                  </div>
                </div>

                <!-- 비밀번호 -->
                <div class="info-item">
                  <label class="info-label">비밀번호</label>
                  <div v-if="isEditingProfile" class="password-edit-form">
                    <input 
                      v-model="profileForm.currentPassword"
                      type="password"
                      class="form-input"
                      placeholder="현재 비밀번호 (변경 시에만 입력)"
                    />
                    <input 
                      v-model="profileForm.newPassword"
                      type="password"
                      class="form-input"
                      placeholder="새 비밀번호 (변경 시에만 입력)"
                    />
                    <input 
                      v-model="profileForm.confirmPassword"
                      type="password"
                      class="form-input"
                      placeholder="새 비밀번호 확인"
                    />
                    <p class="form-help">비밀번호를 변경하지 않으려면 비워두세요. 변경 시 영문, 숫자, 특수문자 포함 8자 이상</p>
                  </div>
                  <div v-else class="info-value">••••••••</div>
                </div>

                <!-- 역할 (읽기 전용) -->
                <div class="info-item">
                  <label class="info-label">역할</label>
                  <div class="info-value readonly">
                    <span class="role-badge">사장님</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

        </div>
    </main>

  </div>
</template>

<style scoped>
/* 전체 페이지 */
.profile-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 메인 콘텐츠 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
}

.page-header {
  margin-bottom: 1rem;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.page-description {
  color: #6b7280;
  font-size: 0.875rem;
}

/* 알림 메시지 */
.alert {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-weight: 500;
}

.alert-success {
  background-color: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.alert-error {
  background-color: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

/* 로딩 상태 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #6b7280;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 프로필 콘텐츠 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.profile-card {
  background-color: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  background-color: #f8f9fa;
}

.card-header h2 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
}

.btn-edit {
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.btn-save {
  background-color: #10b981;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-save:hover {
  background-color: #059669;
}

.btn-save:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.btn-cancel {
  background-color: #6b7280;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-cancel:hover {
  background-color: #4b5563;
}

.card-content {
  padding: 1.5rem;
}

/* 정보 그리드 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: #374151;
}

.info-value {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 6px;
  font-size: 0.875rem;
  color: #1f2937;
}

.info-value.readonly {
  background-color: #f3f4f6;
  color: #6b7280;
}

.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 주소 관련 */
.address-item {
  grid-column: 1 / -1;
}

.address-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-row {
  display: flex;
  gap: 8px;
}

.zipcode-input {
  flex: 1;
}

.btn-address {
  background-color: #3b82f6;
  color: white;
  border: none;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.btn-address:hover {
  background-color: #2563eb;
}

.btn-address.full-width {
  width: 100%;
}

.address-display {
  line-height: 1.5;
}

.role-badge {
  background-color: #3b82f6;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

/* 비밀번호 편집 폼 */
.password-edit-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 폼 스타일 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-help {
  margin-top: 0.25rem;
  font-size: 12px;
  color: #6b7280;
}

/* 반응형 디자인 */
@media (min-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .address-item {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .header-container {
    padding: 0 1rem;
  }
  
  .nav-container {
    padding: 0 1rem;
  }
  
  .nav-menu {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .nav-item {
    white-space: nowrap;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .edit-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .btn-change-password {
    align-self: flex-end;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .address-row {
    flex-direction: column;
  }
  
  .zipcode-input {
    flex: none;
  }
}
</style>