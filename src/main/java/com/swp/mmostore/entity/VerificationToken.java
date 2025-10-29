package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "VerificationToken")
@Getter
@Setter
public class VerificationToken {
    private static final int EXPIRATION_MINUTES = 5; // Token valid for 15 minutes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "Token")
    private String token;


    private LocalDateTime expiryDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    public VerificationToken(){
        this.expiryDate = calculateExpiryDate();
    }

    public VerificationToken(String token, User user) {
        this();
        this.token = token;
        this.user = user;
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

}
