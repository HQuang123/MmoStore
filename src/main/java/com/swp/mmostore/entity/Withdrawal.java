package com.swp.mmostore.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Withdrawal")
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Amount")
    @Min(value = 5000)
    private BigDecimal amount;

    // --- Bank Details transformed to use the Bank enum ---
    @Enumerated(EnumType.STRING) // Stores the enum name (e.g., "VIETINBANK", "VIETCOMBANK")
    @Column(name = "Bank") // You might want to rename the column in the DB, e.g., 'Bank'
    private Bank bank;

    // --- Removed old bankName and bankCode fields ---
    @Column(name = "BankAccount")
    private String bankAccount;

    @Column(name = "AccountHolder")
//    @Pattern(regexp = "^[\\p{L}\\p{M}\\d\\s,]+$\n", message = "Tên tài khoản chứa ký tự lạ")
    private String accountHolder;

    @Column(name = "status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Pending'")
    private String status = "Pending"; // Pending, Approved, Rejected

    @Column(name = "CreateAt", insertable = false)
    private LocalDateTime createAt;

//    @Column(name = "CreateBy", insertable = false)
//    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @ManyToOne
    @JoinColumn(name = "CreateBy")
    private User user;

}