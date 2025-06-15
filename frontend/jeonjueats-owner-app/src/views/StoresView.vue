<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as storesApi from '../api/stores'
import type { Store, StoreCreateRequest, StoreUpdateRequest } from '../api/stores'
import WeeklyOperatingHours from '../components/WeeklyOperatingHours.vue'
import apiClient from '../api/client'

interface Category {
  id: number
  name: string
}

const router = useRouter()

const stores = ref<Store[]>([])
const categories = ref<Category[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingStore = ref<Store | null>(null)

const form = ref({
  name: '',
  description: '',
  phoneNumber: '',
  zipcode: '',
  address1: '',
  address2: '',
  categoryId: '',
  minOrderAmount: 0,
  deliveryFee: 0,
  storeImageUrl: '',
  operatingHours: '',
  openTime: '09:00',
  closeTime: '22:00',
  useWeeklyHours: false
})

const loadStores = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const data = await storesApi.getOwnerStores()
    stores.value = data
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

// 카테고리는 하드코딩 (변하지 않는 고정 데이터)
const CATEGORIES: Category[] = [
  { id: 1, name: '치킨' },
  { id: 2, name: '피자' },
  { id: 3, name: '중식' },
  { id: 4, name: '한식' },
  { id: 5, name: '일식' },
  { id: 6, name: '양식' },
  { id: 7, name: '분식' },
  { id: 8, name: '카페·디저트' },
  { id: 9, name: '족발·보쌈' },
  { id: 10, name: '야식' }
]

const loadCategories = () => {
  categories.value = CATEGORIES
}

const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    phoneNumber: '',
    zipcode: '',
    address1: '',
    address2: '',
    categoryId: '',
    minOrderAmount: 0,
    deliveryFee: 0,
    storeImageUrl: '',
    operatingHours: '',
    openTime: '09:00',
    closeTime: '22:00',
    useWeeklyHours: false
  }
  // 이미지 파일 초기화
  selectedImageFile.value = null
  imagePreviewUrl.value = ''
}

// 이미지 업로드 관련 상태
const selectedImageFile = ref<File | null>(null)
const imagePreviewUrl = ref('')
const isImageUploading = ref(false)
const imageFileInput = ref<HTMLInputElement | null>(null)
const imageFileInputEdit = ref<HTMLInputElement | null>(null)

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
    }
    reader.readAsDataURL(file)
  }
}

// 이미지 업로드 함수
const uploadImage = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('domain', 'stores')
  
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

