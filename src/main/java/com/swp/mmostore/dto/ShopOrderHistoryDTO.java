package com.swp.mmostore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopOrderHistoryDTO(
        Integer orderId,
        Integer productId,
        String productTitle,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice,
        LocalDateTime createAt,
        String status
) {}
