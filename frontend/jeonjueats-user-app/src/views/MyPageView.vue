<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserProfile, updateUserProfile, type UserProfile, type UserUpdateRequest } from '@/api/users'
import { getOrders } from '@/api/orders'

// 라우터와 스토어
const router = useRouter()
const authStore = useAuthStore()

// 상태 관리
const user = ref<UserProfile | null>(null)
const isLoading = ref(false)
const isUpdating = ref(false)

// 주문 통계
const stats = ref({
  totalOrders: 0,
  totalAmount: 0
})

// 프로필 수정 모달
const isEditModalVisible = ref(false)
const editForm = ref<UserUpdateRequest>({
  nickname: '',
  phoneNumber: '',
  defaultZipcode: '',
  defaultAddress1: '',
  defaultAddress2: ''
})

const menuItems = [
  { name: '프로필 수정', icon: '👤', route: '/profile', description: '개인정보 변경' },
  { name: '주문내역', icon: '📋', route: '/orders', description: '주문 및 배송 현황' },
  { name: '즐겨찾기', icon: '❤️', route: '/wishlist', description: '관심 매장 관리' }
]

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await loadUserProfile()
  await loadOrderStats()
})

// 사용자 정보 로드
const loadUserProfile = async () => {
  try {
    isLoading.value = true
    user.value = await getUserProfile()
    console.log('🔍 사용자 정보 구조 확인:', user.value)
    console.log('🔍 사용자명 필드들:', {
      nickname: user.value.nickname,
      email: user.value.email,
      phoneNumber: user.value.phoneNumber,
      defaultZipcode: user.value.defaultZipcode,
      defaultAddress1: user.value.defaultAddress1,
      defaultAddress2: user.value.defaultAddress2
    })
    console.log('🔍 authStore 사용자 정보:', authStore.user)
  } catch (error) {
    console.error('MyPageView: 사용자 정보 로드 실패:', error)
    // 인증 오류인 경우 로그인 페이지로 리다이렉트
    if ((error as any).response?.status === 401) {
      await authStore.logout()
      router.push('/login')
    }
  } finally {
    isLoading.value = false
  }
}

// 주문 통계 로드
const loadOrderStats = async () => {
  try {
    const ordersResponse = await getOrders()
    stats.value.totalOrders = ordersResponse.totalElements
    
    // 총 결제 금액 계산
    const totalAmount = ordersResponse.content.reduce((sum, order) => {
      return sum + order.totalPrice
    }, 0)
    stats.value.totalAmount = totalAmount
    
    console.log('MyPageView: 주문 통계 로드 완료:', stats.value)
  } catch (error) {
    console.error('MyPageView: 주문 통계 로드 실패:', error)
    // 에러가 발생해도 기본값 유지
  }
}

// 프로필 수정 모달 열기
const openEditModal = async () => {
  // 사용자 정보가 로드되지 않았다면 먼저 로드
  if (!user.value) {
    console.log('🔍 사용자 정보가 없어서 먼저 로드합니다.')
    await loadUserProfile()
  }
  
  console.log('🔍 모달 열기 전 user.value:', user.value)
  
  // 모달을 먼저 열고
  isEditModalVisible.value = true
  
  // 다음 틱에서 폼을 채움 (DOM이 렌더링된 후)
  await nextTick()
  fillEditForm()
}

// 편집 폼에 기존 정보 채우기
const fillEditForm = () => {
  if (user.value) {
    editForm.value = {
      nickname: user.value.nickname || '',
      phoneNumber: user.value.phoneNumber || '',
      defaultZipcode: user.value.defaultZipcode || '',
      defaultAddress1: user.value.defaultAddress1 || '',
      defaultAddress2: user.value.defaultAddress2 || ''
    }
    
    console.log('🔍 폼에 설정된 값:', editForm.value)
  } else {
    console.log('🔍 사용자 정보가 없어서 폼을 비웁니다.')
    editForm.value = {
      nickname: '',
      phoneNumber: '',
      defaultZipcode: '',
      defaultAddress1: '',
      defaultAddress2: ''
    }
  }
}

