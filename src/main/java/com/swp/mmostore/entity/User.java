package com.swp.mmostore.entity;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private int userId;

    @Column(name = "Username", nullable = false, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Role", nullable = false, length = 255)
    private String userRole; // Note: ENUM might need a custom converter, but String works for now

    @Column(name = "Balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", columnDefinition = "ENUM('Active','Inactive')")
    private UserStatus userStatus; // Note: ENUM might need a custom converter

    @Column(name = "CreateAt")
    private LocalDateTime createdAt;

    @Column(name = "CreateBy")
    private int createBy;

    @Column(name = "UpdateAt")
    private LocalDateTime updateAt; //

    @Column(name = "UpdateBy")
    private int updateBy; // FIX: The DB column is INT, so this must be Integer

    // Note: The isDeleted column name matches the Java field, no @Column annotation is needed here
    // as it follows the camelCase/camelCase convention.
    @Column(name = "IsDeleted")
    private boolean isDeleted;

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
    public User(int userId, String username, String password,  String email, String userRole) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.password = password;
    }

}