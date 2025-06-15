<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import apiClient from '../api/client'
import * as storesApi from '../api/stores'
import * as menusApi from '../api/menus'
import type { Store } from '../api/stores'
import type { Menu, MenuCreateRequest, MenuUpdateRequest } from '../api/menus'


// 상태 관리
const stores = ref<Store[]>([])
const selectedStoreId = ref<number | null>(null)
const menus = ref<Menu[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingMenu = ref<Menu | null>(null)

// 폼 데이터
const form = ref({
  name: '',
  description: '',
  price: 0,
  menuImageUrl: ''
})

// 이미지 업로드 관련 상태
const selectedImageFile = ref<File | null>(null)
const imagePreviewUrl = ref('')
const isImageUploading = ref(false)
const menuImageInput = ref<HTMLInputElement | null>(null)
const menuImageInputEdit = ref<HTMLInputElement | null>(null)

// 이미지 파일 선택 핸들러
const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (file) {
    // 파일 크기 체크 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('파일 크기가 5MB를 초과합니다.')
      return
    }
    
    // 파일 형식 체크
    if (!file.type.match(/^image\/(jpeg|jpg|png)$/)) {
      alert('JPEG 또는 PNG 파일만 업로드 가능합니다.')
      return
    }
    
    selectedImageFile.value = file
    
    // 미리보기 생성
    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreviewUrl.value = e.target?.result as string
      // 폼의 menuImageUrl도 미리보기 URL로 업데이트 (미리보기 표시용)
      form.value.menuImageUrl = imagePreviewUrl.value
    }
    reader.readAsDataURL(file)
  }
}

// 이미지 업로드 함수
const uploadImage = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('domain', 'menus')
  
  try {
    const response = await apiClient.post('/api/upload/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    return response.data.imageUrl
  } catch (error: any) {
    console.error('이미지 업로드 실패:', error)
    
    if (error.response?.status === 401) {
      window.location.href = '/login'
      throw new Error('로그인이 만료되었습니다. 다시 로그인해주세요.')
    } else if (error.response?.status === 403) {
      throw new Error('이미지 업로드 권한이 없습니다.')
    } else if (error.response?.data?.message) {
      throw new Error(error.response.data.message)
    } else {
      throw new Error('이미지 업로드에 실패했습니다.')
    }
  }
}

