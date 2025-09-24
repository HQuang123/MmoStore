package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "Discount")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DiscountPercent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "CreateAt", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy")
    private int createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy")
    private int updateBy;

    @ManyToOne
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    public Discount(BigDecimal discountPercent, LocalDate startDate, LocalDate endDate, Product product) {
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.product = product;
    }
}
