package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByEmail(String email);

    public List<User> findByRole(String role);

    public User findByResetToken(String resetToken);

    public User findByProviderId(String providerId);




}
