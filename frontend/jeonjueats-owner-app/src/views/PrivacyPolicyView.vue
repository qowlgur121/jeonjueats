<template>
  <div class="privacy-policy">
    <div class="header">
      <button @click="$router.go(-1)" class="back-button">
        <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">개인정보 처리방침</h1>
    </div>

    <div class="content">
      <div class="section">
        <h2>1. 개인정보의 처리목적</h2>
        <p>전주이츠(이하 "회사")는 다음의 목적을 위하여 개인정보를 처리합니다.</p>
        <ul>
          <li>회원 가입 및 관리</li>
          <li>서비스 제공 및 계약의 이행</li>
          <li>주문 및 배송 서비스</li>
          <li>고객 상담 및 불만 처리</li>
          <li>서비스 개선 및 맞춤형 서비스 제공</li>
        </ul>
      </div>

      <div class="section">
        <h2>2. 개인정보의 처리 및 보유기간</h2>
        <p>회사는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터 개인정보를 수집 시에 동의받은 개인정보 보유·이용기간 내에서 개인정보를 처리·보유합니다.</p>
        <ul>
          <li>회원정보: 회원 탈퇴 시까지</li>
          <li>주문정보: 주문 완료 후 5년</li>
          <li>결제정보: 결제 완료 후 5년</li>
        </ul>
      </div>

      <div class="section">
        <h2>3. 처리하는 개인정보의 항목</h2>
        <p>회사는 다음의 개인정보 항목을 처리하고 있습니다.</p>
        <ul>
          <li>필수항목: 이메일, 비밀번호, 닉네임</li>
          <li>선택항목: 마케팅 정보 수신 동의</li>
          <li>자동생성정보: 접속 IP, 쿠키, 서비스 이용기록</li>
        </ul>
      </div>

      <div class="section">
        <h2>4. 개인정보의 제3자 제공</h2>
        <p>회사는 정보주체의 개인정보를 원칙적으로 외부에 제공하지 않습니다. 다만, 아래의 경우에는 예외로 합니다.</p>
        <ul>
          <li>정보주체가 사전에 동의한 경우</li>
          <li>법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우</li>
        </ul>
      </div>

      <div class="section">
        <h2>5. 개인정보 처리의 위탁</h2>
        <p>회사는 원활한 개인정보 업무처리를 위하여 다음과 같이 개인정보 처리업무를 위탁하고 있습니다.</p>
        <ul>
          <li>결제처리: 가상 결제 시스템</li>
          <li>서버 호스팅: 클라우드 서비스 제공업체</li>
        </ul>
      </div>

      <div class="section">
        <h2>6. 정보주체의 권리·의무 및 행사방법</h2>
        <p>정보주체는 회사에 대해 언제든지 다음 각 호의 개인정보 보호 관련 권리를 행사할 수 있습니다.</p>
        <ul>
          <li>개인정보 처리정지 요구권</li>
          <li>개인정보 열람요구권</li>
          <li>개인정보 정정·삭제요구권</li>
          <li>개인정보 처리정지 요구권</li>
        </ul>
      </div>

      <div class="section">
        <h2>7. 개인정보의 안전성 확보조치</h2>
        <p>회사는 개인정보의 안전성 확보를 위해 다음과 같은 조치를 취하고 있습니다.</p>
        <ul>
          <li>관리적 조치: 개인정보 취급직원의 최소화 및 교육</li>
          <li>기술적 조치: 개인정보처리시스템 등의 접근권한 관리, 접근통제시스템 설치, 고유식별정보 등의 암호화, 보안프로그램 설치</li>
          <li>물리적 조치: 전산실, 자료보관실 등의 접근통제</li>
        </ul>
      </div>

      <div class="section">
        <h2>8. 개인정보보호책임자</h2>
        <div class="contact-info">
          <p><strong>개인정보보호책임자</strong></p>
            <li>이메일: qowlgur121@gmail.com</li>
            <li>전화: 010-3449-9493</li>
        </div>
      </div>

      <div class="section">
        <h2>9. 개인정보 처리방침 변경</h2>
        <p>이 개인정보처리방침은 시행일로부터 적용되며, 법령 및 방침에 따른 변경내용의 추가, 삭제 및 정정이 있는 경우에는 변경사항의 시행 7일 전부터 공지사항을 통하여 고지할 것입니다.</p>
      </div>

      <div class="effective-date">
        <p><strong>시행일자: 2025년 6월 10일</strong></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 뒤로가기 기능은 template에서 처리
</script>

<style scoped>
.privacy-policy {
  min-height: 100vh;
  background-color: #ffffff;
}

.header {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  background-color: #ffffff;
  z-index: 10;
}

.back-button {
  background: none;
  border: none;
  padding: 8px;
  margin-right: 12px;
  cursor: pointer;
  color: #374151;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.content {
  padding: 24px 16px;
  line-height: 1.6;
}

.section {
  margin-bottom: 32px;
}

.section h2 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.section p {
  color: #374151;
  margin-bottom: 8px;
}

.section ul {
  color: #374151;
  padding-left: 20px;
  margin-bottom: 8px;
}

.section li {
  margin-bottom: 4px;
}

.contact-info {
  background-color: #f9fafb;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.contact-info p {
  margin-bottom: 4px;
}

.effective-date {
  text-align: center;
  padding: 16px;
  background-color: #f3f4f6;
  border-radius: 8px;
  margin-top: 24px;
}

.effective-date p {
  color: #374151;
  margin: 0;
}
</style>