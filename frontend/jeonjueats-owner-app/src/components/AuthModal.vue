<script setup lang="ts">
import { useRouter } from 'vue-router'

interface Props {
  modelValue: boolean
}

defineProps<Props>()
const emit = defineEmits(['update:modelValue'])
const router = useRouter()

const closeModal = () => {
  emit('update:modelValue', false)
}

const goToLogin = () => {
  router.push('/login')
  closeModal()
}

const goToSignup = () => {
  router.push('/signup')
  closeModal()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="modal-overlay" @click="closeModal">
        <div class="modal-container" @click.stop>
          <!-- 헤더 -->
          <div class="modal-header">
            <button class="close-button" @click="closeModal">×</button>
          </div>
          
          <!-- 제목 섹션 -->
          <div class="title-section">
            <h2 class="modal-title">전주이츠 사장님</h2>
          </div>
          
          <!-- 메인 콘텐츠 -->
          <div class="modal-content">
            <p class="modal-description">
              사장님 서비스를 이용하시려면<br>
              로그인이 필요합니다
            </p>
            
            <div class="auth-buttons">
              <button class="btn-primary" @click="goToLogin">
                로그인
              </button>
              <button class="btn-secondary" @click="goToSignup">
                사장님 회원가입
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
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
}

.modal-container {
  background-color: white;
  width: 100%;
  max-width: 100%;
  border-radius: 20px 20px 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -5px 20px rgba(0, 0, 0, 0.15);
}

/* 헤더 */
.modal-header {
  background-color: white;
  padding: 16px 20px 0px 20px;
  display: flex;
  justify-content: flex-end;
}

.close-button {
  background: none;
  border: none;
  color: #666;
  font-size: 18px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.close-button:hover {
  background-color: #f5f5f5;
}

/* 제목 섹션 */
.title-section {
  background-color: white;
  padding: 20px 24px 5px 24px;
  text-align: center;
}


/* 메인 콘텐츠 */
.modal-content {
  padding: 10px 24px 32px 24px;
  text-align: center;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.modal-description {
  font-size: 18px;
  color: #666;
  text-align: center;
  margin: 0px 0 32px 0;
  line-height: 1.5;
}

.auth-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.btn-primary {
  width: 100%;
  padding: 16px 20px;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #1e90ff;
  color: white;
}

.btn-primary:hover {
  background-color: #1c7ed6;
}

.btn-secondary {
  width: 100%;
  padding: 16px 20px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background-color: white;
  color: #666;
}

.btn-secondary:hover {
  background-color: #f7fafc;
  border-color: #a0aec0;
}

/* 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: translateY(100%);
}

/* 반응형 */
@media (max-width: 500px) {
  .modal-container {
    max-width: 100%;
  }
}
</style>