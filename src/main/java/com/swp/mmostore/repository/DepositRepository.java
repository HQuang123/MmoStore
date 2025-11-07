package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Integer> {
    List<Deposit> findByUser(User user);
}
