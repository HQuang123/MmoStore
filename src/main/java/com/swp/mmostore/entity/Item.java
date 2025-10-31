package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Items")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer itemId;

    @Column(name = "Value")
    private String value;

    @Column(name = "IsSold")
    private Boolean isSold;

    @Column(name = "isDeleted", insertable = false)
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

    @ManyToOne
    @JoinColumn(name = "OrderID")
    private Order order;
}
