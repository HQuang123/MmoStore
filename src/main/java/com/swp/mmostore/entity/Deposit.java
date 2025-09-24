package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Deposit")
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "PaymentMethod", length = 100)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private DepositStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ActionType")
    private ActionType actionType;

    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;

    @Column(name = "CreateAt", updatable = false)
    private LocalDateTime createAt;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    @Column(name = "CreateBy")
    private int createBy;

    @Column(name = "UpdateBy")
    private int updateBy;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;
}