// 초기 데이터 로드
const loadStores = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const data = await storesApi.getOwnerStores()
    stores.value = data
    
    // 첫 번째 가게 자동 선택
    if (data.length > 0) {
      selectedStoreId.value = data[0].storeId
    }
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 메뉴 목록 로드
const loadMenus = async () => {
  if (!selectedStoreId.value) return
  
  console.log('메뉴 로드 시작 - 가게 ID:', selectedStoreId.value)
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const data = await menusApi.getStoreMenus(selectedStoreId.value)
    console.log('메뉴 데이터 로드 완료:', data.length, '개')
    menus.value = data
  } catch (error: any) {
    console.error('메뉴 목록 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '메뉴 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 가게 변경 시 메뉴 목록 리로드
watch(selectedStoreId, (newStoreId, oldStoreId) => {
  console.log('가게 선택 변경:', oldStoreId, '->', newStoreId)
  if (newStoreId) {
    loadMenus()
  }
})

// 메뉴 생성
const createMenu = async () => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    // 이미지 업로드 처리
    let menuImageUrl = form.value.menuImageUrl
    if (selectedImageFile.value) {
      isImageUploading.value = true
      try {
        menuImageUrl = await uploadImage(selectedImageFile.value)
      } catch (error) {
        console.error('이미지 업로드 실패:', error)
        throw new Error('이미지 업로드에 실패했습니다.')
      } finally {
        isImageUploading.value = false
      }
    }
    
    const menuData: MenuCreateRequest = {
      menuName: form.value.name,
      description: form.value.description,
      price: form.value.price,
      menuImageUrl: menuImageUrl || undefined
    }
    
    await menusApi.createMenu(selectedStoreId.value, menuData)
    
    // 성공 후 폼 초기화 및 모달 닫기
    resetForm()
    showCreateModal.value = false
    
    // 메뉴 목록 새로고침
    await loadMenus()
  } catch (error: any) {
    console.error('메뉴 등록 실패:', error)
    errorMessage.value = error.response?.data?.message || '메뉴 등록에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 메뉴 수정
const updateMenu = async () => {
  if (!selectedStoreId.value || !editingMenu.value) return
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    // 이미지 업로드 처리
    let menuImageUrl = form.value.menuImageUrl
    if (selectedImageFile.value) {
      isImageUploading.value = true
      try {
        menuImageUrl = await uploadImage(selectedImageFile.value)
      } catch (error) {
        console.error('이미지 업로드 실패:', error)
        throw new Error('이미지 업로드에 실패했습니다.')
      } finally {
        isImageUploading.value = false
      }
    }
    
    const menuData: MenuUpdateRequest = {
      menuName: form.value.name,
      description: form.value.description,
      price: form.value.price,
      menuImageUrl: menuImageUrl || undefined
    }
    
    await menusApi.updateMenu(selectedStoreId.value, editingMenu.value.id, menuData)
    
    // 성공 후 폼 초기화 및 모달 닫기
    resetForm()
    showEditModal.value = false
    editingMenu.value = null
    
    // 메뉴 목록 새로고침
    await loadMenus()
  } catch (error: any) {
    console.error('메뉴 수정 실패:', error)
    errorMessage.value = error.response?.data?.message || '메뉴 수정에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 메뉴 삭제
const deleteMenu = async (menu: Menu) => {
  if (!selectedStoreId.value) return
  
  if (!confirm(`"${menu.name}" 메뉴를 삭제하시겠습니까?`)) {
    return
  }
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    await menusApi.deleteMenu(selectedStoreId.value, menu.id)
    
    // 메뉴 목록 새로고침
    await loadMenus()
  } catch (error: any) {
    console.error('메뉴 삭제 실패:', error)
    errorMessage.value = error.response?.data?.message || '메뉴 삭제에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 메뉴 판매 상태 토글
const toggleMenuStatus = async (menu: Menu) => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    await menusApi.toggleMenuAvailability(selectedStoreId.value, menu.id)
    
    // 메뉴 목록 새로고침
    await loadMenus()
  } catch (error: any) {
    console.error('메뉴 상태 변경 실패:', error)
    errorMessage.value = error.response?.data?.message || '메뉴 상태 변경에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 폼 초기화
const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    price: 0,
    menuImageUrl: ''
  }
  // 이미지 파일 초기화
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
}

// 수정 모달 열기
const openEditModal = (menu: Menu) => {
  editingMenu.value = menu
  form.value = {
    name: menu.name,
    description: menu.description,
    price: menu.price,
    menuImageUrl: menu.menuImageUrl || ''
  }
  // 기존 이미지가 있으면 미리보기 설정
  if (menu.menuImageUrl) {
    imagePreviewUrl.value = menu.menuImageUrl
  }
  selectedImageFile.value = null
  showEditModal.value = true
}

// 생성 모달 열기
const openCreateModal = () => {
  resetForm()
  showCreateModal.value = true
}

// 유틸리티 함수
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    minimumFractionDigits: 0
  }).format(amount)
}

const getStatusColor = (status: string) => {
  return status === 'AVAILABLE' ? 'text-green-600' : 'text-red-600'
}

const getStatusText = (status: string) => {
  return status === 'AVAILABLE' ? '판매중' : '품절'
}


// 페이지 로드 시 초기화
onMounted(async () => {
  await loadStores()
})
</script>

