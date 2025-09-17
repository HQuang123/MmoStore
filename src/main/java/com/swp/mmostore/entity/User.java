package com.swp.mmostore.entity;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

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

    @Column(name = "UserRole", nullable = false, length = 255)
    private String userRole; // Note: ENUM might need a custom converter, but String works for now

    @Column(name = "Balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Active','Inactive')")
    private Status status; // Note: ENUM might need a custom converter

//    @Column(name = "CreatedAt")
//    private Instant createdAt;

    @Column(name = "CreatedBy")
    private Integer createdBy; // FIX: The DB column is INT, so this must be Integer

    @Column(name = "UpdatedAt")
    private Instant updatedAt; // FIX: This column exists in your DB, but not in your entity

    @Column(name = "UpdatedBy")
    private Integer updatedBy; // FIX: The DB column is INT, so this must be Integer

    // Note: The isDeleted column name matches the Java field, no @Column annotation is needed here
    // as it follows the camelCase/camelCase convention.
    private boolean isDeleted;

    @Column(name = "DeletedBy")
    private Integer deletedBy; // FIX: The DB column is INT, so this must be Integer


    // Constructor is fine
    public User(int userId, String username, String password,  String email, String userRole) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
        this.password = password;
    }

}