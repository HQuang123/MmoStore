package com.swp.mmostore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopStatisticDTO {
    private long totalOrders;
    private long totalProducts;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long completedOrders;
    private long canceledOrders;
}
