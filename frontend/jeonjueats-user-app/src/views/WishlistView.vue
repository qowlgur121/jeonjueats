<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getWishlists, toggleWishlist, type WishlistResponseDto } from '../api/wishlist'

const router = useRouter()

const wishlistItems = ref<WishlistResponseDto[]>([])
const filteredItems = ref<WishlistResponseDto[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const sortBy = ref('latest')

// 컴포넌트 마운트 시 찜 목록 로드
onMounted(async () => {
  await loadWishlists()
})

// 찜 목록 로드
const loadWishlists = async () => {
  try {
    isLoading.value = true
    error.value = null
    
    const response = await getWishlists()
    wishlistItems.value = response.content
    applySort()
    
  } catch (err) {
    console.error('찜 목록 로드 실패:', err)
    error.value = '찜 목록을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

// 정렬 적용
const applySort = () => {
  let sorted = [...wishlistItems.value]
  
  switch (sortBy.value) {
    case 'latest':
      sorted.sort((a, b) => new Date(b.wishedAt).getTime() - new Date(a.wishedAt).getTime())
      break
    case 'name':
      sorted.sort((a, b) => a.store.storeName.localeCompare(b.store.storeName))
      break
    case 'rating':
      // 평점 정보가 없으므로 이름순으로 대체
      sorted.sort((a, b) => a.store.storeName.localeCompare(b.store.storeName))
      break
  }
  
  filteredItems.value = sorted
}

// 정렬 옵션 변경 감지
watch(sortBy, () => {
  applySort()
})

// 찜 해제
const removeFromWishlist = async (storeId: number) => {
  try {
    console.log('찜 해제 시작:', storeId)
    const response = await toggleWishlist(storeId)
    
    if (!response.wished) {
      // 찜 목록에서 제거
      const removedItem = wishlistItems.value.find(item => item.store.storeId === storeId)
      wishlistItems.value = wishlistItems.value.filter(item => item.store.storeId !== storeId)
      applySort()
      console.log('찜 해제 완료:', removedItem?.store.storeName || '가게')
    }
    
  } catch (err) {
    console.error('찜 해제 실패:', err)
    alert('찜 해제에 실패했습니다.')
  }
}

// 주문하기 (가게 상세 페이지로 이동)
const goToStore = (storeId: number) => {
  router.push(`/stores/${storeId}`)
}

// 날짜 포맷팅
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}
</script>

<template>
  <!-- 즐겨찾기 페이지 -->
  <div class="wishlist-page">
    
    <!-- 헤더 섹션 -->
    <section class="header-section">
      <div class="section-container">
        <div class="header-content">
          <h1 class="page-title">즐겨찾기</h1>
          <p class="page-subtitle">좋아하는 맛집을 저장해보세요</p>
        </div>
      </div>
    </section>

    <!-- 찜 목록 컨텐츠 -->
    <div class="wishlist-content">
      
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="loading-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">찜 목록을 불러오는 중...</p>
      </div>
      
      <!-- 에러 상태 -->
      <div v-else-if="error" class="error-container">
        <p class="error-text">{{ error }}</p>
        <button @click="loadWishlists" class="retry-btn">다시 시도</button>
      </div>
      
      <!-- 찜 목록이 있을 때 -->
      <section v-else-if="wishlistItems.length > 0" class="stores-section">
        <div class="section-container">
          
          <!-- 통계 정보 -->
          <div class="stats-info">
            <h2 class="stats-title">
              총 <span class="stats-number">{{ wishlistItems.length }}개</span> 매장
            </h2>
            <select v-model="sortBy" class="sort-select">
              <option value="latest">최신순</option>
              <option value="name">이름순</option>
              <option value="rating">평점순</option>
            </select>
          </div>
          
          <!-- 찜한 매장 목록 -->
          <div class="stores-grid">
            <div 
              v-for="item in filteredItems" 
              :key="item.wishlistId"
              class="store-card"
              @click="goToStore(item.store.storeId)"
            >
              <!-- 매장 이미지 영역 -->
              <div class="store-image-area">
                <img 
                  v-if="item.store.storeImageUrl" 
                  :src="item.store.storeImageUrl" 
                  :alt="item.store.storeName"
                  class="store-image"
                >
                <div v-else class="store-image-placeholder">🏪</div>
                <button 
                  @click.stop="removeFromWishlist(item.store.storeId)"
                  class="heart-btn active"
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                  </svg>
                </button>
              </div>
              
              <!-- 매장 정보 -->
              <div class="store-info">
                <div class="store-header">
                  <h3 class="store-name">{{ item.store.storeName }}</h3>
                </div>
                
                <p class="store-description">{{ item.store.description }}</p>
                
                <div class="store-stats">
                  <!-- 평점 정보는 백엔드에서 제공하지 않으므로 제거 -->
                  <div class="delivery-info">
                    <span class="delivery-fee">
                      {{ item.store.deliveryTip === 0 ? '무료배달' : `배달비 ${item.store.deliveryTip.toLocaleString()}원` }}
                    </span>
                    <span class="min-order">
                      최소주문 {{ item.store.minOrderAmount.toLocaleString() }}원
                    </span>
                  </div>
                </div>
                
                <div class="store-actions">
                  <span class="added-date">{{ formatDate(item.wishedAt) }} 즐겨찾기</span>
                  <button 
                    @click.stop="goToStore(item.store.storeId)"
                    class="order-btn"
                  >
                    주문하기
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 빈 상태 -->
      <section v-else class="empty-section">
        <div class="section-container">
          <div class="empty-content">
            <div class="empty-icon">💔</div>
            <h3 class="empty-title">아직 즐겨찾기한 매장이 없어요</h3>
            <p class="empty-description">마음에 드는 매장을 즐겨찾기 해서<br>나중에 쉽게 찾아보세요</p>
            <router-link to="/search" class="explore-btn">
              매장 둘러보기
            </router-link>
          </div>
        </div>
      </section>
      
    </div>

  </div>
</template>

<style scoped>
/* 즐겨찾기 페이지 컨테이너 */
.wishlist-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 로딩 컨테이너 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem 0;
  border-radius: 12px;
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
  font-size: 16px;
  color: #6b7280;
}

/* 에러 컨테이너 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 0;
  background-color: white;
  margin: 1rem 0;
  border-radius: 12px;
}

.error-text {
  font-size: 16px;
  color: #ef4444;
  margin-bottom: 1rem;
}

.retry-btn {
  padding: 8px 20px;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background-color: #1f2937;
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

/* 찜 목록 컨텐츠 */
.wishlist-content {
  padding-top: 1rem;
}

/* 매장 목록 섹션 */
.stores-section {
  background-color: white;
  margin: 1rem 0;
  padding: 2rem 0;
}

/* 통계 정보 */
.stats-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.stats-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.stats-number {
  color: #ef4444;
  font-weight: 700;
}

.sort-select {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: white;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  outline: none;
}

.sort-select:focus {
  border-color: #374151;
}

/* 매장 그리드 */
.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

/* 매장 카드 */
.store-card {
  background-color: white;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
}

.store-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border-color: #e5e7eb;
}