<template>
  <div class="menus-page">
  <!-- 메인 컨텐츠 -->
  <main class="main-container">
    <!-- 에러 메시지 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <span>메뉴 데이터를 불러오는 중...</span>
    </div>

    <!-- 메뉴 관리 헤더 -->
    <div class="page-header">
      <div class="page-title">
        <h1>메뉴 관리</h1>
        <p class="page-subtitle">가게의 메뉴를 등록하고 관리하세요</p>
      </div>
      <button 
        class="btn-primary" 
        @click="openCreateModal"
        :disabled="!selectedStoreId"
      >
        + 새 메뉴 등록
      </button>
    </div>

    <!-- 가게 선택 -->
    <div class="store-selector">
      <label for="store-select" class="selector-label">가게 선택:</label>
      <select 
        id="store-select"
        v-model="selectedStoreId" 
        class="store-select"
      >
        <option value="" disabled>가게를 선택하세요</option>
        <option 
          v-for="store in stores" 
          :key="store.storeId" 
          :value="store.storeId"
        >
          {{ store.name }}
        </option>
      </select>
    </div>

    <!-- 메뉴 목록 -->
    <div v-if="selectedStoreId" class="menus-grid">
      <div v-if="menus.length === 0 && !isLoading" class="empty-state">
        <div class="empty-icon">🍽️</div>
        <h3>등록된 메뉴가 없습니다</h3>
        <p>첫 번째 메뉴를 등록해보세요!</p>
        <button class="btn-primary" @click="openCreateModal">
          메뉴 등록하기
        </button>
      </div>

      <div v-else class="menu-cards">
        <div 
          v-for="menu in menus" 
          :key="menu.id" 
          class="menu-card"
        >
          <!-- 메뉴 이미지 -->
          <div class="menu-image">
            <img 
              v-if="menu.menuImageUrl" 
              :src="menu.menuImageUrl"
              :alt="menu.name"
              class="menu-img"
            />
            <div v-else class="menu-img-placeholder">
              🍽️
            </div>
          </div>

          <!-- 메뉴 정보 -->
          <div class="menu-info">
            <div class="menu-header">
              <h3 class="menu-name">{{ menu.name }}</h3>
              <span 
                class="menu-status"
                :class="getStatusColor(menu.status)"
              >
                {{ getStatusText(menu.status) }}
              </span>
            </div>
            
            <p class="menu-description">{{ menu.description }}</p>
            <div class="menu-price">{{ formatCurrency(menu.price) }}</div>
            
            <!-- 액션 버튼들 -->
            <div class="menu-actions">
              <button 
                class="btn-secondary"
                @click="openEditModal(menu)"
              >
                수정
              </button>
              <button 
                class="btn-toggle"
                :class="menu.status === 'AVAILABLE' ? 'btn-sold-out' : 'btn-available'"
                @click="toggleMenuStatus(menu)"
              >
                {{ menu.status === 'AVAILABLE' ? '품절처리' : '판매재개' }}
              </button>
              <button 
                class="btn-danger"
                @click="deleteMenu(menu)"
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>

  <!-- 메뉴 생성 모달 -->
  <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h2>새 메뉴 등록</h2>
        <button class="modal-close" @click="showCreateModal = false">×</button>
      </div>
      
      <form @submit.prevent="createMenu" class="modal-form">
        <div class="form-group">
          <label for="menu-name">메뉴명 *</label>
          <input 
            id="menu-name"
            v-model="form.name" 
            type="text" 
            required 
            maxlength="100"
            placeholder="메뉴명을 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label for="menu-description">메뉴 설명</label>
          <textarea 
            id="menu-description"
            v-model="form.description" 
            maxlength="500"
            rows="3"
            placeholder="메뉴에 대한 설명을 입력하세요"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label for="menu-price">가격 *</label>
          <input 
            id="menu-price"
            v-model.number="form.price" 
            type="number" 
            required 
            min="0"
            step="100"
            placeholder="가격을 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label for="menu-image">메뉴 이미지</label>
          <button 
            type="button"
            class="image-upload-btn"
            @click="menuImageInput?.click()"
            :disabled="isImageUploading"
          >
            {{ isImageUploading ? '업로드 중...' : '이미지 선택' }}
          </button>
          <input 
            ref="menuImageInput"
            type="file" 
            accept="image/jpeg,image/jpg,image/png"
            @change="handleImageSelect"
            style="display: none"
          />
          <div v-if="form.menuImageUrl" class="image-preview">
            <img :src="form.menuImageUrl" alt="메뉴 이미지 미리보기" />
          </div>
        </div>
        
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="showCreateModal = false">
            취소
          </button>
          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? '등록 중...' : '메뉴 등록' }}
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- 메뉴 수정 모달 -->
  <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
    <div class="modal" @click.stop>
      <div class="modal-header">
        <h2>메뉴 수정</h2>
        <button class="modal-close" @click="showEditModal = false">×</button>
      </div>
      
      <form @submit.prevent="updateMenu" class="modal-form">
        <div class="form-group">
          <label for="edit-menu-name">메뉴명 *</label>
          <input 
            id="edit-menu-name"
            v-model="form.name" 
            type="text" 
            required 
            maxlength="100"
            placeholder="메뉴명을 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label for="edit-menu-description">메뉴 설명</label>
          <textarea 
            id="edit-menu-description"
            v-model="form.description" 
            maxlength="500"
            rows="3"
            placeholder="메뉴에 대한 설명을 입력하세요"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label for="edit-menu-price">가격 *</label>
          <input 
            id="edit-menu-price"
            v-model.number="form.price" 
            type="number" 
            required 
            min="0"
            step="100"
            placeholder="가격을 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label for="edit-menu-image">메뉴 이미지</label>
          <button 
            type="button"
            class="image-upload-btn"
            @click="menuImageInputEdit?.click()"
            :disabled="isImageUploading"
          >
            {{ isImageUploading ? '업로드 중...' : '이미지 선택' }}
          </button>
          <input 
            ref="menuImageInputEdit"
            type="file" 
            accept="image/jpeg,image/jpg,image/png"
            @change="handleImageSelect"
            style="display: none"
          />
          <div v-if="form.menuImageUrl" class="image-preview">
            <img :src="form.menuImageUrl.startsWith('http') ? form.menuImageUrl : `http://localhost:8080${form.menuImageUrl}`" alt="메뉴 이미지 미리보기" />
          </div>
        </div>
        
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="showEditModal = false">
            취소
          </button>
          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? '수정 중...' : '메뉴 수정' }}
          </button>
        </div>
      </form>
    </div>
  </div>
  </div>
