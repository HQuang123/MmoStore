package com.swp.mmostore.repository;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WithdrawRequestRepository extends JpaRepository<Withdrawal, Integer> {
    List<Withdrawal> findByStatus(String status);
    Optional<Withdrawal> findByUserAndStatus(User user, String status);
    List<Withdrawal> findByUserOrderByCreateAtDesc(User user);

}
