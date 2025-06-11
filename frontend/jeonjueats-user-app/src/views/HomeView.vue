<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { getStores, type Store, type StorePageResponse } from '../api/stores'
import { getCategories, type Category } from '../api/categories'
import { useRouter } from 'vue-router'

// 상태 관리
const stores = ref<Store[]>([])
const isLoading = ref(false)
const isLoadingMore = ref(false)
const hasMoreStores = ref(true)
const currentPage = ref(0)
const error = ref<string | null>(null)
const selectedCategoryId = ref<number | null>(null)



// 라우터
const router = useRouter()

// 카테고리 데이터 (프론트엔드에서 하드코딩 - 백엔드 DataInitializer와 일치)
const categories = ref([
  { id: 1, name: '치킨', emoji: '🍗' },
  { id: 2, name: '피자', emoji: '🍕' },
  { id: 3, name: '중식', emoji: '🥢' },
  { id: 4, name: '한식', emoji: '🍚' },
  { id: 5, name: '일식', emoji: '🍣' },
  { id: 6, name: '양식', emoji: '🍝' },
  { id: 7, name: '분식', emoji: '🍢' },
  { id: 8, name: '카페·디저트', emoji: '☕' },
  { id: 9, name: '족발·보쌈', emoji: '🥩' },
  { id: 10, name: '야식', emoji: '🌙' }
])

// Intersection Observer 정리를 위한 변수
let observer: IntersectionObserver | null = null

// 무한 스크롤 구현
const loadMoreObserver = ref<HTMLElement | null>(null)

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await loadStores(true)
  setupInfiniteScroll()
})

// 가게 목록 로드
const loadStores = async (reset: boolean = true) => {
  try {
    console.log('가게 목록 로드 시작:', { reset, categoryId: selectedCategoryId.value, page: currentPage.value })
    
    if (reset) {
      isLoading.value = true
      currentPage.value = 0
      stores.value = []
    } else {
      isLoadingMore.value = true
    }

    const storeData: StorePageResponse = await getStores({
      categoryId: selectedCategoryId.value !== null ? selectedCategoryId.value : undefined,
      page: currentPage.value,
      size: 20,
      sort: 'id,desc'
    })

    console.log('가게 목록 응답:', storeData)

    if (reset) {
      stores.value = storeData.content
    } else {
      stores.value.push(...storeData.content)
    }

    hasMoreStores.value = !storeData.last
    currentPage.value = storeData.number

    console.log('가게 목록 로드 완료:', { storeCount: stores.value.length, hasMore: hasMoreStores.value })

  } catch (err) {
    console.error('가게 목록 로드 실패:', err)
    error.value = '가게 목록을 불러오는데 실패했습니다.'
  } finally {
    isLoading.value = false
    isLoadingMore.value = false
  }
}

// 컴포넌트 언마운트 시 observer 정리 (setup 단계에서 등록)
onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})

// Intersection Observer 설정
const setupInfiniteScroll = () => {
  nextTick(() => {
    if (!loadMoreObserver.value) return
    
    observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !isLoadingMore.value && hasMoreStores.value) {
        loadMoreStores()
      }
    }, {
      threshold: 0.1,
      rootMargin: '50px'
    })
    
    observer.observe(loadMoreObserver.value)
  })
}

// 추가 가게 로드
const loadMoreStores = async () => {
  if (isLoadingMore.value || !hasMoreStores.value) return
  
  isLoadingMore.value = true
  try {
    const nextPage = currentPage.value + 1
    const response = await getStores({
      categoryId: selectedCategoryId.value !== null ? selectedCategoryId.value : undefined,
      page: nextPage,
      size: 10,
      sort: 'id,desc'
    })
    
    // 새 데이터를 기존 목록에 추가
    stores.value = [...stores.value, ...response.content]
    currentPage.value = nextPage
    hasMoreStores.value = !response.last
    
  } catch (err) {
    console.error('추가 가게 로딩 실패:', err)
    error.value = '추가 가게를 불러오는데 실패했습니다'
  } finally {
    isLoadingMore.value = false
  }
}

// 검색 페이지로 이동
const goToSearch = () => {
  router.push('/search')
}

// 카테고리 클릭 핸들러
const handleCategoryClick = async (categoryId: number) => {
  console.log('카테고리 클릭:', { categoryId, current: selectedCategoryId.value })
  selectedCategoryId.value = categoryId === selectedCategoryId.value ? null : categoryId
  console.log('카테고리 선택:', selectedCategoryId.value)
  await loadStores(true) // 카테고리 변경 시 첫 페이지부터 다시 로드
}

// 가게 카드 클릭 핸들러
const handleStoreClick = (storeId: number) => {
  console.log('가게 클릭:', storeId)
  router.push(`/stores/${storeId}`)
}

// 가격 포맷팅
const formatPrice = (price: number): string => {
  return price.toLocaleString() + '원'
}

// 배달비 표시
const getDeliveryFeeText = (fee: number): string => {
  return fee === 0 ? '무료배달' : `배달비 ${formatPrice(fee)}`
}
</script>

