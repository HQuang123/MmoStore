package com.swp.mmostore.dto;

import lombok.Getter;
import lombok.Setter;

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
        BigDecimal unitPrice,   // thêm
        BigDecimal totalPrice,
        Integer shopId,         //thêm
        String shopName,        // thêm
        List<Map<String, String>> values
) {}
