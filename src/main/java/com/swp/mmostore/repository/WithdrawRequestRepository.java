package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawRequestRepository extends JpaRepository<Withdrawal, Integer> {
    List<Withdrawal> findByStatus(String status);

}
