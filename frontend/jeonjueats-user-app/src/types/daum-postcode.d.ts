// 다음 우편번호 서비스 타입 정의
declare namespace daum {
  export class Postcode {
    constructor(options: PostcodeOptions);
    open(): void;
  }

  interface PostcodeOptions {
    oncomplete: (data: PostcodeData) => void;
    onclose?: () => void;
    width?: string | number;
    height?: string | number;
    animation?: boolean;
    hideMapBtn?: boolean;
    hideEngBtn?: boolean;
    shorthand?: boolean;
    theme?: PostcodeTheme;
  }

  interface PostcodeData {
    zonecode: string; // 우편번호
    address: string; // 기본 주소
    addressEnglish: string; // 영문 주소
    roadAddress: string; // 도로명 주소
    jibunAddress: string; // 지번 주소
    autoRoadAddress: string; // 자동 처리된 도로명 주소
    autoJibunAddress: string; // 자동 처리된 지번 주소
    buildingName: string; // 건물명
    buildingCode: string; // 건물 코드
    apartment: string; // 아파트 여부 (Y/N)
    userSelectedType: 'R' | 'J'; // 사용자가 선택한 주소 타입 (R: 도로명, J: 지번)
    sido: string; // 시/도
    sigungu: string; // 시/군/구
    bname: string; // 법정동명
    query: string; // 검색어
  }

  interface PostcodeTheme {
    bgColor?: string;
    searchBgColor?: string;
    contentBgColor?: string;
    pageBgColor?: string;
    textColor?: string;
    queryTextColor?: string;
    postcodeTextColor?: string;
    emphTextColor?: string;
    outlineColor?: string;
  }
}