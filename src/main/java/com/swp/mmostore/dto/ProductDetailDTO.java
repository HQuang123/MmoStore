package com.swp.mmostore.dto;

import java.math.BigDecimal;

public record ProductDetailDTO(Integer id, String title, String description, BigDecimal price, String productImageUrl, Integer shopId, Double avgRating, Integer numberOfRatings, Integer numberOfSoldItems, String category) {
}
