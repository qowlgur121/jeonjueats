<template>
  <div class="terms-of-service">
    <div class="header">
      <button @click="goBack" class="back-button">
        <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
      <h1 class="title">이용약관</h1>
    </div>

    <div class="content">
      <div class="section">
        <h2>제1조 (목적)</h2>
        <p>이 약관은 전주이츠(이하 "회사")가 제공하는 배달 중개 서비스(이하 "서비스")의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.</p>
      </div>

      <div class="section">
        <h2>제2조 (정의)</h2>
        <p>이 약관에서 사용하는 용어의 정의는 다음과 같습니다.</p>
        <ul>
          <li>"서비스"란 회사가 제공하는 배달 중개 플랫폼을 의미합니다.</li>
          <li>"이용자"란 회사의 서비스에 접속하여 이 약관에 따라 회사가 제공하는 서비스를 받는 회원 및 비회원을 의미합니다.</li>
          <li>"회원"란 회사에 개인정보를 제공하여 회원등록을 한 자로서, 회사의 정보를 지속적으로 제공받으며, 회사가 제공하는 서비스를 계속적으로 이용할 수 있는 자를 의미합니다.</li>
          <li>"가맹점"란 회사와 계약을 체결하고 서비스를 통하여 음식을 판매하는 사업자를 의미합니다.</li>
        </ul>
      </div>

      <div class="section">
        <h2>제3조 (약관의 효력 및 변경)</h2>
        <p>① 이 약관은 서비스를 이용하고자 하는 모든 이용자에 대하여 그 효력을 발생합니다.</p>
        <p>② 회사는 필요하다고 인정되는 경우 이 약관을 변경할 수 있으며, 회사가 약관을 변경할 경우에는 적용일자 및 변경사유를 명시하여 현행약관과 함께 서비스 초기화면에 그 적용일자 7일 이전부터 적용일자 전일까지 공지합니다.</p>
      </div>

      <div class="section">
        <h2>제4조 (서비스의 제공)</h2>
        <p>① 회사는 다음과 같은 서비스를 제공합니다.</p>
        <ul>
          <li>음식점 정보 제공 서비스</li>
          <li>음식 주문 중개 서비스</li>
          <li>결제 대행 서비스</li>
          <li>리뷰 및 평점 서비스</li>
          <li>기타 회사가 정하는 서비스</li>
        </ul>
        <p>② 회사는 서비스의 일부 또는 전부를 회사가 정한 필요에 의해 변경할 수 있으며, 변경된 서비스의 내용 및 제공일자를 서비스 화면에 게시하거나 기타의 방법으로 이용자에게 통지할 수 있습니다.</p>
      </div>

      <div class="section">
        <h2>제5조 (회원가입)</h2>
        <p>① 이용자는 회사가 정한 가입 양식에 따라 회원정보를 기입한 후 이 약관에 동의한다는 의사표시를 함으로서 회원가입을 신청합니다.</p>
        <p>② 회사는 제1항과 같이 회원으로 가입할 것을 신청한 이용자 중 다음 각 호에 해당하지 않는 한 회원으로 등록합니다.</p>
        <ul>
          <li>가입신청자가 이 약관에 의하여 이전에 회원자격을 상실한 적이 있는 경우</li>
          <li>등록 내용에 허위, 기재누락, 오기가 있는 경우</li>
          <li>기타 회원으로 등록하는 것이 회사의 기술상 현저히 지장이 있다고 판단되는 경우</li>
        </ul>
      </div>

      <div class="section">
        <h2>제6조 (회원정보의 변경)</h2>
        <p>① 회원은 개인정보관리화면을 통하여 언제든지 본인의 개인정보를 열람하고 수정할 수 있습니다.</p>
        <p>② 회원은 회원가입 시 기재한 사항이 변경되었을 경우 온라인으로 수정을 하거나 전자우편 기타 방법으로 회사에 그 변경사항을 알려야 합니다.</p>
      </div>

      <div class="section">
        <h2>제7조 (개인정보보호 의무)</h2>
        <p>회사는 관련법령이 정하는 바에 따라서 회원 등록정보를 포함한 회원의 개인정보를 보호하기 위해 노력합니다. 회원의 개인정보보호에 관해서는 관련법령 및 회사가 정하는 개인정보처리방침에 정한 바에 의합니다.</p>
      </div>

      <div class="section">
        <h2>제8조 (주문 및 계약의 성립)</h2>
        <p>① 이용자는 서비스 상에서 다음 각 호의 방법에 의하여 주문을 신청하며, 회사는 이용자가 주문신청을 함에 있어서 다음의 각 내용을 알기 쉽게 제공하여야 합니다.</p>
        <ul>
          <li>상품의 검색 및 선택</li>
          <li>받는 사람의 성명, 주소, 전화번호 등의 입력</li>
          <li>약관내용, 청약철회권이 제한되는 서비스, 배송료 등의 비용부담과 관련한 내용에 대한 확인</li>
          <li>이 약관에 동의하고 위 사항을 확인하거나 거부하는 표시</li>
        </ul>
      </div>

      <div class="section">
        <h2>제9조 (결제방법)</h2>
        <p>이용자는 서비스에서 구매한 상품에 대한 대금지급을 다음 각 호의 방법 중 가용한 방법으로 할 수 있습니다. 단, 회사는 이용자의 지급방법에 대하여 상품 등의 대금에 어떠한 명목의 수수료도 추가하여 징수할 수 없습니다.</p>
        <ul>
          <li>신용카드 결제</li>
          <li>계좌이체</li>
          <li>전자화폐를 이용한 결제</li>
          <li>기타 전자적 지급방법에 의한 대금지급</li>
        </ul>
      </div>

      <div class="section">
        <h2>제10조 (수수료)</h2>
        <p>회사는 서비스 제공의 대가로 가맹점으로부터 수수료를 받을 수 있으며, 수수료의 구체적인 내용은 별도의 계약으로 정합니다.</p>
      </div>

      <div class="section">
        <h2>제11조 (회원탈퇴 및 자격 상실)</h2>
        <p>① 회원은 회사에 언제든지 탈퇴를 요청할 수 있으며 회사는 즉시 회원탈퇴를 처리합니다.</p>
        <p>② 회원이 다음 각 호의 사유에 해당하는 경우, 회사는 회원자격을 제한 및 정지시킬 수 있습니다.</p>
        <ul>
          <li>가입 신청 시에 허위 내용을 등록한 경우</li>
          <li>다른 사람의 서비스 이용을 방해하거나 그 정보를 도용하는 등 전자상거래 질서를 위협하는 경우</li>
          <li>서비스를 이용하여 법령 또는 이 약관이 금지하거나 공서양속에 반하는 행위를 하는 경우</li>
        </ul>
      </div>

      <div class="section">
        <h2>제12조 (손해배상)</h2>
        <p>① 회사는 무료로 제공되는 서비스와 관련하여 회원에게 어떠한 손해가 발생하더라도 동 손해가 회사의 고의 또는 중대한 과실에 의한 경우를 제외하고 이에 대하여 책임을 부담하지 아니합니다.</p>
        <p>② 회사가 관계법령, 이 약관에서 정한 이행의무 등을 위반하여 회원에게 손해를 끼친 경우, 그 손해를 배상할 책임이 있습니다.</p>
      </div>

      <div class="section">
        <h2>제13조 (면책조항)</h2>
        <p>① 회사는 천재지변 또는 이에 준하는 불가항력으로 인하여 서비스를 제공할 수 없는 경우에는 서비스 제공에 관한 책임이 면제됩니다.</p>
        <p>② 회사는 회원의 귀책사유로 인한 서비스 이용의 장애에 대하여는 책임을 지지 않습니다.</p>
      </div>

      <div class="section">
        <h2>제14조 (분쟁해결)</h2>
        <p>① 회사는 이용자가 제기하는 정당한 의견이나 불만을 반영하고 그 피해를 보상처리하기 위하여 피해보상처리기구를 설치·운영합니다.</p>
        <p>② 이 약관에 관한 분쟁은 대한민국 법을 준거법으로 하며, 분쟁 발생시 회사의 본사 소재지를 관할하는 법원을 합의관할로 합니다.</p>
      </div>

      <div class="effective-date">
        <p><strong>부칙</strong></p>
        <p>이 약관은 2024년 6월 10일부터 적용됩니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const goBack = () => {
  // 항상 브라우저 히스토리로 뒤로가기 (가장 자연스러운 방법)
  router.go(-1)
}
</script>

<style scoped>
.terms-of-service {
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
  margin-bottom: 4px;
}
</style>