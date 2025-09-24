package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payouts")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Payout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PayoutID")
    private Integer payoutId;

    @Column(name = "Amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "TransactionFee", precision = 15, scale = 2)
    private BigDecimal transactionFee;

    @Column(name = "CreateAt", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "CreateBy")
    private User createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "UpdateBy")
    private User updateBy;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "ShopID", nullable = false)
    private Shop shop;


}
