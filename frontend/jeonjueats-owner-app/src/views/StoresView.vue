<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import * as storesApi from '../api/stores'
import * as categoriesApi from '../api/categories'

const router = useRouter()
const authStore = useAuthStore()

const stores = ref<any[]>([])
const categories = ref<any[]>([])
const isLoading = ref(false)
const errorMessage = ref('')
const showCreateModal = ref(false)

const form = ref({
  name: '',
  description: '',
  phoneNumber: '',
  address: '',
  categoryId: '',
  minOrderAmount: 0,
  deliveryFee: 0,
  storeImageUrl: ''
})

const loadStores = async () => {
  try {
    isLoading.value = true
    stores.value = await storesApi.getOwnerStores()
  } catch (error: any) {
    console.error('가게 목록 조회 실패:', error)
    errorMessage.value = '가게 목록을 불러올 수 없습니다.'
  } finally {
    isLoading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await categoriesApi.getCategories()
  } catch (error: any) {
    console.error('카테고리 조회 실패:', error)
  }
}

const handleCreateStore = async () => {
  try {
    isLoading.value = true
    await storesApi.createStore({
      name: form.value.name,
      description: form.value.description,
      phoneNumber: form.value.phoneNumber,
      address: form.value.address,
      categoryId: Number(form.value.categoryId),
      minOrderAmount: form.value.minOrderAmount,
      deliveryFee: form.value.deliveryFee,
      storeImageUrl: form.value.storeImageUrl
    })
    
    showCreateModal.value = false
    await loadStores()
  } catch (error: any) {
    console.error('가게 생성 실패:', error)
    errorMessage.value = '가게 생성에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.push('/dashboard')
}

onMounted(() => {
  loadStores()
  loadCategories()
})
</script>

<template>
  <div class="stores-page">
    <!-- 헤더 -->
    <header class="page-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
        </svg>
      </button>
      <h1 class="page-title">가게 관리</h1>
      <button @click="showCreateModal = true" class="btn-add">
        가게 추가
      </button>
    </header>

    <!-- 컨텐츠 -->
    <div class="page-content">
      <div v-if="isLoading" class="loading">
        가게 정보를 불러오는 중...
      </div>

      <div v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <div v-else-if="stores.length === 0" class="empty-state">
        <div class="empty-icon">🏪</div>
        <h3>등록된 가게가 없습니다</h3>
        <p>첫 번째 가게를 등록해보세요!</p>
      </div>

      <div v-else class="stores-list">
        <div v-for="store in stores" :key="store.id" class="store-item">
          <div class="store-image">
            <img v-if="store.storeImageUrl" :src="store.storeImageUrl" :alt="store.name">
            <div v-else class="store-placeholder">🏪</div>
          </div>
          
          <div class="store-info">
            <div class="store-header">
              <h3 class="store-name">{{ store.name }}</h3>
              <span class="store-status" :class="store.status.toLowerCase()">
                {{ store.status === 'OPEN' ? '영업중' : '영업종료' }}
              </span>
            </div>
            
            <p class="store-category">{{ store.categoryName }}</p>
            <p class="store-description">{{ store.description }}</p>
            <p class="store-address">📍 {{ store.address }}</p>
            
            <div class="store-meta">
              <span>최소주문: {{ store.minOrderAmount.toLocaleString() }}원</span>
              <span>배달비: {{ store.deliveryFee.toLocaleString() }}원</span>
            </div>
            
            <div class="store-actions">
              <button @click="$router.push(`/stores/${store.id}/menus`)" class="btn-action">
                메뉴 관리
              </button>
              <button @click="$router.push(`/stores/${store.id}/orders`)" class="btn-action">
                주문 관리
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 가게 생성 모달 -->
    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h2>새 가게 등록</h2>
          <button @click="showCreateModal = false" class="close-button">×</button>
        </div>
        
        <form @submit.prevent="handleCreateStore" class="modal-form">
          <div class="form-group">
            <label>가게명 *</label>
            <input v-model="form.name" type="text" required>
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
            <textarea v-model="form.description" required></textarea>
          </div>
          
          <div class="form-group">
            <label>전화번호 *</label>
            <input v-model="form.phoneNumber" type="tel" required>
          </div>
          
          <div class="form-group">
            <label>주소 *</label>
            <input v-model="form.address" type="text" required>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>최소주문금액 *</label>
              <input v-model.number="form.minOrderAmount" type="number" min="0" required>
            </div>
            
            <div class="form-group">
              <label>배달비 *</label>
              <input v-model.number="form.deliveryFee" type="number" min="0" required>
            </div>
          </div>
          
          <div class="modal-actions">
            <button type="button" @click="showCreateModal = false" class="btn-cancel">
              취소
            </button>
            <button type="submit" class="btn-submit" :disabled="isLoading">
              {{ isLoading ? '등록 중...' : '등록하기' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stores-page {
  min-height: 100vh;
  background-color: #f8fafc;
}

/* 헤더 */
.page-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-button {
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
  background-color: #f8fafc;
}

.back-button svg {
  width: 24px;
  height: 24px;
  color: #374151;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.btn-add {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-add:hover {
  background: #2563eb;
}

/* 컨텐츠 */
.page-content {
  padding: 20px;
}

.loading,
.error-message {
  text-align: center;
  padding: 40px;
  color: #64748b;
}

.error-message {
  color: #dc2626;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  margin: 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.empty-state p {
  color: #64748b;
}

/* 가게 목록 */
.stores-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.store-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  display: flex;
}

.store-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-placeholder {
  font-size: 32px;
  color: #cbd5e1;
}

.store-info {
  flex: 1;
  padding: 16px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.store-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.store-status.open {
  background: #dcfce7;
  color: #166534;
}

.store-status.closed {
  background: #fee2e2;
  color: #991b1b;
}

.store-category {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.store-description {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
}

.store-address {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.store-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 16px;
}

.store-actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 8px 16px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-action:hover {
  background: #e2e8f0;
  color: #475569;
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
  padding: 20px;
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-button:hover {
  background: #f1f5f9;
}

.modal-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
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
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-submit {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #f1f5f9;
}

.btn-submit {
  background: #3b82f6;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #2563eb;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 반응형 */
@media (max-width: 768px) {
  .store-item {
    flex-direction: column;
  }
  
  .store-image {
    width: 100%;
    height: 200px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>