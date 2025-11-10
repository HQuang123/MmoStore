package com.swp.mmostore.dto;

import java.math.BigDecimal;

public record ProductSummaryDTO(Integer id, String title, String description, BigDecimal price, String shopName, Double avgRating, String productImageUrl) {
}
