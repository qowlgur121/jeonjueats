<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="policy-page">
    <!-- 헤더 -->
    <header class="policy-header">
      <button @click="goBack" class="back-button">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
        </svg>
      </button>
      <h1 class="header-title">마케팅 정보 수신 동의</h1>
    </header>

    <div class="policy-content">
      <div class="policy-section">
        <h2>마케팅 정보 수신 동의 (선택)</h2>
        <p class="effective-date">시행일: 2025년 6월 14일</p>
        
        <div class="section">
          <h3>1. 수집하는 개인정보 항목</h3>
          <p>전주이츠는 다음과 같은 개인정보를 수집합니다:</p>
          <ul>
            <li>이메일 주소</li>
            <li>휴대폰 번호</li>
            <li>서비스 이용 기록</li>
            <li>주문 및 결제 내역</li>
          </ul>
        </div>

        <div class="section">
          <h3>2. 개인정보 수집 및 이용 목적</h3>
          <ul>
            <li>신메뉴, 이벤트, 프로모션 등 마케팅 정보 제공</li>
            <li>맞춤형 상품 및 서비스 추천</li>
            <li>고객 만족도 조사 및 서비스 개선</li>
            <li>각종 혜택 및 쿠폰 제공</li>
          </ul>
        </div>

        <div class="section">
          <h3>3. 개인정보 보유 및 이용 기간</h3>
          <p>
            수집된 개인정보는 동의 철회 시 또는 마케팅 목적 달성 시까지 보유 및 이용됩니다.
            다만, 관련 법령에 따라 보존해야 하는 경우에는 해당 기간 동안 보관합니다.
          </p>
        </div>

        <div class="section">
          <h3>4. 개인정보 제3자 제공</h3>
          <p>
            전주이츠는 마케팅 목적으로 수집한 개인정보를 제3자에게 제공하지 않습니다.
            다만, 다음의 경우에는 예외로 합니다:
          </p>
          <ul>
            <li>이용자가 사전에 동의한 경우</li>
            <li>법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우</li>
          </ul>
        </div>

        <div class="section">
          <h3>5. 마케팅 정보 수신 거부</h3>
          <p>
            이용자는 언제든지 마케팅 정보 수신을 거부할 수 있으며, 
            다음과 같은 방법으로 동의를 철회할 수 있습니다:
          </p>
          <ul>
            <li>전주이츠 앱 내 마이페이지 > 설정에서 수신 거부</li>
            <li>수신한 이메일 하단의 '수신거부' 링크 클릭</li>
            <li>고객센터를 통한 수신 거부 요청</li>
          </ul>
        </div>

        <div class="section">
          <h3>6. 문의처</h3>
          <p>
            마케팅 정보 수신과 관련하여 문의사항이 있으시면 
            아래의 연락처로 연락주시기 바랍니다:
          </p>
          <ul>
            <li>이메일: qowlgur121@gmail.com</li>
            <li>전화: 010-3449-9493</li>
          </ul>
        </div>

        <div class="notice">
          <p>
            <strong>※ 본 동의는 선택사항이며, 동의하지 않아도 전주이츠 서비스 이용에는 제한이 없습니다.</strong>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.policy-page {
  min-height: 100vh;
  background-color: white;
}

/* 헤더 */
.policy-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
}

.back-button {
  position: absolute;
  left: 16px;
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
  background-color: #f8f9fa;
}

.back-button svg {
  width: 24px;
  height: 24px;
  color: #374151;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

/* 컨텐츠 */
.policy-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px;
}

.policy-section h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.effective-date {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 32px;
}

.section {
  margin-bottom: 32px;
}

.section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.section p {
  color: #374151;
  line-height: 1.6;
  margin-bottom: 12px;
}

.section ul {
  padding-left: 20px;
  margin: 12px 0;
}

.section li {
  color: #374151;
  line-height: 1.6;
  margin-bottom: 8px;
}

.notice {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
  margin-top: 32px;
}

.notice p {
  color: #1f2937;
  margin: 0;
}

/* 반응형 */
@media (max-width: 768px) {
  .policy-content {
    padding: 16px;
  }
  
  .policy-section h2 {
    font-size: 20px;
  }
  
  .section h3 {
    font-size: 16px;
  }
}
</style>