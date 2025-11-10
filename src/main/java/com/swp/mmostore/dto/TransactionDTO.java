package com.swp.mmostore.dto;

import com.swp.mmostore.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionDTO(String id, TransactionType type, BigDecimal amount, String status, LocalDateTime createAt,
                             String detail) {
}
