<script setup lang="ts">
import { ref, computed } from 'vue'

// Props 정의
interface Props {
  menuItem: any
  isVisible: boolean
}

const props = defineProps<Props>()

// Emits 정의
const emit = defineEmits<{
  close: []
  addToCart: [menuItem: any, options: any]
}>()

// 선택된 옵션들
const selectedOptions = ref<Record<string, string>>({})
const quantity = ref(1)

// 모달 닫기
const closeModal = () => {
  emit('close')
  resetModal()
}

// 모달 초기화
const resetModal = () => {
  selectedOptions.value = {}
  quantity.value = 1
}

// 수량 증가/감소
const increaseQuantity = () => {
  quantity.value++
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 옵션 선택
const selectOption = (optionName: string, choice: string) => {
  selectedOptions.value[optionName] = choice
}

// 장바구니에 담기
const addToCart = () => {
  console.log('MenuItemModal: 장바구니 담기 버튼 클릭됨')
  console.log('MenuItemModal: 메뉴 아이템:', props.menuItem)
  console.log('MenuItemModal: 선택된 옵션:', selectedOptions.value)
  console.log('MenuItemModal: 수량:', quantity.value)
  
  emit('addToCart', props.menuItem, {
    ...selectedOptions.value,
    quantity: quantity.value
  })
  resetModal()
}

// 총 가격 계산
const totalPrice = computed(() => {
  let price = props.menuItem?.price || 0
  
  // 옵션 가격 추가 (예: "+2000원" 형태의 옵션 가격 파싱)
  Object.values(selectedOptions.value).forEach(choice => {
    const priceMatch = choice.match(/\+(\d+)원/)
    if (priceMatch) {
      price += parseInt(priceMatch[1])
    }
  })
  
  return price * quantity.value
})

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 필수 옵션 체크
const canAddToCart = computed(() => {
  if (!props.menuItem?.options) return true
  
  return props.menuItem.options.every((option: any) => {
    if (option.required) {
      return selectedOptions.value[option.name]
    }
    return true
  })
})
</script>

<template>
  <!-- 메뉴 아이템 모달 -->
  <div v-if="isVisible" class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      
      <!-- 모달 헤더 -->
      <div class="modal-header">
        <button @click="closeModal" class="close-button">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>

      <!-- 메뉴 이미지 -->
      <div class="menu-image-section">
        <img 
          v-if="menuItem?.menuImageUrl" 
          :src="menuItem.menuImageUrl" 
          :alt="menuItem.name"
          class="modal-menu-image"
        >
        <div v-else class="modal-image-placeholder">
          <span class="placeholder-emoji">🍽️</span>
        </div>
      </div>

      <!-- 메뉴 정보 -->
      <div class="menu-info-section">
        <h2 class="menu-title">{{ menuItem?.name }}</h2>
        <p class="menu-description">{{ menuItem?.description }}</p>
        
        <div class="menu-price-info">
          <span v-if="menuItem?.originalPrice" class="original-price">
            {{ formatPrice(menuItem.originalPrice) }}
          </span>
          <span class="current-price">{{ formatPrice(menuItem?.price || 0) }}</span>
        </div>
      </div>

      <!-- 옵션 선택 -->
      <div v-if="menuItem?.options" class="options-section">
        <div 
          v-for="option in menuItem.options"
          :key="option.name"
          class="option-group"
        >
          <div class="option-header">
            <h3 class="option-title">{{ option.name }}</h3>
            <span v-if="option.required" class="required-badge">필수</span>
          </div>
          
          <div class="option-choices">
            <label 
              v-for="choice in option.choices"
              :key="choice"
              class="choice-item"
              :class="{ 'selected': selectedOptions[option.name] === choice }"
            >
              <input 
                type="radio"
                :name="option.name"
                :value="choice"
                @change="selectOption(option.name, choice)"
                class="choice-radio"
              >
              <span class="choice-text">{{ choice }}</span>
            </label>
          </div>
        </div>
      </div>

      <!-- 수량 선택 -->
      <div class="quantity-section">
        <h3 class="section-title">수량</h3>
        <div class="quantity-controls">
          <button 
            @click="decreaseQuantity"
            :disabled="quantity <= 1"
            class="quantity-button"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13H5v-2h14v2z"/>
            </svg>
          </button>
          <span class="quantity-display">{{ quantity }}</span>
          <button 
            @click="increaseQuantity"
            class="quantity-button"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 장바구니 담기 버튼 -->
      <div class="modal-footer">
        <button 
          @click="addToCart"
          :disabled="!canAddToCart"
          class="add-to-cart-button btn-primary"
          :class="{ 'disabled': !canAddToCart }"
        >
          <span class="button-text">배달 카트에 담기</span>
          <span class="button-price">{{ formatPrice(totalPrice) }}</span>
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 모달 컨테이너 */
.modal-container {
  background-color: white;
  border-radius: 20px 20px 0 0;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { 
    transform: translateY(100%);
    opacity: 0;
  }
  to { 
    transform: translateY(0);
    opacity: 1;
  }
}

/* 모달 헤더 */
.modal-header {
  position: relative;
  padding: 1rem;
  display: flex;
  justify-content: flex-end;
}

.close-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f3f4f6;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
}

