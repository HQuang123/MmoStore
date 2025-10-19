package com.swp.mmostore.entity;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "User") // The table name is also PascalCase in your schema
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="UserID")
    private Integer userId;

    @Column(name = "Name", length = 50)
    private String name;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Password", length = 255)
    private String password;

    @Column(name = "Role", length = 255, insertable = false)
    private String role; // Note: ENUM might need a custom converter, but String works for now

    @Column(name = "Balance", precision = 10, scale = 2, insertable = false)
    private BigDecimal balance;

    @Column(name = "Status", columnDefinition = "Boolean DEFAULT true", insertable = false)
    private Boolean status;

    @Column(name = "CreateAt", insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "CreateBy", insertable = false)
    private Integer createBy;

    @Column(name = "UpdateAt", insertable = false)
    private LocalDateTime updateAt; //

    @Column(name = "UpdateBy", insertable = false)
    private Integer updateBy; // FIX: The DB column is Integer, so this must be Integereger

    // Note: The isDeleted column name matches the Java field, no @Column annotation is needed here
    // as it follows the camelCase/camelCase convention.
    @Column(name = "IsDeleted", insertable = false)
    private Boolean isDeleted;

    @Column(name = "Provider")
    private String provider;

    @Column(name = "ProviderId")
    private String providerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Shop shop;

    public void addShop(Shop shop) {
        this.shop = shop;
        if (shop != null) {
            shop.setUser(this);
        }
    }

    public void removeShop() {
        if (this.shop != null) {
            this.shop.setUser(null);
            this.shop = null;
        }
    }

    @Column(name = "AccountStatusNonLocked", insertable = false)
    private Boolean accountStatusNonLocked;

    @Column(name = "AccountFailedAttemptCount", insertable = false)
    private Integer accountFailedAttempt;

    @Column(name = "AccountLockTime", insertable = false)
    private Date accountLockTime;

    @Column(name = "ResetTokens")
    private String resetToken;

    @Column(name = "ProfileImage")
    private String profileImage;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Deposit> deposits = new ArrayList<>();

    public void addDeposit(Deposit deposit) {
        deposits.add(deposit);
        deposit.setUser(this);
    }
    public void removeDeposit(Deposit deposit) {
        deposits.remove(deposit);
        deposit.setUser(null);
    }

    // Constructor is fine


    public User(String name, String email, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public User(String name, String email, String phoneNumber, String password, String provider, String providerId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
    }
}