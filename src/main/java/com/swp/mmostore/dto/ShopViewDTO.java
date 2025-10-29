package com.swp.mmostore.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopViewDTO {
    private Integer shopId;
    private String name;
    private String description;
    private String shopImageUrl;
    private String ownerUsername;
    private String ownerEmail;
    private int productCount;
    private Double avgRating;
    private LocalDateTime createAt;
    private LocalDateTime lastUpdated;

    public ShopViewDTO(Integer shopId, String name, String description,
                       String shopImageUrl, String ownerUsername, String ownerEmail,
                       long productCount, Double avgRating,
                       LocalDateTime createAt, LocalDateTime lastUpdated) {
        this.shopId = shopId;
        this.name = name;
        this.description = description;
        this.shopImageUrl = shopImageUrl;
        this.ownerUsername = ownerUsername;
        this.ownerEmail = ownerEmail;
        this.productCount = (int) productCount;
        this.avgRating = avgRating;
        this.createAt = createAt;
        this.lastUpdated = lastUpdated;
    }

    public boolean hasImage() {
        return this.shopImageUrl != null && !this.shopImageUrl.isEmpty();
    }
}
