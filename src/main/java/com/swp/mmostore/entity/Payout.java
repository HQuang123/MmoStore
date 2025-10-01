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

    @Column(name = "CreateAt",insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @Column(name = "isDeleted", insertable = false)
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "ShopID", nullable = false)
    private Shop shop;


}