// 프로필 저장
const saveProfile = async () => {
  try {
    isUpdating.value = true
    console.log('MyPageView: 프로필 업데이트 요청:', editForm.value)
    
    const updatedUser = await updateUserProfile(editForm.value)
    console.log('MyPageView: 프로필 업데이트 성공:', updatedUser)
    
    user.value = updatedUser
    isEditModalVisible.value = false
    
    // authStore의 사용자 정보도 업데이트
    if (authStore.user) {
      const updatedAuthUser = {
        ...authStore.user,
        nickname: updatedUser.nickname,
        email: updatedUser.email
      }
      authStore.updateUser(updatedAuthUser)
    }
    
  } catch (error) {
    console.error('MyPageView: 프로필 업데이트 실패:', error)
    alert('프로필 수정에 실패했습니다.')
  } finally {
    isUpdating.value = false
  }
}

// 로그아웃
const logout = async () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    try {
      await authStore.logout()
      router.push('/')
    } catch (error) {
      console.error('MyPageView: 로그아웃 실패:', error)
    }
  }
}


// 주소 포맷팅
const fullAddress = computed(() => {
  if (!user.value) return ''
  const parts = [user.value.defaultAddress1, user.value.defaultAddress2].filter(Boolean)
  return parts.join(' ')
})

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 전화번호 포맷팅 (자동 하이픈 추가)
const formatPhoneNumber = (value: string) => {
  // 숫자만 추출
  const numbers = value.replace(/[^\d]/g, '')
  
  // 길이 제한 (최대 11자리)
  const limitedNumbers = numbers.slice(0, 11)
  
  // 포맷팅
  if (limitedNumbers.length <= 2) {
    return limitedNumbers
  }
  
  // 서울 지역번호 (02)
  if (limitedNumbers.startsWith('02')) {
    if (limitedNumbers.length <= 2) {
      return limitedNumbers
    } else if (limitedNumbers.length <= 5) {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2)}`
    } else if (limitedNumbers.length <= 9) {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2, 5)}-${limitedNumbers.slice(5)}`
    } else {
      return `${limitedNumbers.slice(0, 2)}-${limitedNumbers.slice(2, 6)}-${limitedNumbers.slice(6)}`
    }
  }
  
  // 휴대폰 및 기타 지역번호 (010, 031, 032 등)
  if (limitedNumbers.length <= 3) {
    return limitedNumbers
  } else if (limitedNumbers.length <= 6) {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3)}`
  } else if (limitedNumbers.length <= 10) {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3, 6)}-${limitedNumbers.slice(6)}`
  } else {
    return `${limitedNumbers.slice(0, 3)}-${limitedNumbers.slice(3, 7)}-${limitedNumbers.slice(7)}`
  }
}

// 전화번호 입력 핸들러
const handlePhoneInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const formatted = formatPhoneNumber(target.value)
  editForm.value.phoneNumber = formatted
  
  // 커서 위치 조정 (Vue의 양방향 바인딩 때문에 필요)
  setTimeout(() => {
    target.value = formatted
  }, 0)
}


