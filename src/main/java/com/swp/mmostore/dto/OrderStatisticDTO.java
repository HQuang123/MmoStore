package com.swp.mmostore.dto;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OrderStatisticDTO(
        Integer orderId,
        String productName,
        LocalDateTime createAt,
        int quantity,
        String status,
        BigDecimal totalPrice,
        List<Map<String, String>> values
) {}
