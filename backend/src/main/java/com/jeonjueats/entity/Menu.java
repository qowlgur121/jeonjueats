package com.jeonjueats.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 메뉴 엔티티 (MVP 버전)
 * MVP 범위: 기본 메뉴 정보, 가격, 판매 상태
 * 제외 기능: 옵션 관리, 영양정보, 알레르기 정보, 메뉴 리뷰
 */
@Entity
@Table(name = "menu", indexes = {
    @Index(name = "idx_menu_store", columnList = "store_id"),
    @Index(name = "idx_menu_name", columnList = "name")
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE menu SET is_deleted = true WHERE menu_id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    /**
     * MVP 핵심 필드들
     */
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuStatus status = MenuStatus.AVAILABLE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "menu_image_url", length = 255)
    private String menuImageUrl;

    @Column(name = "menu_category", length = 50)
    private String menuCategory;

    /*
     * MVP 이후 확장 예정 필드들 (현재 비활성화)
     * 
     * 1. 메뉴 이미지
     * @Column(name = "image_url", length = 255)
     * private String imageUrl;
     * 
     * 2. 메뉴 옵션 (사이즈, 추가 토핑 등)
     * @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
     * private List<MenuOption> options = new ArrayList<>();
     * 
     * 3. 영양 정보
     * @Column(name = "calories")
     * private Integer calories;
     * 
     * @Column(name = "protein", precision = 5, scale = 2)
     * private BigDecimal protein;
     * 
     * @Column(name = "carbs", precision = 5, scale = 2)
     * private BigDecimal carbs;
     * 
     * @Column(name = "fat", precision = 5, scale = 2)
     * private BigDecimal fat;
     * 
     * 4. 알레르기 정보
     * @Column(name = "allergen_info", length = 255)
     * private String allergenInfo;
     * 
     * 5. 인기도 및 추천
     * @Column(name = "is_recommended", nullable = false)
     * private Boolean isRecommended = false;
     * 
     * @Column(name = "order_count", nullable = false)
     * private Integer orderCount = 0;
     * 
     * 6. 할인 정보
     * @Column(name = "discount_price", precision = 10, scale = 0)
     * private BigDecimal discountPrice;
     * 
     * @Column(name = "discount_start_date")
     * private LocalDateTime discountStartDate;
     * 
     * @Column(name = "discount_end_date")
     * private LocalDateTime discountEndDate;
     * 
     * 7. 조리 시간
     * @Column(name = "cooking_time")
     * private Integer cookingTime; // 분 단위
     */

    /**
     * 생성자 (MVP 필수 필드)
     */
    public Menu(Long storeId, String name, String description, BigDecimal price) {
        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = MenuStatus.AVAILABLE;
        this.isDeleted = false;
    }

    /**
     * 편의 메서드들
     */
    public void makeAvailable() {
        this.status = MenuStatus.AVAILABLE;
    }

    public void makeSoldOut() {
        this.status = MenuStatus.SOLD_OUT;
    }

    public boolean isAvailable() {
        return this.status == MenuStatus.AVAILABLE && !this.isDeleted;
    }

    public boolean isSoldOut() {
        return this.status == MenuStatus.SOLD_OUT;
    }

    public void updatePrice(BigDecimal newPrice) {
        this.price = newPrice;
    }

    /**
     * 논리적 삭제 (soft delete)
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = MenuStatus.SOLD_OUT;
    }

    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.status = MenuStatus.AVAILABLE;
    }

    public void updateBasicInfo(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void updateMenuImage(String menuImageUrl) {
        this.menuImageUrl = menuImageUrl;
    }

    public void updateStatus(MenuStatus status) {
        this.status = status;
    }

    public void updateMenuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }
} 