<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchStores, type Store, type StorePageResponse } from '../api/stores'

// 라우터
const router = useRouter()

// 검색 관련 데이터
const searchQuery = ref('')
const isSearching = ref(false)
const searchResults = ref<Store[]>([])
const hasSearched = ref(false)

// 최근 검색어 (로컬 스토리지에서 관리)
const recentSearches = ref<string[]>([])

// 카테고리 데이터 (가게 카드에서 사용)
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

// 컴포넌트 마운트 시 최근 검색어 로드
onMounted(() => {
  loadRecentSearches()
})

// 최근 검색어 로드
const loadRecentSearches = () => {
  try {
    const saved = localStorage.getItem('recent_searches')
    if (saved) {
      recentSearches.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('최근 검색어 로드 실패:', error)
    recentSearches.value = []
  }
}

// 최근 검색어 저장
const saveRecentSearch = (query: string) => {
  try {
    const trimmedQuery = query.trim()
    if (!trimmedQuery) return
    
    // 중복 제거 및 최신 검색어를 맨 앞에 배치
    const filtered = recentSearches.value.filter(item => item !== trimmedQuery)
    recentSearches.value = [trimmedQuery, ...filtered].slice(0, 10) // 최대 10개
    
    localStorage.setItem('recent_searches', JSON.stringify(recentSearches.value))
  } catch (error) {
    console.error('최근 검색어 저장 실패:', error)
  }
}

// 검색 기능
const handleSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) return
  
  try {
    isSearching.value = true
    hasSearched.value = true
    
    console.log('검색 시작:', query)
    
    // 최근 검색어에 추가
    saveRecentSearch(query)
    
    // 백엔드 API 호출
    const searchData: StorePageResponse = await searchStores(query, 0, 50)
    searchResults.value = searchData.content
    
    console.log('검색 결과:', searchResults.value.length, '개')
  } catch (error) {
    console.error('검색 실패:', error)
    searchResults.value = []
  } finally {
    isSearching.value = false
  }
}

// 최근 검색어 선택
const selectRecentSearch = (search: string) => {
  searchQuery.value = search
  handleSearch()
}

// 최근 검색어 전체 삭제
const clearRecentSearches = () => {
  recentSearches.value = []
  localStorage.removeItem('recent_searches')
}

// 검색 초기화
const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
  hasSearched.value = false
  isSearching.value = false
}

// 가게 카드 클릭
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

// 뒤로가기
const goBack = () => {
  router.back()
}
</script>

<template>
  <!-- 검색 페이지 -->
  <div class="search-page">
    
    <!-- 검색 입력 섹션 -->
    <section class="search-input-section">
      <div class="section-container">
        <div class="search-wrapper">
          <button @click="goBack" class="back-button">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
          </button>
          <div class="search-input-container">
            <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
            </svg>
            <input 
              v-model="searchQuery"
              @keyup.enter="handleSearch"
              type="text" 
              placeholder="음식점이나 음식을 검색해보세요"
              class="search-input"
              autofocus
            >
            <button 
              v-if="searchQuery"
              @click="searchQuery = ''"
              class="clear-btn"
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 검색 결과가 없을 때 -->
    <div v-if="!hasSearched" class="search-content">
      
      <!-- 최근 검색어 -->
      <section v-if="recentSearches.length" class="recent-section">
        <div class="section-container">
          <div class="section-header">
            <h3 class="section-title">최근 검색어</h3>
            <button @click="clearRecentSearches" class="clear-all-btn">
              전체삭제
            </button>
          </div>
          <div class="tag-list">
            <button 
              v-for="search in recentSearches"
              :key="search"
              @click="selectRecentSearch(search)"
              class="tag-item"
            >
              {{ search }}
            </button>
          </div>
        </div>
      </section>

      <!-- 최근 검색어가 없을 때 -->
      <section v-else class="empty-recent-section">
        <div class="section-container">
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h3 class="empty-title">검색어를 입력해주세요</h3>
            <p class="empty-description">맛집과 음식을 검색해보세요!</p>
          </div>
        </div>
      </section>
    </div>

    <!-- 검색 중 -->
    <div v-if="isSearching" class="searching-state">
      <div class="section-container">
        <div class="loading-spinner"></div>
        <p class="loading-text">검색 중...</p>
      </div>
    </div>

    <!-- 검색 결과 -->
    <div v-if="hasSearched && !isSearching" class="search-results">
      <div class="section-container">
        <div class="results-header">
          <h3 class="results-title">
            <span v-if="searchResults.length > 0">
              '{{ searchQuery }}'에 대한 검색결과 {{ searchResults.length }}개
            </span>
            <span v-else>
              '{{ searchQuery }}'에 대한 검색결과가 없습니다
            </span>
          </h3>
          <button @click="clearSearch" class="clear-search-btn">
            다시 검색
          </button>
        </div>
        
        <!-- 검색 결과 가게 목록 -->
        <div v-if="searchResults.length > 0" class="stores-grid">
          <div 
            v-for="store in searchResults" 
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
        
        <!-- 검색 결과 없음 -->
        <div v-else class="empty-search-results">
          <div class="empty-icon">💭</div>
          <h3 class="empty-title">검색 결과가 없어요</h3>
          <p class="empty-description">다른 키워드로 검색해보세요</p>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 검색 페이지 컨테이너 */
