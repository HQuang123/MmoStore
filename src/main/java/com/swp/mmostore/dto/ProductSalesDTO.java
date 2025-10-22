package com.swp.mmostore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSalesDTO {
    private Integer productId;
    private String title;
    private BigDecimal price;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
