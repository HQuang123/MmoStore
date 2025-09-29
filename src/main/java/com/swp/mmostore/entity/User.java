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

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Role", nullable = false, length = 255)
    private String role; // Note: ENUM might need a custom converter, but String works for now

    @Column(name = "Balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "Status")
    private Boolean status;

    @Column(name = "CreateAt")
    private LocalDateTime createdAt;

    @Column(name = "CreateBy")
    private Integer createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt; //

    @Column(name = "UpdateBy")
    private Integer updateBy; // FIX: The DB column is Integer, so this must be Integereger

    // Note: The isDeleted column name matches the Java field, no @Column annotation is needed here
    // as it follows the camelCase/camelCase convention.
    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Shop> shops = new ArrayList<>();

    public void addShop(Shop shop) {
        shops.add(shop);
        shop.setUser(this);
    }

    public void removeShop(Shop shop){
        shops.remove(shop);
        shop.setUser(null);
    }

    @Column(name = "AccountStatusNonLocked")
    private Boolean accountStatusNonLocked;

    @Column(name = "AccountFailedAttemptCount")
    private Integer accountFailedAttempt;

    @Column(name = "AccountLockTime")
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
}