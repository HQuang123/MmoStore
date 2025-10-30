package com.swp.mmostore.repository;

import com.swp.mmostore.entity.PasswordResetToken;
import com.swp.mmostore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
}