<template>
  <!-- 쿠팡이츠 홈페이지 - 진짜 100% 전체 화면 -->
  <div class="home">
    
    <!-- 검색 섹션 -->
    <section class="search-section">
      <div class="search-container">
        <div class="search-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
          <div 
            @click="goToSearch"
            class="search-input-clickable"
          >
            맛집과 음식을 검색해보세요!
          </div>
        </div>
      </div>
    </section>

    <!-- 카테고리 섹션 -->
    <section class="categories-section">
      <div class="section-container">
        <div class="categories-grid">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-item"
            :class="{ 'selected': selectedCategoryId === category.id }"
            @click="handleCategoryClick(category.id)"
          >
            <div class="category-circle">
              <span class="category-emoji">{{ category.emoji }}</span>
            </div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>
      </div>
    </section>


    <!-- 선택된 카테고리 헤더 -->
    <section v-if="selectedCategoryId" class="category-header-section">
      <div class="section-container">
        <div class="category-header">
          <button @click="selectedCategoryId = null; loadStores(true)" class="back-button">
            ← 전체보기
          </button>
          <h1 class="category-title">
            {{ categories.find(c => c.id === selectedCategoryId)?.emoji }}
            {{ categories.find(c => c.id === selectedCategoryId)?.name }}
          </h1>
        </div>
      </div>
    </section>

    <!-- 가게 목록 섹션 -->
    <section class="stores-section">
      <div class="section-container">
        <div v-if="!selectedCategoryId" class="section-header">
          <h2 class="section-title">골라먹는 맛집</h2>
          <span class="section-subtitle">오늘은 뭐 먹을까?</span>
        </div>
        
        <!-- 카테고리별 헤더 -->
        <div v-else class="section-header">
          <span class="section-subtitle">선택한 카테고리의 맛집들이에요</span>
        </div>
        
        <!-- 에러 메시지 -->
        <div v-if="error" class="error-container">
          <p class="error-message">{{ error }}</p>
          <button @click="loadStores()" class="retry-button">다시 시도</button>
        </div>
        
        <!-- 가게 목록 -->
        <div v-else class="stores-grid">
          <div 
            v-for="store in stores" 
            :key="store.storeId"
            class="store-card"
            @click="handleStoreClick(store.storeId)"
          >
            <!-- 가게 이미지 영역 -->
            <div class="store-image-container">
              <img 
                v-if="store.storeImageUrl" 
                :src="store.storeImageUrl" 
                :alt="store.name"
                class="store-image"
              >
              <div v-else class="store-image-placeholder">
                <span class="placeholder-emoji">{{ categories.find(c => c.id === store.categoryId)?.emoji || '🏪' }}</span>
              </div>
              
              <!-- 운영 상태 뱃지 -->
              <div class="operation-badge" :class="store.status.toLowerCase()">
                {{ store.status === 'OPEN' ? '영업중' : '영업종료' }}
              </div>
              
              <!-- 배달 정보 오버레이 -->
              <div class="delivery-overlay">
                <span class="delivery-fee-badge">{{ getDeliveryFeeText(store.deliveryFee) }}</span>
              </div>
            </div>
            
            <!-- 가게 정보 -->
            <div class="store-content">
              <div class="store-header">
                <h4 class="store-name">{{ store.name }}</h4>
              </div>
              
              <p class="store-description">{{ store.description }}</p>
              
              <div class="store-footer">
                <span class="category-tag">{{ categories.find(c => c.id === store.categoryId)?.name || '기타' }}</span>
                <span class="min-order">최소주문 {{ formatPrice(store.minOrderAmount) }}</span>
              </div>
            </div>
          </div>
        </div>

      <!-- 빈 상태 (가게가 없을 때) -->
      <div v-if="!isLoading && stores.length === 0" class="empty-state">
        <div class="empty-icon">🏪</div>
        <h3 class="empty-title">
          {{ selectedCategoryId ? '해당 카테고리의 가게가 없어요' : '가게를 찾을 수 없어요' }}
        </h3>
        <p class="empty-description">
          {{ selectedCategoryId ? '다른 카테고리를 선택해보세요' : '잠시 후 다시 시도해주세요' }}
        </p>
        <button v-if="selectedCategoryId" @click="selectedCategoryId = null; loadStores(true)" class="retry-button">
          전체 가게 보기
        </button>
      </div>

      <!-- 무한 스크롤 트리거 영역 -->
        <div ref="loadMoreObserver" class="load-more-trigger">
          <div v-if="isLoadingMore" class="loading-more">
            <div class="loading-spinner"></div>
            <span>더 많은 가게를 불러오는 중...</span>
          </div>
          <div v-else-if="!hasMoreStores && stores.length > 0" class="no-more-stores">
            모든 가게를 확인했습니다
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
/* 홈 컨테이너 - 진짜 100% 화면 */
.home {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 공통 섹션 컨테이너 - 100% 너비, 좌우 균형감 */
.section-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 4rem;
}