/* 매장 이미지 영역 */
.store-image-area {
  position: relative;
  height: 150px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-image-placeholder {
  font-size: 48px;
}

.heart-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background-color: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
}

.heart-btn svg {
  width: 20px;
  height: 20px;
  color: #ef4444;
}

.heart-btn:hover {
  background-color: white;
  transform: scale(1.1);
}

/* 매장 정보 */
.store-info {
  padding: 1.25rem;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.store-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.store-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
  line-height: 1.4;
}

.store-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star {
  font-size: 12px;
}

.rating {
  font-weight: 600;
  color: #1f2937;
}

.review-count {
  color: #6b7280;
  font-size: 12px;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  gap: 2px;
}

.delivery-fee {
  color: #374151;
  font-weight: 500;
}

.min-order {
  color: #6b7280;
  font-size: 12px;
}

.store-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.added-date {
  font-size: 12px;
  color: #9ca3af;
}

.order-btn {
  background-color: #374151;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-btn:hover {
  background-color: #1f2937;
}

/* 빈 상태 섹션 */
.empty-section {
  background-color: white;
  margin: 1rem 0;
  padding: 4rem 0;
}

.empty-content {
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.empty-description {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 2rem;
}

.explore-btn {
  display: inline-block;
  background-color: #374151;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.explore-btn:hover {
  background-color: #1f2937;
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 768px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .stores-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-info {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>