const handleCreateStore = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    // 이미지 업로드 처리
    let storeImageUrl = form.value.storeImageUrl
    if (selectedImageFile.value) {
      isImageUploading.value = true
      try {
        storeImageUrl = await uploadImage(selectedImageFile.value)
      } catch (error) {
        console.error('이미지 업로드 실패:', error)
        throw new Error('이미지 업로드에 실패했습니다.')
      } finally {
        isImageUploading.value = false
      }
    }
    
    const createData: StoreCreateRequest = {
      name: form.value.name,
      description: form.value.description,
      phoneNumber: form.value.phoneNumber,
      zipcode: form.value.zipcode,
      address1: form.value.address1,
      address2: form.value.address2,
      categoryId: Number(form.value.categoryId),
      minOrderAmount: form.value.minOrderAmount,
      deliveryFee: form.value.deliveryFee,
      operatingHours: form.value.operatingHours,
      storeImageUrl: storeImageUrl || undefined
    }
    
    const newStore = await storesApi.createStore(createData)
    stores.value.push(newStore)
    
    showCreateModal.value = false
    resetForm()
  } catch (error: any) {
    console.error('가게 생성 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 생성에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const handleEditStore = (store: Store) => {
  editingStore.value = store
  
  // 기존 운영시간에서 시간 추출
  const { openTime, closeTime } = parseOperatingHours((store as any).operatingHours || '')
  const isWeeklyFormat = (store as any).operatingHours?.startsWith('{')
  
  form.value = {
    name: store.name,
    description: store.description,
    phoneNumber: store.phoneNumber,
    zipcode: store.zipcode,
    address1: store.address1,
    address2: store.address2 || '',
    categoryId: store.categoryId.toString(),
    minOrderAmount: store.minOrderAmount,
    deliveryFee: store.deliveryFee,
    storeImageUrl: store.storeImageUrl || '',
    operatingHours: (store as any).operatingHours || '',
    openTime,
    closeTime,
    useWeeklyHours: isWeeklyFormat
  }
  // 기존 이미지가 있으면 미리보기 설정
  if (store.storeImageUrl) {
    imagePreviewUrl.value = store.storeImageUrl
  }
  showEditModal.value = true
}

const handleUpdateStore = async () => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    if (!editingStore.value) return
    
    // 이미지 업로드 처리
    let storeImageUrl = form.value.storeImageUrl
    if (selectedImageFile.value) {
      isImageUploading.value = true
      try {
        storeImageUrl = await uploadImage(selectedImageFile.value)
      } catch (error) {
        console.error('이미지 업로드 실패:', error)
        throw new Error('이미지 업로드에 실패했습니다.')
      } finally {
        isImageUploading.value = false
      }
    }
    
    const updateData: StoreUpdateRequest = {
      name: form.value.name,
      description: form.value.description,
      phoneNumber: form.value.phoneNumber,
      zipcode: form.value.zipcode,
      address1: form.value.address1,
      address2: form.value.address2,
      categoryId: Number(form.value.categoryId),
      minOrderAmount: form.value.minOrderAmount,
      deliveryFee: form.value.deliveryFee,
      operatingHours: form.value.operatingHours,
      storeImageUrl: storeImageUrl || undefined
    }
    
    const updatedStore = await storesApi.updateStore(editingStore.value.storeId, updateData)
    
    const index = stores.value.findIndex(s => s.storeId === editingStore.value!.storeId)
    if (index !== -1) {
      stores.value[index] = updatedStore
    }
    
    showEditModal.value = false
    editingStore.value = null
    resetForm()
  } catch (error: any) {
    console.error('가게 수정 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 수정에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const handleDeleteStore = async (store: Store) => {
  if (!confirm(`'${store.name}' 가게를 삭제하시겠습니까?`)) {
    return
  }
  
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    await storesApi.deleteStore(store.storeId)
    stores.value = stores.value.filter(s => s.storeId !== store.storeId)
  } catch (error: any) {
    console.error('가게 삭제 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 삭제에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const toggleStoreStatus = async (store: Store) => {
  try {
    isLoading.value = true
    errorMessage.value = ''
    
    const updatedStore = await storesApi.toggleStoreOperation(store.storeId)
    
    // 로컬 상태 업데이트
    const index = stores.value.findIndex(s => s.storeId === store.storeId)
    if (index !== -1) {
      stores.value[index] = updatedStore
    }
  } catch (error: any) {
    console.error('가게 상태 변경 실패:', error)
    errorMessage.value = error.response?.data?.message || '가게 상태 변경에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

// 전화번호 포맷팅 (자동 하이픈 추가) - 회원가입과 동일한 로직
const formatPhoneNumber = (value: string) => {
  // 숫자만 추출
  const numbers = value.replace(/[^\d]/g, '')
  
  // 포맷팅
  if (numbers.length <= 2) {
    return numbers
  }
  
  // 서울 지역번호 (02) - 최대 10자리 (02 + 8자리)
  if (numbers.startsWith('02')) {
    const limitedNumbers = numbers.slice(0, 10) // 02번호는 최대 10자리
    
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
  
  // 휴대폰 및 기타 지역번호 (010, 031, 032 등) - 최대 11자리
  const limitedNumbers = numbers.slice(0, 11)
  
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
  form.value.phoneNumber = formatted
  
  // 커서 위치 조정 (Vue의 양방향 바인딩 때문에 필요)
  setTimeout(() => {
    target.value = formatted
  }, 0)
}

// 운영시간 관련 로직
const generateTimeOptions = () => {
  const times = []
  for (let hour = 0; hour < 24; hour++) {
    for (let minute = 0; minute < 60; minute += 30) {
      const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
      const displayString = formatTimeDisplay(timeString)
      times.push({ value: timeString, display: displayString })
    }
  }
  return times
}

const formatTimeDisplay = (time: string) => {
  const [hour, minute] = time.split(':').map(Number)
  const period = hour < 12 ? '오전' : '오후'
  const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour
  return `${period} ${displayHour}:${minute.toString().padStart(2, '0')}`
}

const timeOptions = generateTimeOptions()

// 시간 변경 시 운영시간 문자열 업데이트
const updateOperatingHours = () => {
  if (!form.value.useWeeklyHours && form.value.openTime && form.value.closeTime) {
    form.value.operatingHours = `${form.value.openTime} - ${form.value.closeTime}`
  }
}

// 기존 운영시간 문자열에서 시간 추출
const parseOperatingHours = (operatingHours: string) => {
  if (!operatingHours) return { openTime: '09:00', closeTime: '22:00' }
  
  const match = operatingHours.match(/(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})/)
  if (match) {
    return { openTime: match[1], closeTime: match[2] }
  }
  return { openTime: '09:00', closeTime: '22:00' }
}

// 카카오 주소 API - 회원가입과 동일한 로직
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

      form.value.zipcode = data.zonecode;
      form.value.address1 = fullAddress;
      
      // 상세 주소 입력 필드로 포커스 이동
      setTimeout(() => {
        const detailAddressInput = document.querySelector('input[name="address2"]') as HTMLInputElement;
        if (detailAddressInput) {
          detailAddressInput.focus();
        }
      }, 100);
    }
  }).open();
}

