package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ShopFee")
public class ShopFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    // Loại phí (đăng ký, duy trì, mở rộng...)
    @Enumerated(EnumType.STRING)
    @Column(name = "FeeType", nullable = false, length = 50)
    private FeeType feeType;

    // Số tiền thu
    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;


    // Trạng thái thanh toán (nếu bạn trừ tiền ngay, luôn là PAID)
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private FeeStatus status = FeeStatus.PAID;

    // Ngày tạo bản ghi (insertable = false => DB tự động sinh CURRENT_TIMESTAMP)
    @Column(name = "CreateAt", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy")
    private Integer createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy")
    private Integer updateBy;


    // Enum loại phí
    public enum FeeType {
        REGISTRATION, // Phí đăng ký mở shop
        MAINTENANCE,  // Phí duy trì hằng tháng/năm
        OTHER         // Các loại khác
    }

    // Enum trạng thái
    public enum FeeStatus {
        PAID,
        FAILED
    }
}