.close-button:hover {
  background-color: #e5e7eb;
  color: #374151;
}

.close-button svg {
  width: 18px;
  height: 18px;
}

/* 메뉴 이미지 */
.menu-image-section {
  padding: 0 1.5rem;
  margin-bottom: 1.5rem;
}

.modal-menu-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 16px;
}

.modal-image-placeholder {
  width: 100%;
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  border-radius: 16px;
}

.placeholder-emoji {
  font-size: 80px;
  opacity: 0.6;
}

/* 메뉴 정보 */
.menu-info-section {
  padding: 0 1.5rem 1.5rem;
  border-bottom: 1px solid #f1f3f4;
}

.menu-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.menu-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 16px;
}

.menu-price-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.original-price {
  font-size: 16px;
  color: #9ca3af;
  text-decoration: line-through;
}

.current-price {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

/* 옵션 섹션 */
.options-section {
  padding: 1.5rem;
  border-bottom: 1px solid #f1f3f4;
}

.option-group {
  margin-bottom: 2rem;
}

.option-group:last-child {
  margin-bottom: 0;
}

.option-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 1rem;
}

.option-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.required-badge {
  padding: 2px 8px;
  background-color: #ef4444;
  color: white;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.option-choices {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.choice-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.choice-item:hover {
  border-color: #d1d5db;
  background-color: #f9fafb;
}

.choice-item.selected {
  border-color: #374151;
  background-color: #f3f4f6;
}

.choice-radio {
  margin-right: 12px;
  accent-color: #374151;
}

.choice-text {
  font-size: 16px;
  color: #374151;
  font-weight: 500;
}

/* 수량 섹션 */
.quantity-section {
  padding: 1.5rem;
  border-bottom: 1px solid #f1f3f4;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #374151;
}

.quantity-button:hover:not(:disabled) {
  border-color: #374151;
  background-color: #f9fafb;
}

.quantity-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-button svg {
  width: 20px;
  height: 20px;
}

.quantity-display {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  min-width: 40px;
  text-align: center;
}

/* 모달 푸터 */
.modal-footer {
  padding: 1.5rem;
}

.add-to-cart-button {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 20px !important;
  background-color: #3b82f6;
  border-radius: 12px;
}

.add-to-cart-button:hover:not(.disabled) {
  background-color: #2563eb;
}

.add-to-cart-button.disabled {
  background-color: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.button-text {
  font-weight: 600;
}

.button-price {
  font-weight: 700;
}

/* 반응형 */
@media (min-width: 768px) {
  .modal-overlay {
    align-items: center;
  }
  
  .modal-container {
    border-radius: 20px;
    max-height: 85vh;
  }
}
</style>