const getCategoryName = (categoryId: number) => {
  return categories.value.find(c => c.id === categoryId)?.name || '기타'
}


const goToMenus = (storeId: number) => {
  router.push(`/menus?storeId=${storeId}`)
}

const goToOrders = (storeId: number) => {
  router.push(`/orders?storeId=${storeId}`)
}

// 시간 변경 감지 및 자동 업데이트
watch(
  () => [form.value.openTime, form.value.closeTime, form.value.useWeeklyHours],
  () => updateOperatingHours(),
  { immediate: true }
)

onMounted(() => {
  loadStores()
  loadCategories()
  // 초기 운영시간 설정
  updateOperatingHours()
})
</script>

<template>
  <div class="stores-page">
    <!-- 메인 콘텐츠 -->
    <main class="main-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>가게 정보를 불러오는 중...</p>
      </div>

      <div v-else-if="errorMessage" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMessage }}</p>
        <button @click="loadStores" class="btn-retry">다시 시도</button>
      </div>

      <div v-else-if="stores.length === 0" class="empty-state">
        <div class="empty-content">
          <h3>등록된 가게가 없습니다</h3>
          <p>첫 번째 가게를 등록하고 운영을 시작해보세요</p>
          <button @click="showCreateModal = true" class="btn-primary">가게 등록하기</button>
        </div>
      </div>

      <div v-else class="stores-content">
        <!-- 액션 바 -->
        <div class="action-bar">
          <div class="stores-header">
            <h2 class="section-title">가게 관리</h2>
            <p class="section-subtitle">{{ stores.length }}개의 가게 운영 중</p>
          </div>
          <button @click="showCreateModal = true" class="btn-primary">
            <span class="btn-icon">+</span>
            가게 추가
          </button>
        </div>
        
        <!-- 가게 목록 -->
        <div class="stores-grid">
          <div v-for="store in stores" :key="store.storeId" class="store-card">
            <!-- 가게 이미지 -->
            <div class="store-image">
              <img v-if="store.storeImageUrl" :src="store.storeImageUrl" :alt="store.name">
              <div v-else class="store-placeholder">
                <span class="placeholder-icon">🏪</span>
              </div>
              
              <!-- 상태 배지 -->
              <div class="status-badge" :class="store.status.toLowerCase()">
                <span class="status-dot"></span>
                {{ store.status === 'OPEN' ? '영업중' : '영업종료' }}
              </div>
            </div>
            
            <!-- 가게 정보 -->
            <div class="store-info">
              <div class="store-header">
                <h3 class="store-name">{{ store.name }}</h3>
                <span class="store-category">{{ getCategoryName(store.categoryId) }}</span>
              </div>
              
              <p class="store-description">{{ store.description }}</p>
              
              <div class="store-details">
                <div class="detail-item">
                  <span class="detail-icon">📍</span>
                  <span class="detail-text">{{ store.address1 }}{{ store.address2 ? ' ' + store.address2 : '' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-icon">📞</span>
                  <span class="detail-text">{{ formatPhoneNumber(store.phoneNumber) }}</span>
                </div>
              </div>
              
              <div class="store-stats">
                <div class="stat-item">
                  <span class="stat-label">최소주문</span>
                  <span class="stat-value">{{ formatCurrency(store.minOrderAmount) }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">배달비</span>
                  <span class="stat-value">{{ formatCurrency(store.deliveryFee) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 가게 액션 -->
            <div class="store-actions">
              <div class="action-row">
                <button @click="goToMenus(store.storeId)" class="btn-action primary">
                  <span class="action-icon">📋</span>
                  메뉴 관리
                </button>
                <button @click="goToOrders(store.storeId)" class="btn-action primary">
                  <span class="action-icon">📦</span>
                  주문 관리
                </button>
              </div>
              
              <div class="action-row">
                <button @click="toggleStoreStatus(store)" class="btn-action secondary">
                  <span class="action-icon">{{ store.status === 'OPEN' ? '🔴' : '🟢' }}</span>
                  {{ store.status === 'OPEN' ? '영업종료' : '영업시작' }}
                </button>
                <button @click="handleEditStore(store)" class="btn-action secondary">
                  <span class="action-icon">✏️</span>
                  수정
                </button>
                <button @click="handleDeleteStore(store)" class="btn-action danger">
                  <span class="action-icon">🗑️</span>
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 가게 생성 모달 -->
    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2>새 가게 등록</h2>
          <button @click="showCreateModal = false; resetForm()" class="close-button">×</button>
        </div>
        
        <form @submit.prevent="handleCreateStore" class="modal-form">
          <div class="form-group">
            <label>가게명 *</label>
            <input v-model="form.name" type="text" placeholder="가게 이름을 입력하세요" required>
          </div>
          
          <div class="form-group">
            <label>카테고리 *</label>
            <select v-model="form.categoryId" required>
              <option value="">카테고리 선택</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>가게 설명 *</label>
            <textarea v-model="form.description" placeholder="가게에 대한 간단한 설명을 입력하세요" required></textarea>
          </div>
          
          <div class="form-group">
            <label>전화번호 *</label>
            <input 
              v-model="form.phoneNumber" 
              type="tel" 
              placeholder="02-1234-5678" 
              @input="handlePhoneInput"
              required>
          </div>
          
          <div class="form-group">
            <label>가게 주소 *</label>
            <div class="address-section">
              <button type="button" @click="searchAddress" class="btn-search-address">
                주소 검색
              </button>
              <div v-if="!form.address1" class="address-help">
                <small class="help-text">주소 검색 버튼을 클릭하여 가게 주소를 입력하세요.</small>
              </div>
              
              <div v-if="form.address1" class="address-fields">
                <div class="form-group">
                  <label>우편번호</label>
                  <input v-model="form.zipcode" type="text" readonly>
                </div>
                
                <div class="form-group">
                  <label>기본 주소</label>
                  <input v-model="form.address1" type="text" readonly>
                </div>
                
                <div class="form-group">
                  <label>상세 주소 *</label>
                  <input 
                    v-model="form.address2" 
                    name="address2"
                    type="text" 
                    placeholder="상세 주소를 입력하세요"
                    required>
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>최소주문금액 (원) *</label>
              <input v-model.number="form.minOrderAmount" type="number" min="0" step="1000" placeholder="15000" required>
            </div>
            
            <div class="form-group">
              <label>배달비 (원) *</label>
              <input v-model.number="form.deliveryFee" type="number" min="0" step="500" placeholder="3000" required>
            </div>
          </div>
          
          <div class="form-group">
            <label>운영시간</label>
            <div class="operating-hours-section">
              <!-- 운영시간 설정 방식 선택 -->
              <div class="hours-type-selector">
                <label class="radio-option">
                  <input type="radio" :value="false" v-model="form.useWeeklyHours">
                  <span>단일 시간 (매일 동일)</span>
                </label>
                <label class="radio-option">
                  <input type="radio" :value="true" v-model="form.useWeeklyHours">
                  <span>요일별 시간 설정</span>
                </label>
              </div>
              
              <!-- 단일 시간 설정 -->
              <div v-if="!form.useWeeklyHours" class="simple-hours">
                <div class="time-picker-row">
                  <select v-model="form.openTime" @change="updateOperatingHours" class="time-select">
                    <option v-for="time in timeOptions" :key="time.value" :value="time.value">
                      {{ time.display }}
                    </option>
                  </select>
                  <span class="time-separator">부터</span>
                  <select v-model="form.closeTime" @change="updateOperatingHours" class="time-select">
                    <option v-for="time in timeOptions" :key="time.value" :value="time.value">
                      {{ time.display }}
                    </option>
                  </select>
                  <span class="time-separator">까지</span>
                </div>
                <div v-if="form.operatingHours" class="operating-hours-preview">
                  운영시간: {{ form.operatingHours }}
                </div>
              </div>
              
              <!-- 요일별 시간 설정 -->
              <div v-else class="weekly-hours">
                <WeeklyOperatingHours v-model="form.operatingHours" />
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>가게 이미지</label>
            <div class="image-upload-section">
              <input 
                type="file" 
                accept="image/jpeg,image/jpg,image/png"
                @change="handleImageSelect"
                ref="imageFileInput"
                class="file-input-hidden">
              <button type="button" @click="imageFileInput?.click()" class="btn-image-upload">
                이미지 선택
              </button>
              
              <div v-if="imagePreviewUrl" class="image-preview-container">
                <div class="image-preview">
                  <img :src="imagePreviewUrl" alt="미리보기">
                  <button type="button" @click="selectedImageFile = null; imagePreviewUrl = ''" class="remove-image">×</button>
                </div>
              </div>
              
              <small class="help-text">JPEG 또는 PNG 파일, 최대 5MB</small>
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="showCreateModal = false; resetForm()" class="btn-cancel">
              취소
            </button>
            <button 
              type="submit" 
              class="btn-submit" 
              :disabled="isLoading || !form.address1 || !form.address2">
              {{ isLoading ? '등록 중...' : '등록하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 가게 수정 모달 -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2>가게 정보 수정</h2>
          <button @click="showEditModal = false; resetForm()" class="close-button">×</button>
        </div>
        
        <form @submit.prevent="handleUpdateStore" class="modal-form">
          <div class="form-group">
            <label>가게명 *</label>
            <input v-model="form.name" type="text" placeholder="가게 이름을 입력하세요" required>
          </div>
          
          <div class="form-group">
            <label>카테고리 *</label>
            <select v-model="form.categoryId" required>
              <option value="">카테고리 선택</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label>가게 설명 *</label>
            <textarea v-model="form.description" placeholder="가게에 대한 간단한 설명을 입력하세요" required></textarea>
          </div>
          
          <div class="form-group">
            <label>전화번호 *</label>
            <input 
              v-model="form.phoneNumber" 
              type="tel" 
              placeholder="02-1234-5678" 
              @input="handlePhoneInput"
              required>
          </div>
          
          <div class="form-group">
            <label>가게 주소 *</label>
            <div class="address-section">
              <button type="button" @click="searchAddress" class="btn-search-address">
                주소 검색
              </button>
              
              <div v-if="form.address1" class="address-fields">
                <div class="form-group">
                  <label>우편번호</label>
                  <input v-model="form.zipcode" type="text" readonly>
                </div>
                
                <div class="form-group">
                  <label>기본 주소</label>
                  <input v-model="form.address1" type="text" readonly>
                </div>
                
                <div class="form-group">
                  <label>상세 주소 *</label>
                  <input 
                    v-model="form.address2" 
                    name="address2"
                    type="text" 
                    placeholder="상세 주소를 입력하세요"
                    required>
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>최소주문금액 (원) *</label>
              <input v-model.number="form.minOrderAmount" type="number" min="0" step="1000" required>
            </div>
            
            <div class="form-group">
              <label>배달비 (원) *</label>
              <input v-model.number="form.deliveryFee" type="number" min="0" step="500" required>
            </div>
          </div>
          
          <div class="form-group">
            <label>운영시간</label>
            <div class="operating-hours-section">
              <!-- 운영시간 설정 방식 선택 -->
              <div class="hours-type-selector">
                <label class="radio-option">
                  <input type="radio" :value="false" v-model="form.useWeeklyHours">
                  <span>단일 시간 (매일 동일)</span>
                </label>
                <label class="radio-option">
                  <input type="radio" :value="true" v-model="form.useWeeklyHours">
                  <span>요일별 시간 설정</span>
                </label>
              </div>
              
              <!-- 단일 시간 설정 -->
              <div v-if="!form.useWeeklyHours" class="simple-hours">
                <div class="time-picker-row">
                  <select v-model="form.openTime" @change="updateOperatingHours" class="time-select">
                    <option v-for="time in timeOptions" :key="time.value" :value="time.value">
                      {{ time.display }}
                    </option>
                  </select>
                  <span class="time-separator">부터</span>
                  <select v-model="form.closeTime" @change="updateOperatingHours" class="time-select">
                    <option v-for="time in timeOptions" :key="time.value" :value="time.value">
                      {{ time.display }}
                    </option>
                  </select>
                  <span class="time-separator">까지</span>
                </div>
                <div v-if="form.operatingHours" class="operating-hours-preview">
                  운영시간: {{ form.operatingHours }}
                </div>
              </div>
              
              <!-- 요일별 시간 설정 -->
              <div v-else class="weekly-hours">
                <WeeklyOperatingHours v-model="form.operatingHours" />
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label>가게 이미지</label>
            <div class="image-upload-section">
              <input 
                type="file" 
                accept="image/jpeg,image/jpg,image/png"
                @change="handleImageSelect"
                ref="imageFileInputEdit"
                class="file-input-hidden">
              <button type="button" @click="imageFileInputEdit?.click()" class="btn-image-upload">
                이미지 선택
              </button>
              
              <div v-if="imagePreviewUrl" class="image-preview-container">
                <div class="image-preview">
                  <img :src="imagePreviewUrl" alt="미리보기">
                  <button type="button" @click="selectedImageFile = null; imagePreviewUrl = ''" class="remove-image">×</button>
                </div>
              </div>
              
              <small class="help-text">JPEG 또는 PNG 파일, 최대 5MB</small>
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="showEditModal = false; resetForm()" class="btn-cancel">
              취소
            </button>
            <button type="submit" class="btn-submit" :disabled="isLoading">
              {{ isLoading ? '수정 중...' : '수정하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 전체 페이지 */
.stores-page {
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

/* 상태 */
.loading-state {
  text-align: center;
  padding: 4rem 2rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #dc2626;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 1rem;
}

.btn-retry {
  padding: 8px 16px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 1rem;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.empty-content {
  text-align: center;
}

.empty-content h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-content p {
  color: #6b7280;
  margin-bottom: 24px;
}

.empty-content .btn-primary {
  display: inline-block;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.empty-content .btn-primary:hover {
  background: #2563eb;
}

/* 가게 콘텐츠 */

.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

/* 가게 카드 */
.store-card {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
}

.store-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.store-image {
  position: relative;
  width: 100%;
  height: 200px;
  background: #f3f4f6;
  overflow: hidden;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.placeholder-icon {
  font-size: 48px;
  color: #cbd5e1;
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.status-badge.open {
  background: rgba(34, 197, 94, 0.9);
  color: white;
}

.status-badge.closed {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.store-info {
  padding: 1.5rem;
  flex: 1;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.store-name {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  line-height: 1.2;
}

.store-category {
  font-size: 0.75rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.store-description {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.5;
  margin: 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 액션 바 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.stores-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px 0;
}

.section-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-icon {
  font-size: 16px;
  font-weight: 700;
}

.store-details {
  margin: 12px 0;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #6b7280;
}

.detail-icon {
  font-size: 12px;
  width: 16px;
  flex-shrink: 0;
}

.detail-text {
  line-height: 1.4;
}

.store-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin: 16px 0;
}

.stat-item {
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 11px;
  color: #6b7280;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-value {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.store-actions {
  padding: 0 1.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-row {
  display: flex;
  gap: 8px;
}

.btn-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex: 1;
  text-align: center;
}

.btn-action.primary {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

.btn-action.primary:hover {
  background: #dbeafe;
  color: #2563eb;
}

.btn-action.secondary {
  background: #f8fafc;
  border-color: #e2e8f0;
  color: #64748b;
}

.btn-action.secondary:hover {
  background: #f1f5f9;
  color: #475569;
}

.btn-action.danger {
  background: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
}

.btn-action.danger:hover {
  background: #fee2e2;
  color: #b91c1c;
}

.action-icon {
  font-size: 14px;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(2px);
}

.modal-container {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.modal-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-button:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-form {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
  background: #fafafa;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #9ca3af;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn-cancel,
.btn-submit {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f8fafc;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #f1f5f9;
  color: #374151;
}

.btn-submit {
  background: #3b82f6;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 반응형 */
@media (max-width: 1200px) {
  .stores-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .header-container,
  .nav-container,
  .main-content {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .stores-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .nav-menu {
    overflow-x: auto;
  }
  
  .nav-item {
    white-space: nowrap;
    padding: 12px 16px;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .page-subtitle {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .header-container {
    height: 56px;
  }
  
  .btn-primary {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .btn-icon {
    font-size: 14px;
  }
  
  .store-actions {
    padding: 0 1rem 1rem;
  }
  
  .action-row {
    flex-direction: column;
  }
  
  .btn-action {
    justify-content: center;
  }
}

/* 주소 검색 관련 스타일 */
.address-section {
  margin-bottom: 1rem;
}

.btn-search-address {
  width: 100%;
  padding: 12px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 1rem;
}

.btn-search-address:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.address-fields {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  background: #f9fafb;
}

.address-fields .form-group {
  margin-bottom: 0.75rem;
}

.address-fields .form-group:last-child {
  margin-bottom: 0;
}

.address-fields input[readonly] {
  background: #f3f4f6;
  color: #6b7280;
  cursor: default;
}

/* 이미지 업로드 관련 스타일 */
.image-upload-section {
  margin-bottom: 1rem;
}

.file-input-hidden {
  display: none;
}

.btn-image-upload {
  width: 100%;
  padding: 12px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 1rem;
  display: block;
  text-align: center;
}

.btn-image-upload:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.image-preview-container {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  background: #f9fafb;
  margin-bottom: 1rem;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  max-width: 200px;
  max-height: 150px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.remove-image:hover {
  background: #dc2626;
  transform: scale(1.1);
}

.help-text {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-top: 0.25rem;
}

.address-help {
  margin-top: 0.5rem;
  padding: 8px 12px;
  background: #fef3c7;
  border: 1px solid #f59e0b;
  border-radius: 6px;
}

.address-help .help-text {
  color: #92400e;
  margin: 0;
}

/* 운영시간 선택기 스타일 */
.operating-hours-section {
  margin-bottom: 1rem;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 0.5rem;
}

.time-select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.time-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.time-separator {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  min-width: 30px;
  text-align: center;
}

.operating-hours-preview {
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  font-size: 13px;
  color: #0369a1;
  font-weight: 500;
}

/* 라디오 버튼 스타일 */
.hours-type-selector {
  display: flex;
  gap: 20px;
  margin-bottom: 1rem;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
}

.radio-option input[type="radio"] {
  width: 16px;
  height: 16px;
  accent-color: #3b82f6;
}

.simple-hours,
.weekly-hours {
  margin-top: 0.5rem;
}

/* 모바일 반응형 */
@media (max-width: 768px) {
  .time-picker-row {
    flex-direction: column;
    gap: 8px;
  }
  
  .time-select {
    width: 100%;
  }
  
  .time-separator {
    min-width: auto;
  }
  
  .hours-type-selector {
    flex-direction: column;
    gap: 12px;
  }
}

</style>