package com.swp.mmostore.dto;

import com.swp.mmostore.entity.ActionType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionEvent {
    private Integer transactionId; //depositId, withdrawalId or orderId base on ActionType
    private Integer userId;
    private BigDecimal amount;
    private String bank;
    private String account;
    private ActionType type;
    private LocalDateTime createdAt;
    private String status;
}
