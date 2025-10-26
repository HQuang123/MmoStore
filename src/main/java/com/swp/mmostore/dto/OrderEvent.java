package com.swp.mmostore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderEvent {
    private Integer orderId;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;
}
