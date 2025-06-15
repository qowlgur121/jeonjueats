<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  isVisible: boolean
  title?: string
  message: string
  type?: 'info' | 'warning' | 'error' | 'success'
  confirmText?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '알림',
  type: 'info',
  confirmText: '확인'
})

const emit = defineEmits<{
  close: []
}>()

const iconEmoji = computed(() => {
  switch (props.type) {
    case 'success': return '✅'
    case 'warning': return '⚠️'
    case 'error': return '❌'
    default: return '🕐'
  }
})

const modalClass = computed(() => {
  return `alert-modal alert-${props.type}`
})

const handleClose = () => {
  emit('close')
}
</script>

<template>
  <Transition name="modal">
    <div v-if="isVisible" class="modal-overlay" @click="handleClose">
      <div :class="modalClass" @click.stop>
        <div class="alert-icon">{{ iconEmoji }}</div>
        <h3 class="alert-title">{{ title }}</h3>
        <p class="alert-message">{{ message }}</p>
        <button class="alert-button" @click="handleClose">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </Transition>
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
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

.alert-modal {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.alert-modal::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #6b7280;
}

.alert-info::before {
  background: #3b82f6;
}

.alert-success::before {
  background: #10b981;
}

.alert-warning::before {
  background: #f59e0b;
}

.alert-error::before {
  background: #ef4444;
}

.alert-icon {
  font-size: 48px;
  margin-bottom: 1rem;
  animation: bounce 0.5s ease-out;
}

.alert-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.75rem;
}

.alert-message {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 1.5rem;
}

.alert-button {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.alert-button:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.alert-button:active {
  transform: translateY(0);
}

/* 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .alert-modal,
.modal-leave-active .alert-modal {
  transition: all 0.3s ease;
}

.modal-enter-from .alert-modal {
  transform: scale(0.9);
  opacity: 0;
}

.modal-leave-to .alert-modal {
  transform: scale(0.9);
  opacity: 0;
}

@keyframes bounce {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 반응형 */
@media (max-width: 640px) {
  .alert-modal {
    padding: 1.5rem;
  }
  
  .alert-icon {
    font-size: 40px;
  }
  
  .alert-title {
    font-size: 18px;
  }
  
  .alert-message {
    font-size: 14px;
  }
  
  .alert-button {
    width: 100%;
  }
}
</style>