<script setup lang="ts">
import { ref } from 'vue'

const wishlistItems = ref([
  {
    id: 1,
    storeName: '전주비빔밥 본점',
    category: '한식',
    rating: 4.8,
    deliveryTime: '25-35분',
    deliveryFee: 2000,
    imageUrl: '🍚',
    description: '정통 전주비빔밥의 원조 맛집',
    addedDate: '2024-01-15'
  },
  {
    id: 2,
    storeName: '한옥마을 치킨',
    category: '치킨',
    rating: 4.7,
    deliveryTime: '20-30분',
    deliveryFee: 2500,
    imageUrl: '🍗',
    description: '바삭한 치킨의 명가',
    addedDate: '2024-01-12'
  },
  {
    id: 3,
    storeName: '전주 막걸리집',
    category: '주점',
    rating: 4.6,
    deliveryTime: '30-40분',
    deliveryFee: 3000,
    imageUrl: '🍶',
    description: '정통 전주 막걸리와 안주',
    addedDate: '2024-01-10'
  }
])

const removeFromWishlist = (storeId: number) => {
  wishlistItems.value = wishlistItems.value.filter(item => item.id !== storeId)
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
      
      <!-- 찜 목록이 있을 때 -->
      <section v-if="wishlistItems.length > 0" class="stores-section">
        <div class="section-container">
          
          <!-- 통계 정보 -->
          <div class="stats-info">
            <h2 class="stats-title">
              총 <span class="stats-number">{{ wishlistItems.length }}개</span> 매장
            </h2>
            <select class="sort-select">
              <option>최신순</option>
              <option>이름순</option>
              <option>평점순</option>
            </select>
          </div>
          
          <!-- 찜한 매장 목록 -->
          <div class="stores-grid">
            <div 
              v-for="item in wishlistItems" 
              :key="item.id"
              class="store-card"
            >
              <!-- 매장 이미지 영역 -->
              <div class="store-image-area">
                <div class="store-image">{{ item.imageUrl }}</div>
                <button 
                  @click="removeFromWishlist(item.id)"
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
                  <h3 class="store-name">{{ item.storeName }}</h3>
                  <span class="store-category">{{ item.category }}</span>
                </div>
                
                <p class="store-description">{{ item.description }}</p>
                
                <div class="store-stats">
                  <div class="rating-info">
                    <span class="star">⭐</span>
                    <span class="rating">{{ item.rating }}</span>
                  </div>
                  <div class="delivery-info">
                    <span class="delivery-time">{{ item.deliveryTime }}</span>
                    <span class="delivery-fee">
                      {{ item.deliveryFee === 0 ? '무료배달' : `배달비 ${item.deliveryFee.toLocaleString()}원` }}
                    </span>
                  </div>
                </div>
                
                <div class="store-actions">
                  <span class="added-date">{{ item.addedDate }} 즐겨찾기</span>
                  <button class="order-btn">
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

.store-category {
  padding: 4px 8px;
  background-color: #f3f4f6;
  color: #6b7280;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
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

.delivery-info {
  display: flex;
  flex-direction: column;
  text-align: right;
  gap: 2px;
}

.delivery-time {
  color: #6b7280;
}

.delivery-fee {
  color: #374151;
  font-weight: 500;
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