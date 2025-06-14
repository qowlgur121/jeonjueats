<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()

onMounted(async () => {
  await authStore.initialize()
})
</script>

<template>
  <!-- 전주이츠 사장님 앱 -->
  <div class="app">
    <RouterView />
  </div>
</template>

<style scoped>
.app {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}
</style>