/* 검색 섹션 */
.search-section {
  width: 100%;
  background-color: white;
  padding: 2rem 0;
  border-bottom: 1px solid #f1f3f4;
}

.search-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 4rem;
  display: flex;
  justify-content: center;
}

.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 800px;
}

.search-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #9ca3af;
  z-index: 1;
}

.search-input-clickable {
  width: 100%;
  height: 52px;
  padding: 0 20px 0 50px;
  border: 1px solid #e5e7eb;
  border-radius: 26px;
  background-color: #f9fafb;
  font-size: 16px;
  font-weight: 400;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
}

.search-input-clickable:hover {
  border-color: #d1d5db;
  background-color: #f3f4f6;
}


/* 카테고리 섹션 */
.categories-section {
  width: 100%;
  background-color: white;
  padding: 2.5rem 0;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  justify-items: center;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
  width: 100%;
}

.category-item:hover {
  transform: translateY(-2px);
}

.category-item.selected .category-circle {
  background-color: #007aff;
  border-color: #007aff;
}

.category-item.selected .category-name {
  color: #007aff;
  font-weight: 600;
}

.category-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.2s ease;
}

.category-item:hover .category-circle {
  background-color: #eeeeee;
}

.category-emoji {
  font-size: 24px;
}

.category-name {
  font-size: 12px;
  font-weight: 500;
  color: #666666;
  text-align: center;
  transition: all 0.2s ease;
}

/* 배너 섹션 */
.banner-section {
  width: 100%;
  background-color: #f8f9fa;
  padding: 1.5rem 0;
}

.promo-banner {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  border-radius: 12px;
  padding: 1.5rem 2rem;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.2);
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.banner-text {
  color: white;
}

.banner-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.banner-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

.banner-icon {
  font-size: 36px;
}

/* 가게 목록 섹션 */
.stores-section {
  width: 100%;
  background-color: white;
  padding: 2.5rem 0;
}

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.section-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.stores-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.store-card {
  background-color: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.store-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 이미지 영역 */
.store-image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f8f9fa;
}

.store-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.store-card:hover .store-image {
  transform: scale(1.05);
}

.store-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
}

.placeholder-emoji {
  font-size: 64px;
  opacity: 0.6;
}

/* 운영 상태 뱃지 */
.operation-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.operation-badge.open {
  background-color: rgba(255, 255, 255, 0.95);
  color: #059669;
  border: 1px solid rgba(5, 150, 105, 0.2);
}

.operation-badge.closed {
  background-color: rgba(255, 255, 255, 0.95);
  color: #dc2626;
  border: 1px solid rgba(220, 38, 38, 0.2);
}

/* 배달 정보 오버레이 */
.delivery-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.4), transparent);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.delivery-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  font-size: 13px;
  font-weight: 500;
}

.icon-clock {
  width: 16px;
  height: 16px;
}

.delivery-fee-badge {
  padding: 4px 10px;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* 가게 정보 콘텐츠 */
.store-content {
  padding: 16px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.store-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  flex: 1;
  margin-right: 8px;
}

.rating-container {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.star {
  font-size: 14px;
}

.rating {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.review-count {
  font-size: 12px;
  color: #6b7280;
}

.store-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.store-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-tag {
  padding: 4px 10px;
  background-color: #f3f4f6;
  color: #4b5563;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.min-order {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

/* 로딩 및 에러 상태 */
.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 2rem;
}

.error-message {
  color: #dc2626;
  font-size: 14px;
  margin-bottom: 1rem;
}

.retry-button {
  padding: 8px 16px;
  background-color: #374151;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background-color: #1f2937;
}

.end-message {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  font-size: 14px;
}

/* 카테고리 헤더 섹션 */
.category-header-section {
  width: 100%;
  background-color: white;
  border-bottom: 1px solid #e9ecef;
  padding: 1rem 0;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.back-button {
  background: none;
  border: none;
  font-size: 16px;
  color: #6b7280;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.back-button:hover {
  background-color: #f3f4f6;
}

.category-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}


/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 1rem;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 1.5rem;
}

/* 반응형 */
@media (max-width: 1200px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .search-container {
    padding: 0 2rem;
  }
  
  .stores-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 768px) {
  .section-container {
    padding: 0 1.5rem;
  }
  
  .search-container {
    padding: 0 1.5rem;
  }
  
  .categories-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 0.8rem;
  }
  
  .category-circle {
    width: 54px;
    height: 54px;
  }
  
  .category-emoji {
    font-size: 22px;
  }
  
  .stores-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }
  
  .promo-banner {
    padding: 1.25rem 1.5rem;
  }
  
  .banner-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .banner-icon {
    font-size: 28px;
  }

}

@media (max-width: 480px) {
  .categories-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 0.75rem;
  }
  
  .category-circle {
    width: 48px;
    height: 48px;
  }
  
  .category-emoji {
    font-size: 20px;
  }
  
  .category-name {
    font-size: 12px;
  }
  
  .stores-grid {
    grid-template-columns: 1fr;
  }
}
</style>