// 카카오 주소 API
const searchAddressForProfile = () => {
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

      // 프로필 수정 폼에 주소 정보 입력
      editForm.value.defaultZipcode = data.zonecode;
      editForm.value.defaultAddress1 = fullAddress;
      
      // 상세 주소 입력 필드로 포커스 이동
      setTimeout(() => {
        const detailAddressInput = document.querySelector('.modal-content input[placeholder*="상세주소"]') as HTMLInputElement;
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
  <!-- My이츠 페이지 -->
  <div class="mypage">
    
    <!-- 헤더 섹션 -->
    <section class="header-section">
      <div class="section-container">
        <div class="header-content">
          <h1 class="page-title">My이츠</h1>
          <p class="page-subtitle">나의 배달 라이프를 관리해보세요</p>
        </div>
      </div>
    </section>

    <!-- 프로필 컨텐츠 -->
    <div class="profile-content">
      
      <!-- 사용자 프로필 섹션 -->
      <section class="profile-section">
        <div class="section-container">
          <div class="profile-card">
            
            <!-- 프로필 정보 -->
            <div v-if="isLoading" class="loading-container">
              <div class="loading-spinner"></div>
              <p class="loading-text">사용자 정보를 불러오는 중...</p>
            </div>
            
            <div v-else-if="user" class="profile-info">
              <div class="profile-avatar">
                <span class="avatar-emoji">👤</span>
              </div>
              <div class="profile-details">
                <h2 class="user-name">{{ authStore.user?.nickname || user.nickname }}</h2>
                <p class="user-email">{{ user.email }}</p>
                <p class="user-info" v-if="fullAddress">
                  {{ fullAddress }}
                </p>
              </div>
              <button @click="openEditModal" class="edit-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
                </svg>
              </button>
            </div>

            <!-- 통계 정보 -->
            <div v-if="user" class="stats-grid">
              <div class="stat-item">
                <div class="stat-icon">📦</div>
                <div class="stat-details">
                  <div class="stat-value">{{ stats.totalOrders }}회</div>
                  <div class="stat-label">주문</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">💰</div>
                <div class="stat-details">
                  <div class="stat-value">{{ formatPrice(stats.totalAmount) }}</div>
                  <div class="stat-label">총 결제</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 메뉴 섹션 -->
      <section class="menu-section">
        <div class="section-container">
          <div class="menu-card">
            <h3 class="menu-title">서비스 메뉴</h3>
            
            <div class="menu-list">
              <router-link 
                v-for="(item, index) in menuItems" 
                :key="item.name"
                :to="item.route"
                class="menu-item"
              >
                <div class="menu-icon">{{ item.icon }}</div>
                <div class="menu-content">
                  <div class="menu-name">{{ item.name }}</div>
                  <div class="menu-description">{{ item.description }}</div>
                </div>
                <div class="menu-arrow">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- 계정 관리 섹션 -->
      <section class="account-section">
        <div class="section-container">
          <div class="account-card">
            <button @click="logout" class="logout-btn">
              <div class="logout-icon">🚪</div>
              <span>로그아웃</span>
            </button>
            
            <div class="app-info">
              <p class="app-version">JeonjuEats v1.0.0</p>
              <p class="app-copyright">© 2025 JeonjuEats. All rights reserved.</p>
            </div>
          </div>
        </div>
      </section>
      
    </div>

    <!-- 프로필 수정 모달 -->
    <div v-if="isEditModalVisible" class="modal-overlay" @click="isEditModalVisible = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>프로필 수정</h3>
          <button @click="isEditModalVisible = false" class="close-btn">×</button>
        </div>
        
        <form @submit.prevent="saveProfile" class="edit-form">
          <div class="form-group">
            <label>닉네임</label>
            <input 
              :value="editForm.nickname" 
              @input="(e) => editForm.nickname = (e.target as HTMLInputElement).value"
              type="text" 
              placeholder="닉네임을 입력하세요"
              required
            >
          </div>

          <div class="form-group">
            <label>전화번호</label>
            <input 
              :value="editForm.phoneNumber" 
              @input="handlePhoneInput"
              type="tel" 
              placeholder="010-1234-5678"
              maxlength="13"
            >
          </div>
          
          <div class="form-group">
            <label>주소</label>
            <div class="address-input-group">
              <input 
                :value="editForm.defaultAddress1"
                type="text"
                placeholder="주소를 검색해주세요"
                class="form-input"
                readonly
                @click="searchAddressForProfile"
              >
              <button @click="searchAddressForProfile" type="button" class="search-button">
                주소 검색
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>상세주소</label>
            <input 
              :value="editForm.defaultAddress2" 
              @input="(e) => editForm.defaultAddress2 = (e.target as HTMLInputElement).value"
              type="text" 
              placeholder="상세 주소를 입력하세요"
            >
          </div>
          
          <div class="form-actions">
            <button type="button" @click="isEditModalVisible = false" class="cancel-btn">
              취소
            </button>
            <button type="submit" :disabled="isUpdating" class="save-btn">
              {{ isUpdating ? '저장 중...' : '저장' }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* My이츠 페이지 컨테이너 */
.mypage {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 공통 스타일 */
.section-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 4rem;
}

/* 헤더 섹션 */
.header-section {
  width: 100%;
  background-color: white;
  padding: 2.5rem 0;
  border-bottom: 1px solid #f1f3f4;
}

.header-content {
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: #6b7280;
}

/* 프로필 컨텐츠 */
.profile-content {
  padding-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 프로필 섹션 */
.profile-section {
  background-color: white;
}

.profile-card {
  padding: 2rem 0;
}

/* 프로필 정보 */
.profile-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #f3f4f6;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #e5e7eb;
}

.avatar-emoji {
  font-size: 32px;
}

.profile-details {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.user-email {
  font-size: 16px;
  color: #374151;
  margin-bottom: 4px;
}

.user-info {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.join-date {
  font-size: 12px;
  color: #9ca3af;
}

.edit-btn {
  width: 40px;
  height: 40px;
  background-color: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-btn:hover {
  background-color: #f1f3f4;
  border-color: #d1d5db;
}

.edit-btn svg {
  width: 18px;
  height: 18px;
  color: #6b7280;
}

/* 통계 그리드 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background-color: #f1f3f4;
  transform: translateY(-1px);
}

.stat-icon {
  font-size: 20px;
  width: 32px;
  text-align: center;
}

.stat-details {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

/* 메뉴 섹션 */
.menu-section {
  background-color: white;
}

.menu-card {
  padding: 2rem 0;
}

.menu-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.5rem;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #f3f4f6;
  text-decoration: none;
  transition: all 0.2s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background-color: #f8f9fa;
  margin: 0 -1rem;
  padding: 1rem;
  border-radius: 8px;
}

.menu-icon {
  width: 40px;
  height: 40px;
  background-color: #f8f9fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.menu-content {
  flex: 1;
}

.menu-name {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.menu-description {
  font-size: 14px;
  color: #6b7280;
}

.menu-arrow {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-arrow svg {
  width: 16px;
  height: 16px;
  color: #9ca3af;
}

/* 계정 관리 섹션 */
.account-section {
  background-color: white;
}

.account-card {
  padding: 2rem 0;
  text-align: center;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  max-width: 300px;
  margin: 0 auto 2rem;
  padding: 12px 24px;
  background-color: #f8f9fa;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background-color: #f1f3f4;
  border-color: #d1d5db;
}

.logout-icon {
  font-size: 16px;
}

.app-info {
  padding-top: 2rem;
  border-top: 1px solid #f3f4f6;
}

.app-version {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.app-copyright {
  font-size: 12px;
  color: #9ca3af;
}

/* 로딩 상태 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #6b7280;
}

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background-color: white;
  border-radius: 16px;
  padding: 0;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f3f4f6;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: transparent;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 24px;
  color: #6b7280;
}

.close-btn:hover {
  background-color: #f3f4f6;
}

/* 편집 폼 */
.edit-form {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-group input {
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 주소 입력 그룹 스타일 */
.address-input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.search-button {
  flex-shrink: 0;
  padding: 12px 20px;
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

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.cancel-btn, .save-btn {
  flex: 1;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
}

.cancel-btn:hover {
  background-color: #e5e7eb;
}

.save-btn {
  background-color: #3b82f6;
  color: white;
  border: 1px solid #3b82f6;
}

.save-btn:hover:not(:disabled) {
  background-color: #2563eb;
}

.save-btn:disabled {
  background-color: #9ca3af;
  border-color: #9ca3af;
  cursor: not-allowed;
}

/* 반응형 */
@media (max-width: 768px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .profile-info {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .edit-btn {
    position: absolute;
    top: 1rem;
    right: 1rem;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .modal-overlay {
    padding: 0.5rem;
  }
  
  .modal-header {
    padding: 1rem 1.5rem;
  }
  
  .edit-form {
    padding: 1.5rem;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 