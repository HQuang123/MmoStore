package com.swp.mmostore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Rating")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "RatingPoint")
    @Min(value = 1, message = "Điểm dánh giá tối thiểu 1")
    @Max(value = 5, message = "Điểm đánh giá tôi thiểu 5")
    private Integer ratingPoint; // value between 1 and 5

    @Column(name = "Feedback", columnDefinition = "TEXT")
    @Pattern(regexp = "^[\\p{L}\\p{M}\\d\\s,]+$\n", message = "Mô tả chứa ký tự lạ")
    private String feedback;

    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;

    @Column(name = "CreateAt", updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ProductID")
    private Product product;
}
