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
     * 일반 사용자 3명, 사장님 2명 생성
     */
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            log.info("사용자 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("사용자 초기 데이터를 생성합니다...");

        // 일반 사용자 3명 생성
        createUser("user1@example.com", "password", "고객1", UserRole.ROLE_USER);
        createUser("user2@example.com", "password", "고객2", UserRole.ROLE_USER);
        createUser("user3@example.com", "password", "고객3", UserRole.ROLE_USER);

        // 사장님 2명 생성
        createUser("owner1@example.com", "password", "사장님1", UserRole.ROLE_OWNER);
        createUser("owner2@example.com", "password", "사장님2", UserRole.ROLE_OWNER);

        log.info("사용자 5명 생성 완료");
    }

    /**
     * 사용자 생성 헬퍼 메서드
     */
    private void createUser(String email, String password, String nickname, UserRole role) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword, nickname, role);
        userRepository.save(user);
    }

    /**
     * 가게 초기 데이터 생성
     * 각 사장님마다 2-3개의 가게 생성
     */
    private void initializeStores() {
        if (storeRepository.count() > 0) {
            log.info("가게 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("가게 초기 데이터를 생성합니다...");

        // 사장님 계정 조회
        User owner1 = userRepository.findByEmail("owner1@example.com")
                .orElseThrow(() -> new RuntimeException("사장님1 계정을 찾을 수 없습니다"));
        User owner2 = userRepository.findByEmail("owner2@example.com")
                .orElseThrow(() -> new RuntimeException("사장님2 계정을 찾을 수 없습니다"));

        // 카테고리 조회
        Category chickenCategory = categoryRepository.findByName("치킨")
                .orElseThrow(() -> new RuntimeException("치킨 카테고리를 찾을 수 없습니다"));
        Category pizzaCategory = categoryRepository.findByName("피자")
                .orElseThrow(() -> new RuntimeException("피자 카테고리를 찾을 수 없습니다"));
        Category koreanCategory = categoryRepository.findByName("한식")
                .orElseThrow(() -> new RuntimeException("한식 카테고리를 찾을 수 없습니다"));
        Category chineseCategory = categoryRepository.findByName("중식")
                .orElseThrow(() -> new RuntimeException("중식 카테고리를 찾을 수 없습니다"));

        // 사장님1의 가게 3개 생성
        createStore(owner1.getId(), chickenCategory.getId(), "맛있는 치킨집", 
                   "54321", "전북 전주시 완산구 효자동", "063-222-3333",
                   "신선한 재료로 만드는 맛있는 치킨", BigDecimal.valueOf(15000), BigDecimal.valueOf(3000),
                   StoreStatus.OPEN);

        createStore(owner1.getId(), pizzaCategory.getId(), "전주 피자하우스", 
                   "54322", "전북 전주시 덕진구 금암동", "063-333-4444",
                   "수제 도우로 만드는 정통 이탈리안 피자", BigDecimal.valueOf(20000), BigDecimal.valueOf(2000),
                   StoreStatus.OPEN);

        createStore(owner1.getId(), koreanCategory.getId(), "전통 한정식", 
                   "54323", "전북 전주시 완산구 중앙동", "063-444-5555",
                   "전주 전통 한정식 전문점", BigDecimal.valueOf(25000), BigDecimal.valueOf(4000),
                   StoreStatus.CLOSED);

        // 사장님2의 가게 2개 생성
        createStore(owner2.getId(), chineseCategory.getId(), "용궁반점", 
                   "54324", "전북 전주시 덕진구 인후동", "063-555-6666",
                   "정통 중화요리 전문점", BigDecimal.valueOf(18000), BigDecimal.valueOf(2500),
                   StoreStatus.OPEN);

        createStore(owner2.getId(), koreanCategory.getId(), "엄마손 김치찌개", 
                   "54325", "전북 전주시 완산구 서신동", "063-666-7777",
                   "집에서 먹는 것 같은 푸근한 한식", BigDecimal.valueOf(12000), BigDecimal.valueOf(2000),
                   StoreStatus.OPEN);

        log.info("가게 5개 생성 완료");
    }

    /**
     * 가게 생성 헬퍼 메서드
     */
    private void createStore(Long ownerId, Long categoryId, String name, String zipcode, 
                           String address1, String phoneNumber, String description,
                           BigDecimal minOrderAmount, BigDecimal deliveryFee, StoreStatus status) {
        Store store = new Store(ownerId, categoryId, name, zipcode, address1, phoneNumber);
        store.setDescription(description);
        store.setMinOrderAmount(minOrderAmount);
        store.setDeliveryFee(deliveryFee);
        store.setStatus(status);
        store.setOperatingHours("매일 10:00-22:00");
        storeRepository.save(store);
    }

    /**
     * 메뉴 초기 데이터 생성
     * 각 가게별로 3-5개의 메뉴 생성
     */
    private void initializeMenus() {
        if (menuRepository.count() > 0) {
            log.info("메뉴 데이터가 이미 존재합니다. 건너뛰기...");
            return;
        }

        log.info("메뉴 초기 데이터를 생성합니다...");

        // 모든 가게 조회 (가게명으로 직접 조회)
        Store chickenStore = storeRepository.findAll().stream()
                .filter(store -> store.getName().contains("맛있는 치킨집"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("맛있는 치킨집을 찾을 수 없습니다"));
        
        Store pizzaStore = storeRepository.findAll().stream()
                .filter(store -> store.getName().contains("전주 피자하우스"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("전주 피자하우스를 찾을 수 없습니다"));
        
        Store koreanStore1 = storeRepository.findAll().stream()
                .filter(store -> store.getName().contains("전통 한정식"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("전통 한정식을 찾을 수 없습니다"));
        
        Store chineseStore = storeRepository.findAll().stream()
                .filter(store -> store.getName().contains("용궁반점"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("용궁반점을 찾을 수 없습니다"));
        
        Store koreanStore2 = storeRepository.findAll().stream()
                .filter(store -> store.getName().contains("엄마손 김치찌개"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("엄마손 김치찌개를 찾을 수 없습니다"));

        // 맛있는 치킨집 메뉴
        createMenu(chickenStore.getId(), "후라이드 치킨", "바삭하고 담백한 후라이드 치킨", BigDecimal.valueOf(18000), MenuStatus.AVAILABLE);
        createMenu(chickenStore.getId(), "양념 치킨", "달콤짭짤한 양념 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE);
        createMenu(chickenStore.getId(), "간장 치킨", "깔끔한 간장 소스 치킨", BigDecimal.valueOf(19000), MenuStatus.AVAILABLE);
        createMenu(chickenStore.getId(), "치킨 버거", "수제 치킨 버거", BigDecimal.valueOf(8000), MenuStatus.SOLD_OUT);

        // 전주 피자하우스 메뉴
        createMenu(pizzaStore.getId(), "마르게리타 피자", "토마토와 모짜렐라의 클래식 피자", BigDecimal.valueOf(22000), MenuStatus.AVAILABLE);
        createMenu(pizzaStore.getId(), "페퍼로니 피자", "매콤한 페퍼로니가 올라간 피자", BigDecimal.valueOf(25000), MenuStatus.AVAILABLE);
        createMenu(pizzaStore.getId(), "불고기 피자", "한국식 불고기 피자", BigDecimal.valueOf(27000), MenuStatus.AVAILABLE);
        createMenu(pizzaStore.getId(), "시카고 딥디쉬", "두꺼운 도우의 시카고 스타일", BigDecimal.valueOf(30000), MenuStatus.AVAILABLE);
        createMenu(pizzaStore.getId(), "샐러드", "신선한 야채 샐러드", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE);

        // 전통 한정식 메뉴
        createMenu(koreanStore1.getId(), "한정식 A코스", "전통 한정식 기본코스", BigDecimal.valueOf(35000), MenuStatus.AVAILABLE);
        createMenu(koreanStore1.getId(), "한정식 B코스", "전통 한정식 프리미엄코스", BigDecimal.valueOf(45000), MenuStatus.AVAILABLE);
        createMenu(koreanStore1.getId(), "비빔밥", "전주 특산 비빔밥", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE);
        createMenu(koreanStore1.getId(), "갈비탕", "진한 갈비탕", BigDecimal.valueOf(15000), MenuStatus.SOLD_OUT);

        // 용궁반점 메뉴
        createMenu(chineseStore.getId(), "짜장면", "정통 중화 짜장면", BigDecimal.valueOf(7000), MenuStatus.AVAILABLE);
        createMenu(chineseStore.getId(), "짬뽕", "매콤한 해물 짬뽕", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE);
        createMenu(chineseStore.getId(), "탕수육", "바삭한 탕수육", BigDecimal.valueOf(20000), MenuStatus.AVAILABLE);
        createMenu(chineseStore.getId(), "볶음밥", "계란 볶음밥", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE);

        // 엄마손 김치찌개 메뉴
        createMenu(koreanStore2.getId(), "김치찌개", "집에서 끓인 듯한 김치찌개", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE);
        createMenu(koreanStore2.getId(), "된장찌개", "구수한 된장찌개", BigDecimal.valueOf(8000), MenuStatus.AVAILABLE);
        createMenu(koreanStore2.getId(), "제육볶음", "매콤한 제육볶음", BigDecimal.valueOf(12000), MenuStatus.AVAILABLE);
        createMenu(koreanStore2.getId(), "불고기", "달콤한 불고기", BigDecimal.valueOf(15000), MenuStatus.AVAILABLE);
        createMenu(koreanStore2.getId(), "계란말이", "푸짐한 계란말이", BigDecimal.valueOf(6000), MenuStatus.SOLD_OUT);

        log.info("메뉴 22개 생성 완료");
    }

    /**
     * 메뉴 생성 헬퍼 메서드
     */
    private void createMenu(Long storeId, String name, String description, BigDecimal price, MenuStatus status) {
        Menu menu = new Menu(storeId, name, description, price);
        menu.setStatus(status);
        menuRepository.save(menu);
    }
} 