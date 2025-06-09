<script setup lang="ts">
import { ref } from 'vue'

// 카테고리 데이터 (백엔드 DataInitializer와 일치)
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

// 인기 메뉴 데이터
const popularMenus = ref([
  {
    id: 1,
    name: '아미정식2',
    price: '9,900원',
    restaurant: '아미정식',
    rating: 4.9,
    reviewCount: 1234,
    image: '🍚',
    rank: 1,
    badge: '인기'
  },
  {
    id: 2,
    name: '짜장면',
    price: '7,000원',
    restaurant: '홍콩반점',
    rating: 4.8,
    reviewCount: 856,
    image: '🍜',
    rank: 2,
    badge: 'HOT'
  },
  {
    id: 3,
    name: '치킨버거',
    price: '5,500원',
    restaurant: 'KFC',
    rating: 4.7,
    reviewCount: 623,
    image: '🍔',
    rank: 3,
    badge: '신메뉴'
  },
  {
    id: 4,
    name: '피자 세트',
    price: '25,000원',
    restaurant: '도미노피자',
    rating: 4.6,
    reviewCount: 412,
    image: '🍕',
    rank: 4,
    badge: '할인'
  }
])

// 검색 기능
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('검색:', searchQuery.value)
    // 실제 검색 로직 구현
  }
}
</script>

<template>
  <!-- 쿠팡이츠 홈페이지 - 진짜 100% 전체 화면 -->
  <div class="home">
    
    <!-- 검색 섹션 - 좌우 균형감 있게 조정 -->
    <section class="search-section">
      <div class="search-container">
        <div class="search-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
          <input 
            v-model="searchQuery"
            @keyup.enter="handleSearch"
            type="text" 
            placeholder="맛집과 음식을 검색해보세요!"
            class="search-input"
          >
        </div>
      </div>
    </section>

    <!-- 카테고리 섹션 - 심플한 디자인 -->
    <section class="categories-section">
      <div class="section-container">
        <div class="categories-grid">
          <div 
            v-for="category in categories" 
            :key="category.id"
            class="category-item"
          >
            <div class="category-circle">
              <span class="category-emoji">{{ category.emoji }}</span>
            </div>
            <span class="category-name">{{ category.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 배너 섹션 -->
    <section class="banner-section">
      <div class="section-container">
        <div class="promo-banner">
          <div class="banner-content">
            <div class="banner-text">
              <h3 class="banner-title">첫 주문 3,000원 할인!</h3>
              <p class="banner-subtitle">지금 주문하고 혜택 받아보세요</p>
            </div>
            <div class="banner-icon">🎁</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 인기 메뉴 섹션 -->
    <section class="popular-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">우리 동네 인기 메뉴</h2>
          <span class="section-subtitle">지금 많이 주문하는 메뉴예요</span>
        </div>
        
        <div class="popular-grid">
          <div 
            v-for="menu in popularMenus" 
            :key="menu.id"
            class="menu-card"
          >
            <div class="card-header">
              <div class="rank-number">{{ menu.rank }}</div>
              <div class="menu-badge" :class="`badge-${menu.rank}`">{{ menu.badge }}</div>
            </div>
            <div class="menu-image">{{ menu.image }}</div>
            <div class="menu-details">
              <h4 class="menu-name">{{ menu.name }}</h4>
              <p class="menu-restaurant">{{ menu.restaurant }}</p>
              <div class="menu-stats">
                <div class="rating-info">
                  <span class="star">⭐</span>
                  <span class="rating">{{ menu.rating }}</span>
                  <span class="review-count">({{ menu.reviewCount.toLocaleString() }})</span>
                </div>
                <div class="price">{{ menu.price }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 추가 섹션 -->
    <section class="extra-section">
      <div class="section-container">
        <div class="extra-cards">
          <div class="extra-card new-restaurant">
            <div class="card-icon">🏪</div>
            <h3>새로운 맛집</h3>
            <p>전주의 숨겨진 맛집을 발견해보세요</p>
          </div>
          <div class="extra-card discount-event">
            <div class="card-icon">🎉</div>
            <h3>할인 이벤트</h3>
            <p>지금 진행중인 특별 할인 혜택</p>
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

/* 검색 섹션 - 좌우 균형감 있게 조정 */
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

.search-input {
  width: 100%;
  height: 52px;
  padding: 0 20px 0 50px;
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

/* 카테고리 섹션 - 심플한 쿠팡이츠 스타일 */
.categories-section {
  width: 100%;
  background-color: white;
  padding: 2.5rem 0;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2rem;
  justify-items: center;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.category-item:hover {
  transform: translateY(-2px);
}

.category-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: all 0.2s ease;
}

.category-item:hover .category-circle {
  background-color: #f1f3f4;
  border-color: #dee2e6;
}

.category-emoji {
  font-size: 22px;
}

.category-name {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  text-align: center;
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

/* 인기 메뉴 섹션 */
.popular-section {
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

.popular-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.menu-card {
  background-color: white;
  border-radius: 12px;
  padding: 1.25rem;
  border: 1px solid #f3f4f6;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
}

.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border-color: #e5e7eb;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.rank-number {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #374151, #1f2937);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
}

.menu-badge {
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.badge-1 { background-color: #ef4444; }
.badge-2 { background-color: #f59e0b; }
.badge-3 { background-color: #10b981; }
.badge-4 { background-color: #8b5cf6; }

.menu-image {
  font-size: 40px;
  text-align: center;
  margin-bottom: 0.75rem;
}

.menu-details {
  text-align: center;
}

.menu-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.menu-restaurant {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.menu-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.review-count {
  font-size: 12px;
  color: #9ca3af;
}

.price {
  font-size: 15px;
  font-weight: 700;
  color: #374151;
}

/* 추가 섹션 */
.extra-section {
  width: 100%;
  background-color: #f8f9fa;
  padding: 2rem 0 3rem;
}

.extra-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.extra-card {
  background-color: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s ease;
  border: 1px solid #f3f4f6;
}

.extra-card:hover {
  transform: translateY(-2px);
}

.card-icon {
  font-size: 32px;
  margin-bottom: 1rem;
}

.extra-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.extra-card p {
  font-size: 14px;
  color: #6b7280;
}

/* 반응형 */
@media (max-width: 1200px) {
  .section-container {
    padding: 0 2rem;
  }
  
  .search-container {
    padding: 0 2rem;
  }
  
  .popular-grid {
    grid-template-columns: repeat(3, 1fr);
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
    gap: 1rem;
  }
  
  .category-circle {
    width: 48px;
    height: 48px;
  }
  
  .category-emoji {
    font-size: 18px;
  }
  
  .popular-grid {
    grid-template-columns: repeat(2, 1fr);
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
    width: 40px;
    height: 40px;
  }
  
  .category-emoji {
    font-size: 16px;
  }
  
  .category-name {
    font-size: 12px;
  }
  
  .popular-grid {
    grid-template-columns: 1fr;
  }
  
  .extra-cards {
    grid-template-columns: 1fr;
  }
}
</style>