.search-page {
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

/* 검색 입력 섹션 */
.search-input-section {
  width: 100%;
  background-color: white;
  padding: 2rem 0;
  border-bottom: 1px solid #f1f3f4;
  position: sticky;
  top: 56px;
  z-index: 50;
}

.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-button {
  background: none;
  border: none;
  width: 24px;
  height: 24px;
  color: #374151;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
  flex-shrink: 0;
}

.back-button:hover {
  color: #1f2937;
}

.search-input-container {
  position: relative;
  flex: 1;
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

.search-input {
  width: 100%;
  height: 52px;
  padding: 0 50px 0 50px;
  border: 1px solid #e5e7eb;
  border-radius: 26px;
  background-color: #f9fafb;
  font-size: 16px;
  font-weight: 400;
  color: #374151;
  outline: none;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #374151;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(55, 65, 81, 0.1);
}

.search-input::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.clear-btn {
  position: absolute;
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.clear-btn:hover {
  color: #6b7280;
}

/* 검색 콘텐츠 */
.search-content {
  padding-top: 1rem;
}

/* 섹션 헤더 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.section-subtitle {
  font-size: 14px;
  color: #6b7280;
}

.clear-all-btn {
  background: none;
  border: none;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.clear-all-btn:hover {
  color: #374151;
  background-color: #f9fafb;
}

/* 최근 검색어 섹션 */
.recent-section {
  background-color: white;
  padding: 1.5rem 0;
  margin-bottom: 1rem;
}

/* 인기 검색어 섹션 */
.popular-section {
  background-color: white;
  padding: 1.5rem 0;
  margin-bottom: 1rem;
}

/* 태그 리스트 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.tag-item {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tag-item:hover {
  background-color: #f1f3f4;
  border-color: #dee2e6;
}

.popular-tag {
  background-color: #f0f9ff;
  border-color: #e0f2fe;
}

.popular-tag:hover {
  background-color: #e0f2fe;
  border-color: #bae6fd;
}

.tag-rank {
  background-color: #374151;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
}

/* 추천 매장 섹션 */
.recommended-section {
  background-color: white;
  padding: 1.5rem 0 2rem;
}

.store-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.store-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.store-card:hover {
  border-color: #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.store-image {
  font-size: 32px;
  flex-shrink: 0;
}

.store-details {
  flex: 1;
  min-width: 0;
}

.store-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.store-category {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.store-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 2px;
}

.star {
  font-size: 12px;
}

.rating {
  font-weight: 600;
  color: #1f2937;
}

.delivery-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.delivery-time {
  color: #6b7280;
}

.delivery-fee {
  color: #6b7280;
}

/* 검색 중 상태 */
.searching-state {
  padding: 3rem 0;
  text-align: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #374151;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #6b7280;
  font-size: 14px;
}

/* 빈 상태 (검색어 없음) */
.empty-recent-section {
  background-color: white;
  padding: 3rem 0;
}

.empty-state {
  text-align: center;
  padding: 2rem;
}

.empty-state .empty-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.empty-state .empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-state .empty-description {
  font-size: 14px;
  color: #6b7280;
}

/* 검색 결과 */
.search-results {
  padding: 1.5rem 0;
  background-color: white;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.results-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.clear-search-btn {
  background: none;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.clear-search-btn:hover {
  border-color: #d1d5db;
  color: #374151;
  background-color: #f9fafb;
}

/* 가게 그리드 */
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

/* 가게 이미지 */
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

/* 가게 콘텐츠 */
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

/* 검색 결과 없음 */
.empty-search-results {
  text-align: center;
  padding: 4rem 2rem;
}

.empty-search-results .empty-icon {
  font-size: 64px;
  margin-bottom: 1.5rem;
  opacity: 0.6;
}

.empty-search-results .empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.empty-search-results .empty-description {
  font-size: 14px;
  color: #6b7280;
}

/* 반응형 */
@media (max-width: 1200px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .store-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .section-container {
    padding: 0 1.5rem;
  }
  
  .search-input-section {
    top: 52px;
  }
  
  .tag-list {
    gap: 0.5rem;
  }
  
  .tag-item {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .store-card {
    padding: 0.75rem;
  }
  
  .store-image {
    font-size: 28px;
  }
}
</style> 