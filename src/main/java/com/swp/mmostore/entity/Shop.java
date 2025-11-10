package com.swp.mmostore.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="Shop")
@NoArgsConstructor
@AllArgsConstructor
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer shopId;

    @Column(name = "Name")
    @Length(max = 255, message = "Tên cửa hàng không được vượt quá 255 ký tự")
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    @Length(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    @Column(name = "ShopImageUrl")
    private String shopImageUrl;

    @Column(name = "isDeleted", insertable = false)
    private Boolean isDeleted = false;

    @Column(name = "CreateAt", insertable = false)
    private LocalDateTime createAt;

    @Column(name = "CreateBy")
    private Integer createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt;

    @Column(name = "UpdateBy")
    private Integer updateBy;

    @OneToOne
    @JoinColumn(name = "UserID")
    private User user;


    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();

//   @Column(name="ShopImageUrl")
//   private String shopImageUrl;

    public void addProduct(Product product) {
        products.add(product);
        product.setShop(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setShop(null);
    }

    public Shop(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }
    public Shop(String name, String description, User user, String shopImageUrl) {
        this.name = name;
        this.description = description;
        this.user = user;
        this.shopImageUrl = shopImageUrl;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "shopId=" + shopId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isDeleted=" + isDeleted +
                ", createBy=" + createBy +
                ", updateBy=" + updateBy +
                '}';
    }
}