</template>

<style scoped>
/* 전체 페이지 */
.menus-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 메인 컨테이너 */
.main-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1rem 2rem;
}

/* 에러 메시지 */
.error-message {
  background: #fee2e2;
  color: #dc2626;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border: 1px solid #fecaca;
}

/* 로딩 */
.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 2rem;
  color: #6b7280;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f4f6;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 페이지 헤더 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.page-title h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: #6b7280;
  margin: 0;
}

/* 가게 선택 */
.store-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.selector-label {
  font-weight: 600;
  color: #374151;
  white-space: nowrap;
}

.store-select {
  flex: 1;
  max-width: 300px;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  background: white;
}

.store-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #6b7280;
  margin-bottom: 1rem;
}

/* 메뉴 카드 그리드 */
.menu-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.menu-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.menu-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 메뉴 이미지 */
.menu-image {
  height: 200px;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.menu-img-placeholder {
  font-size: 3rem;
  color: #d1d5db;
}

/* 메뉴 정보 */
.menu-info {
  padding: 1.5rem;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.menu-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
  flex: 1;
}

.menu-status {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 4px;
  background: currentColor;
  color: white;
  opacity: 0.9;
}

.menu-description {
  color: #6b7280;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.menu-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1.5rem;
}

/* 액션 버튼들 */
.menu-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* 버튼 스타일 */
.btn-primary {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6b7280;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.btn-secondary:hover {
  background: #4b5563;
}

.btn-danger {
  background: #ef4444;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-toggle {
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
}

.btn-sold-out {
  background: #fbbf24;
  color: white;
}

.btn-sold-out:hover {
  background: #f59e0b;
}

.btn-available {
  background: #10b981;
  color: white;
}

.btn-available:hover {
  background: #059669;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.modal-close:hover {
  background: #f3f4f6;
}

.modal-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

/* 이미지 업로드 스타일 */
.image-upload-btn {
  width: 100%;
  padding: 12px 16px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.image-upload-btn:hover:not(:disabled) {
  background-color: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.image-upload-btn:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.image-preview {
  margin-top: 12px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.image-preview img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .main-container {
    padding: 1rem;
  }
  
  .page-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  .menu-cards {
    grid-template-columns: 1fr;
  }
  
  .store-selector {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .store-select {
    max-width: none;
    width: 100%;
  }
  
  .menu-actions {
    flex-direction: column;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>