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

    @Column(name = "DiscountPercent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "IsDeleted", insertable = false)
    private Boolean isDeleted;

    @Column(name = "CreateAt", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private Product product;

    public Discount(BigDecimal discountPercent, LocalDate startDate, LocalDate endDate, Product product) {
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.product = product;
    }
}
