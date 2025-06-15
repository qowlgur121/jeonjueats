package com.jeonjueats.config;

import com.jeonjueats.entity.*;
import com.jeonjueats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 초기 데이터 설정
 * 애플리케이션 시작 시 기본 카테고리, 사용자, 가게, 메뉴 데이터를 자동으로 생성
 * 데이터가 이미 존재하는 경우 중복 생성을 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("초기 데이터 설정을 시작합니다...");
        
        // 카테고리 데이터 생성
        initializeCategories();
        
        // 사용자 데이터 생성
        initializeUsers();
        
        // 가게 데이터 생성
        initializeStores();
        
        // 메뉴 데이터 생성
        initializeMenus();
        
        log.info("초기 데이터 설정이 완료되었습니다.");
    }

    /**
     * 카테고리 초기 데이터 생성
     * 10개의 기본 카테고리를 생성
     */
    private void initializeCategories() {
        if (categoryRepository.count() > 0) {
            log.info("카테고리 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("카테고리 초기 데이터를 생성합니다...");

        String[] categoryNames = {
            "치킨", "피자", "중식", "한식", "일식",
            "양식", "분식", "카페·디저트", "족발·보쌈", "야식"
        };

        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category(categoryNames[i], i + 1);
            categoryRepository.save(category);
        }

        log.info("카테고리 {}개 생성 완료", categoryNames.length);
    }

    /**
     * 사용자 초기 데이터 생성
     * 일반 사용자 5명, 사장님 7명 생성
     */
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            log.info("사용자 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("사용자 초기 데이터를 생성합니다...");

        // 일반 사용자 5명 생성
        createUser("user1@example.com", "password", "김고객", UserRole.ROLE_USER, "063-1234-5678", "54901", "전북 전주시 완산구 태평로 123", "101호");
        createUser("user2@example.com", "password", "이고객", UserRole.ROLE_USER, "063-2345-6789", "55041", "전북 전주시 덕진구 건산로 456", "202호");
        createUser("user3@example.com", "password", "박고객", UserRole.ROLE_USER, "010-3456-7890", "54896", "전북 전주시 완산구 팔달로 789", "303호");
        createUser("user4@example.com", "password", "최고객", UserRole.ROLE_USER, "010-4567-8901", "55069", "전북 전주시 덕진구 백제대로 101", "404호");
        createUser("user5@example.com", "password", "정고객", UserRole.ROLE_USER, "010-5678-9012", "54933", "전북 전주시 완산구 전주천동로 112", "505호");

        // 사장님 7명 생성 (가게 3-4개씩 배분)
        createUser("owner1@example.com", "password", "김사장", UserRole.ROLE_OWNER, "063-1111-1111", "54968", "전북 전주시 완산구 고사동 11-1", "101호");
        createUser("owner2@example.com", "password", "이사장", UserRole.ROLE_OWNER, "063-2222-2222", "55077", "전북 전주시 덕진구 송천동 22-2", "202호");
        createUser("owner3@example.com", "password", "박사장", UserRole.ROLE_OWNER, "010-3333-3333", "54913", "전북 전주시 완산구 중앙동 33-3", "303호");
        createUser("owner4@example.com", "password", "최사장", UserRole.ROLE_OWNER, "010-4444-4444", "55002", "전북 전주시 덕진구 진북동 44-4", "404호");
        createUser("owner5@example.com", "password", "정사장", UserRole.ROLE_OWNER, "010-5555-5555", "54949", "전북 전주시 완산구 서노송동 55-5", "505호");
        createUser("owner6@example.com", "password", "장사장", UserRole.ROLE_OWNER, "010-6666-6666", "55120", "전북 전주시 덕진구 금암동 66-6", "606호");
        createUser("owner7@example.com", "password", "윤사장", UserRole.ROLE_OWNER, "010-7777-7777", "54870", "전북 전주시 완산구 효자동 77-7", "707호");

        log.info("사용자 12명 생성 완료 (고객 5명, 사장님 7명)");
    }

    /**
     * 사용자 생성 헬퍼 메서드
     */
    private void createUser(String email, String password, String nickname, UserRole role, 
                           String phoneNumber, String zipcode, String address1, String address2) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, nickname, role);
        
        // 전화번호와 주소 정보 설정
        user.updatePhoneNumber(phoneNumber);
        user.updateDefaultAddress(zipcode, address1, address2);
        
        userRepository.save(user);
    }

    /**
     * 가게 초기 데이터 생성
     * 25개의 다양한 가게 생성
     */
    private void initializeStores() {
        if (storeRepository.count() > 0) {
            log.info("가게 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("가게 초기 데이터를 생성합니다...");

        // 사장님 계정 조회 (7명)
        User owner1 = userRepository.findByEmail("owner1@example.com")
                .orElseThrow(() -> new RuntimeException("김사장 계정을 찾을 수 없습니다"));
        User owner2 = userRepository.findByEmail("owner2@example.com")
                .orElseThrow(() -> new RuntimeException("이사장 계정을 찾을 수 없습니다"));
        User owner3 = userRepository.findByEmail("owner3@example.com")
                .orElseThrow(() -> new RuntimeException("박사장 계정을 찾을 수 없습니다"));
        User owner4 = userRepository.findByEmail("owner4@example.com")
                .orElseThrow(() -> new RuntimeException("최사장 계정을 찾을 수 없습니다"));
        User owner5 = userRepository.findByEmail("owner5@example.com")
                .orElseThrow(() -> new RuntimeException("정사장 계정을 찾을 수 없습니다"));
        User owner6 = userRepository.findByEmail("owner6@example.com")
                .orElseThrow(() -> new RuntimeException("장사장 계정을 찾을 수 없습니다"));
        User owner7 = userRepository.findByEmail("owner7@example.com")
                .orElseThrow(() -> new RuntimeException("윤사장 계정을 찾을 수 없습니다"));

        // 카테고리 조회
        Category chickenCategory = categoryRepository.findByName("치킨")
                .orElseThrow(() -> new RuntimeException("치킨 카테고리를 찾을 수 없습니다"));
        Category pizzaCategory = categoryRepository.findByName("피자")
                .orElseThrow(() -> new RuntimeException("피자 카테고리를 찾을 수 없습니다"));
        Category koreanCategory = categoryRepository.findByName("한식")
                .orElseThrow(() -> new RuntimeException("한식 카테고리를 찾을 수 없습니다"));
        Category chineseCategory = categoryRepository.findByName("중식")
                .orElseThrow(() -> new RuntimeException("중식 카테고리를 찾을 수 없습니다"));
        Category japaneseCategory = categoryRepository.findByName("일식")
                .orElseThrow(() -> new RuntimeException("일식 카테고리를 찾을 수 없습니다"));
        Category westernCategory = categoryRepository.findByName("양식")
                .orElseThrow(() -> new RuntimeException("양식 카테고리를 찾을 수 없습니다"));
        Category snackCategory = categoryRepository.findByName("분식")
                .orElseThrow(() -> new RuntimeException("분식 카테고리를 찾을 수 없습니다"));
        Category cafeCategory = categoryRepository.findByName("카페·디저트")
                .orElseThrow(() -> new RuntimeException("카페·디저트 카테고리를 찾을 수 없습니다"));
        Category jokbalCategory = categoryRepository.findByName("족발·보쌈")
                .orElseThrow(() -> new RuntimeException("족발·보쌈 카테고리를 찾을 수 없습니다"));
        Category lateNightCategory = categoryRepository.findByName("야식")
                .orElseThrow(() -> new RuntimeException("야식 카테고리를 찾을 수 없습니다"));

        // 치킨 가게들 (4개) - owner1(김사장) 4개
        createStore("맛있는 치킨집", "신선한 재료로 만드는 바삭한 치킨", "54321", "전북 전주시 완산구 효자동", null, "063-222-3333",
                   "/api/images/chicken1.jpg", chickenCategory.getId(), 15000, 3000, "매일 10:00-22:00", StoreStatus.OPEN, owner1.getId());
        
        createStore("황금올리브치킨", "황금빛 바삭함의 완성", "54322", "전북 전주시 덕진구 금암동", null, "063-333-4444",
                   "/api/images/chicken2.jpg", chickenCategory.getId(), 16000, 2500, "매일 11:00-23:00", StoreStatus.OPEN, owner1.getId());
        
        createStore("BHC치킨 전주점", "뿌링클의 원조", "54323", "전북 전주시 완산구 중앙동", null, "063-444-5555",
                   "/api/images/chicken3.jpg", chickenCategory.getId(), 18000, 3000, "매일 12:00-24:00", StoreStatus.OPEN, owner1.getId());
        
        createStore("굽네치킨", "오븐에 구운 건강한 치킨", "54324", "전북 전주시 덕진구 덕진동", null, "063-555-6666",
                   "/api/images/chicken4.jpg", chickenCategory.getId(), 17000, 2000, "매일 11:00-23:00", StoreStatus.OPEN, owner1.getId());

        // 피자 가게들 (3개) - owner2(이사장) 3개
        createStore("전주 피자하우스", "수제 도우로 만드는 정통 이탈리안 피자", "54325", "전북 전주시 덕진구 금암동", null, "063-666-7777",
                   "/api/images/pizza1.jpg", pizzaCategory.getId(), 20000, 2000, "매일 10:00-22:00", StoreStatus.OPEN, owner2.getId());
        
        createStore("도미노피자", "30분 배달 약속", "54326", "전북 전주시 완산구 효자동", null, "063-777-8888",
                   "/api/images/pizza2.jpg", pizzaCategory.getId(), 15000, 1500, "매일 10:30-23:00", StoreStatus.OPEN, owner2.getId());
        
        createStore("피자스쿨", "학생들이 좋아하는 가성비 피자", "54327", "전북 전주시 덕진구 인후동", null, "063-888-9999",
                   "/api/images/pizza3.jpg", pizzaCategory.getId(), 12000, 2000, "매일 11:00-24:00", StoreStatus.OPEN, owner2.getId());

        // 중식 가게들 (3개) - owner3(박사장) 3개
        createStore("용궁반점", "정통 중화요리 전문점", "54328", "전북 전주시 덕진구 인후동", null, "063-999-0000",
                   "/api/images/chinese1.jpg", chineseCategory.getId(), 8000, 2500, "매일 10:00-21:00", StoreStatus.OPEN, owner3.getId());
        
        createStore("차이나타운", "깔끔한 중식당", "54329", "전북 전주시 완산구 중앙동", null, "063-111-2222",
                   "/api/images/chinese2.jpg", chineseCategory.getId(), 12000, 3000, "매일 11:00-21:30", StoreStatus.OPEN, owner3.getId());
        
        createStore("홍콩반점", "광동식 중화요리", "54330", "전북 전주시 덕진구 덕진동", null, "063-222-3333",
                   "/api/images/chinese3.jpg", chineseCategory.getId(), 15000, 2500, "매일 11:30-22:00", StoreStatus.OPEN, owner3.getId());

        // 한식 가게들 (4개) - owner4(최사장) 4개
        createStore("엄마손 김치찌개", "집에서 먹는 것 같은 푸근한 한식", "54331", "전북 전주시 완산구 서신동", null, "063-333-4444",
                   "/api/images/korean1.jpg", koreanCategory.getId(), 8000, 2000, "매일 06:00-22:00", StoreStatus.OPEN, owner4.getId());
        
        createStore("전주 비빔밥", "전주 대표 음식 비빔밥", "54332", "전북 전주시 완산구 풍남동", null, "063-444-5555",
                   "/api/images/korean2.jpg", koreanCategory.getId(), 10000, 1500, "매일 08:00-20:00", StoreStatus.OPEN, owner4.getId());
        
        createStore("황제갈비", "최고급 한우갈비", "54333", "전북 전주시 덕진구 덕진동", null, "063-555-6666",
                   "/api/images/korean3.jpg", koreanCategory.getId(), 35000, 5000, "매일 12:00-22:00", StoreStatus.OPEN, owner4.getId());
        
        createStore("한정식집", "정갈한 한정식", "54334", "전북 전주시 완산구 중앙동", null, "063-666-7777",
                   "/api/images/korean4.jpg", koreanCategory.getId(), 25000, 4000, "매일 11:00-21:00", StoreStatus.CLOSED, owner4.getId());

        // 일식 가게들 (2개) - owner5(정사장) 2개
        createStore("스시장인", "신선한 일본 정통 스시", "54335", "전북 전주시 완산구 효자동", null, "063-777-8888",
                   "/api/images/japanese1.jpg", japaneseCategory.getId(), 30000, 3000, "매일 11:30-21:30", StoreStatus.OPEN, owner5.getId());
        
        createStore("라멘하우스", "진한 돈코츠 라멘", "54336", "전북 전주시 덕진구 인후동", null, "063-888-9999",
                   "/api/images/japanese2.jpg", japaneseCategory.getId(), 12000, 2500, "매일 11:00-22:00", StoreStatus.OPEN, owner5.getId());

        // 양식 가게들 (3개) - owner5(정사장) 3개 (총 5개)
        createStore("파스타 킹", "정통 이탈리안 파스타", "54337", "전북 전주시 완산구 중앙동", null, "063-999-0000",
                   "/api/images/western1.jpg", westernCategory.getId(), 15000, 2000, "매일 11:00-21:00", StoreStatus.OPEN, owner5.getId());
        
        createStore("스테이크하우스", "프리미엄 스테이크 전문점", "54338", "전북 전주시 덕진구 금암동", null, "063-111-2222",
                   "/api/images/western2.jpg", westernCategory.getId(), 40000, 4000, "매일 17:00-23:00", StoreStatus.OPEN, owner5.getId());
        
        createStore("햄버거팩토리", "수제 패티 햄버거", "54339", "전북 전주시 완산구 삼천동", null, "063-222-3333",
                   "/api/images/western3.jpg", westernCategory.getId(), 8000, 2000, "매일 10:00-22:00", StoreStatus.OPEN, owner5.getId());

        // 분식 가게들 (3개) - owner6(장사장) 3개
        createStore("떡볶이명가", "쫄깃한 떡볶이와 순대", "54340", "전북 전주시 완산구 서신동", null, "063-333-4444",
                   "/api/images/snack1.jpg", snackCategory.getId(), 5000, 1000, "매일 10:00-22:00", StoreStatus.OPEN, owner6.getId());
        
        createStore("김밥천국 전주점", "24시간 든든한 한끼", "54341", "전북 전주시 덕진구 덕진동", null, "063-444-5555",
                   "/api/images/snack2.jpg", snackCategory.getId(), 3000, 1500, "24시간 운영", StoreStatus.OPEN, owner6.getId());
        
        createStore("호떡집", "달콤한 전주 호떡", "54342", "전북 전주시 완산구 풍남동", null, "063-555-6666",
                   "/api/images/snack3.jpg", snackCategory.getId(), 2000, 1000, "매일 10:00-21:00", StoreStatus.OPEN, owner6.getId());

        // 카페·디저트 (2개) - owner6(장사장) 2개 (총 5개)  
        createStore("스타벅스 전주효자점", "커피와 디저트의 완벽한 조화", "54343", "전북 전주시 완산구 효자동", null, "063-666-7777",
                   "/api/images/cafe1.jpg", cafeCategory.getId(), 5000, 1000, "매일 06:00-22:00", StoreStatus.OPEN, owner6.getId());
        
        createStore("투썸플레이스", "달콤한 디저트와 커피", "54344", "전북 전주시 덕진구 금암동", null, "063-777-8888",
                   "/api/images/cafe2.jpg", cafeCategory.getId(), 6000, 1500, "매일 07:00-23:00", StoreStatus.OPEN, owner6.getId());

        // 족발·보쌈 (2개) - owner7(윤사장) 2개
        createStore("원조할머니족발", "50년 전통의 족발 맛", "54345", "전북 전주시 완산구 중앙동", null, "063-888-9999",
                   "/api/images/jokbal1.jpg", jokbalCategory.getId(), 25000, 3000, "매일 15:00-24:00", StoreStatus.OPEN, owner7.getId());
        
        createStore("보쌈집", "부드러운 수육과 보쌈", "54346", "전북 전주시 덕진구 인후동", null, "063-999-0000",
                   "/api/images/jokbal2.jpg", jokbalCategory.getId(), 22000, 2500, "매일 16:00-02:00", StoreStatus.OPEN, owner7.getId());

        // 야식 가게들 (3개) - owner7(윤사장) 3개 (총 5개)
        createStore("야식왕", "늦은 밤 든든한 한끼", "54347", "전북 전주시 완산구 효자동", null, "063-111-2222",
                   "/api/images/latenight1.jpg", lateNightCategory.getId(), 8000, 2000, "매일 20:00-06:00", StoreStatus.OPEN, owner7.getId());
        
        createStore("치킨호프", "치킨과 맥주의 완벽한 조합", "54348", "전북 전주시 덕진구 덕진동", null, "063-222-3333",
                   "/api/images/latenight2.jpg", lateNightCategory.getId(), 15000, 2500, "매일 18:00-03:00", StoreStatus.OPEN, owner7.getId());
        
        createStore("포차", "전통 포장마차 야식", "54349", "전북 전주시 완산구 서신동", null, "063-333-4444",
                   "/api/images/latenight3.jpg", lateNightCategory.getId(), 12000, 2000, "매일 19:00-04:00", StoreStatus.OPEN, owner7.getId());

        log.info("가게 25개 생성 완료 (사장님 7명에게 균등 배분)");
    }


    /**
     * 가게 생성
     */
    private Store createStore(String name, String description, String zipcode, String address1, String address2, 
                             String phoneNumber, String storeImageUrl, Long categoryId, 
                             int minOrderAmount, int deliveryFee, String operatingHours, StoreStatus status, Long ownerId) {
        Store store = new Store(ownerId, categoryId, name, zipcode, address1, phoneNumber);
        store.setDescription(description);
        store.setAddress2(address2);
        store.setStoreImageUrl(storeImageUrl);
        store.setMinOrderAmount(BigDecimal.valueOf(minOrderAmount));
        store.setDeliveryFee(BigDecimal.valueOf(deliveryFee));
        store.setOperatingHours(operatingHours);
        store.setStatus(status);
        store.setIsDeleted(false);
        return storeRepository.save(store);
    }

    /**
     * 메뉴 초기 데이터 생성
     * 각 가게별로 카테고리에 맞는 메뉴 생성
     */
    private void initializeMenus() {
        if (menuRepository.count() > 0) {
            log.info("메뉴 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("메뉴 초기 데이터를 생성합니다...");

        // 모든 가게 조회해서 각 가게별로 메뉴 생성
        var allStores = storeRepository.findAll();
        int menuCount = 0;
        
        for (Store store : allStores) {
            Long categoryId = store.getCategoryId();
            
            // 치킨 카테고리 (1번)
            if (categoryId == 1) {
                createMenu(store.getId(), "후라이드 치킨", "바삭하고 담백한 후라이드 치킨", BigDecimal.valueOf(18000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/fried-chicken.jpg");
                createMenu(store.getId(), "양념 치킨", "달콤짭짤한 양념 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/yangnyeom-chicken.jpg");
                createMenu(store.getId(), "간장 치킨", "깔끔한 간장 소스 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/soy-chicken.jpg");
                createMenu(store.getId(), "치킨 버거", "수제 치킨 버거", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/chicken-burger.jpg");
                menuCount += 4;
            }
            // 피자 카테고리 (2번)
            else if (categoryId == 2) {
                createMenu(store.getId(), "마르게리타 피자", "토마토와 모짜렐라의 클래식 피자", BigDecimal.valueOf(22000), MenuStatus.AVAILABLE, "/api/images/menus/pizza/margherita.jpg");
                createMenu(store.getId(), "페퍼로니 피자", "매콤한 페퍼로니가 올라간 피자", BigDecimal.valueOf(25000), MenuStatus.AVAILABLE, "/api/images/menus/pizza/pepperoni.jpg");
                createMenu(store.getId(), "불고기 피자", "한국식 불고기 피자", BigDecimal.valueOf(27000), MenuStatus.AVAILABLE, "/api/images/menus/pizza/bulgogi-pizza.jpg");
                createMenu(store.getId(), "슈프림 피자", "다양한 토핑의 피자", BigDecimal.valueOf(30000), MenuStatus.AVAILABLE, "/api/images/menus/pizza/supreme-pizza.jpg");
                menuCount += 4;
            }
            // 중식 카테고리 (3번)
            else if (categoryId == 3) {
                createMenu(store.getId(), "짜장면", "정통 중화 짜장면", BigDecimal.valueOf(7000), MenuStatus.AVAILABLE, "/api/images/menus/chinese/jjajangmyeon.jpg");
                createMenu(store.getId(), "짬뽕", "매콤한 해물 짬뽕", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/chinese/jjamppong.jpg");
                createMenu(store.getId(), "탕수육", "바삭한 탕수육", BigDecimal.valueOf(20000), MenuStatus.AVAILABLE, "/api/images/menus/chinese/tangsuyuk.jpg");
                createMenu(store.getId(), "볶음밥", "계란 볶음밥", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/chinese/fried-rice.jpg");
                menuCount += 4;
            }
            // 한식 카테고리 (4번)
            else if (categoryId == 4) {
                createMenu(store.getId(), "김치찌개", "집에서 끓인 듯한 김치찌개", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/korean/kimchi-jjigae.jpg");
                createMenu(store.getId(), "된장찌개", "구수한 된장찌개", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/korean/doenjang-jjigae.jpg");
                createMenu(store.getId(), "불고기", "달콤한 불고기", BigDecimal.valueOf(15000), MenuStatus.AVAILABLE, "/api/images/menus/korean/bulgogi.jpg");
                createMenu(store.getId(), "비빔밥", "전주 특산 비빔밥", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE, "/api/images/menus/korean/bibimbap.jpg");
                menuCount += 4;
            }
            // 일식 카테고리 (5번)
            else if (categoryId == 5) {
                createMenu(store.getId(), "초밥 세트", "신선한 회로 만든 초밥", BigDecimal.valueOf(25000), MenuStatus.AVAILABLE, "/api/images/menus/japanese/sushi-set.jpg");
                createMenu(store.getId(), "라멘", "진한 국물의 라멘", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE, "/api/images/menus/japanese/ramen.jpg");
                createMenu(store.getId(), "우동", "쫄깃한 우동", BigDecimal.valueOf(9000), MenuStatus.AVAILABLE, "/api/images/menus/japanese/udon.jpg");
                createMenu(store.getId(), "덴뿌라", "바삭한 튀김", BigDecimal.valueOf(15000), MenuStatus.AVAILABLE, "/api/images/menus/japanese/tempura.jpg");
                menuCount += 4;
            }
            // 양식 카테고리 (6번)
            else if (categoryId == 6) {
                createMenu(store.getId(), "파스타", "정통 이탈리아 파스타", BigDecimal.valueOf(15000), MenuStatus.AVAILABLE, "/api/images/menus/western/pasta.jpg");
                createMenu(store.getId(), "스테이크", "프리미엄 등심 스테이크", BigDecimal.valueOf(35000), MenuStatus.AVAILABLE, "/api/images/menus/western/steak.jpg");
                createMenu(store.getId(), "햄버거", "수제 패티 햄버거", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE, "/api/images/menus/western/burger.jpg");
                createMenu(store.getId(), "샐러드", "신선한 야채 샐러드", BigDecimal.valueOf(9000), MenuStatus.AVAILABLE, "/api/images/menus/western/salad.jpg");
                menuCount += 4;
            }
            // 분식 카테고리 (7번)
            else if (categoryId == 7) {
                createMenu(store.getId(), "떡볶이", "쫄깃하고 매콤한 떡볶이", BigDecimal.valueOf(4000), MenuStatus.AVAILABLE, "/api/images/menus/snack/tteokbokki.jpg");
                createMenu(store.getId(), "순대", "따뜻한 순대", BigDecimal.valueOf(5000), MenuStatus.AVAILABLE, "/api/images/menus/snack/sundae.jpg");
                createMenu(store.getId(), "김밥", "맛있는 김밥", BigDecimal.valueOf(3000), MenuStatus.AVAILABLE, "/api/images/menus/snack/kimbap.jpg");
                createMenu(store.getId(), "라면", "얼큰한 라면", BigDecimal.valueOf(3500), MenuStatus.AVAILABLE, "/api/images/menus/snack/ramyeon.jpg");
                menuCount += 4;
            }
            // 카페·디저트 카테고리 (8번)
            else if (categoryId == 8) {
                createMenu(store.getId(), "아메리카노", "깔끔한 아메리카노", BigDecimal.valueOf(4000), MenuStatus.AVAILABLE, "/api/images/menus/cafe/americano.jpg");
                createMenu(store.getId(), "카페라떼", "부드러운 카페라떼", BigDecimal.valueOf(4500), MenuStatus.AVAILABLE, "/api/images/menus/cafe/latte.jpg");
                createMenu(store.getId(), "케이크", "달콤한 케이크", BigDecimal.valueOf(6000), MenuStatus.AVAILABLE, "/api/images/menus/cafe/cake.jpg");
                createMenu(store.getId(), "쿠키", "바삭한 쿠키", BigDecimal.valueOf(3000), MenuStatus.AVAILABLE, "/api/images/menus/cafe/cookie.jpg");
                menuCount += 4;
            }
            // 족발·보쌈 카테고리 (9번)
            else if (categoryId == 9) {
                createMenu(store.getId(), "족발", "쫄깃한 족발", BigDecimal.valueOf(25000), MenuStatus.AVAILABLE, "/api/images/menus/jokbal/jokbal.jpg");
                createMenu(store.getId(), "보쌈", "부드러운 보쌈", BigDecimal.valueOf(22000), MenuStatus.AVAILABLE, "/api/images/menus/jokbal/bossam.jpg");
                createMenu(store.getId(), "족발보쌈 세트", "족발과 보쌈 함께", BigDecimal.valueOf(35000), MenuStatus.AVAILABLE, "/api/images/menus/jokbal/jokbal-set.jpg");
                createMenu(store.getId(), "막국수", "시원한 막국수", BigDecimal.valueOf(7000), MenuStatus.AVAILABLE, "/api/images/menus/jokbal/makguksu.jpg");
                menuCount += 4;
            }
            // 야식 카테고리 (10번)
            else if (categoryId == 10) {
                // 치킨호프는 치킨 메뉴를 제공
                if (store.getName().contains("치킨호프")) {
                    createMenu(store.getId(), "후라이드 치킨", "바삭하고 담백한 후라이드 치킨", BigDecimal.valueOf(18000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/fried-chicken.jpg");
                    createMenu(store.getId(), "양념 치킨", "달콤짭짤한 양념 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/yangnyeom-chicken.jpg");
                    createMenu(store.getId(), "간장 치킨", "깔끔한 간장 소스 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/soy-chicken.jpg");
                    createMenu(store.getId(), "치킨 버거", "수제 치킨 버거", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE, "/api/images/menus/chicken/chicken-burger.jpg");
                    menuCount += 4;
                } else {
                    createMenu(store.getId(), "야식 치킨", "야식용 치킨", BigDecimal.valueOf(18000), MenuStatus.AVAILABLE, "/api/images/menus/latenight/night-chicken.jpg");
                    createMenu(store.getId(), "야식 피자", "야식용 피자", BigDecimal.valueOf(20000), MenuStatus.AVAILABLE, "/api/images/menus/latenight/night-pizza.jpg");
                    createMenu(store.getId(), "야식 족발", "야식용 족발", BigDecimal.valueOf(25000), MenuStatus.AVAILABLE, "/api/images/menus/latenight/night-jokbal.jpg");
                    createMenu(store.getId(), "야식 라면", "라면과 계란", BigDecimal.valueOf(5000), MenuStatus.AVAILABLE, "/api/images/menus/latenight/night-ramyeon.jpg");
                    menuCount += 4;
                }
            }
        }

        log.info("메뉴 {}개 생성 완료", menuCount);
    }

    /**
     * 메뉴 생성
     */
    private Menu createMenu(Long storeId, String name, String description, BigDecimal price, MenuStatus status, String menuImageUrl) {
        Menu menu = new Menu(storeId, name, description, price);
        menu.setMenuImageUrl(menuImageUrl);
        menu.setStatus(status);
        menu.setIsDeleted(false);
        return menuRepository.save(menu);
    }
} 