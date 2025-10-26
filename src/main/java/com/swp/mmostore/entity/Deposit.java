package com.swp.mmostore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Column(name = "Amount", precision = 15, scale = 2)
    @Min(value = 5000, message = "Nạp tối thiểu 5000 đồng")
    private BigDecimal amount;

    @Column(name = "PaymentMethod", length = 100)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private DepositStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ActionType")
    private ActionType actionType;

    @ManyToOne
    @JoinColumn(name = "OrderID") // Tạo cột OrderID trong Deposit
    private Order order;

    @Column(name = "IsDeleted", insertable = false)
    private Boolean isDeleted;

    @Column(name = "CreateAt", updatable = false, insertable = false)
    private LocalDateTime createAt;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;


}
