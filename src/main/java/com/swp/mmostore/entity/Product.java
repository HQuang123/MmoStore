package com.swp.mmostore.entity;

import com.swp.mmostore.converter.FieldsConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer productId;

    @Column(name = "Title", length = 255)
//    @Pattern(regexp = "^[\\p{L}\\p{M}\\d\\s,]+$\n", message="Tên tiếng việt chứa ký tự lạ")
    private String title;

    @Column(name = "Fields", columnDefinition = "JSON")
    @Convert(converter = FieldsConverter.class)
    private Map<String, Object> fields;

//    @Pattern(regexp = "^[\\p{L}\\p{M}\\d\\s,]+$\n", message="Mô tả chứa ký tự lạ")
    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Price", precision = 15, scale = 2)
    @Min(value = 1000, message = "Giá tối thiểu 1000 đồng")
    private BigDecimal price;

    @Column(name = "Quantity")
    @Min(value = 0, message = "Số lượng tối thiểu là 0 đơn vị")
    private Integer quantity;

    @Column(name = "isDeleted")
    private boolean isDeleted = false;

    @Column(name = "CreateAt", updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy;

    @Column(name = "ProductImageUrl", insertable = false)
    private String productImageUrl;

    @ManyToOne
    @JoinColumn(name = "ShopID")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Discount> discounts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Rating> ratings = new ArrayList<>();

    public void addDiscount(Discount discount) {
        discounts.add(discount);
        discount.setProduct(this);
    }

    public void removeDiscount(Discount discount) {
        discounts.remove(discount);
        discount.setProduct(null);
    }

    public Double getAvgRating() {
        double result = 0;
        for (Rating rating : ratings) {
            result += rating.getRatingPoint();
        }
        return result / ratings.size();
    }

    public Product(String title, String description, BigDecimal price, Integer quantity, Shop shop, Category category) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.shop = shop;
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", quantity=" + quantity;
    }
}
