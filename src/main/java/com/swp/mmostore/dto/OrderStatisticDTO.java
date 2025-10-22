package com.swp.mmostore.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;

public record OrderStatisticDTO(
        Integer orderId,
        String productName,
        LocalDateTime createAt,
        String status,
        BigDecimal totalPrice,
        String paymentMethod
) {}
