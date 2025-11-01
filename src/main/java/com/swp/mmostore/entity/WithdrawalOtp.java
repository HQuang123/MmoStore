package com.swp.mmostore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

import java.time.LocalDateTime;

@Entity
@Table(name = "WithdrawalOtp")
@Getter
@Setter
public class WithdrawalOtp {
    private static final int EXPIRATION_MINUTES = 5; // Token valid for 15 minutes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "Token")
    private String token;


    private LocalDateTime expiryDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "WithdrawalID", nullable = false)
    private Withdrawal withdrawal;

    public WithdrawalOtp(){
        this.expiryDate = calculateExpiryDate();
    }

    public WithdrawalOtp(String token, Withdrawal withdrawal) {
        this();
        this.token = token;
        this.withdrawal = withdrawal;
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

}
