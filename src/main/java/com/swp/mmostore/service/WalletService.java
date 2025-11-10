package com.swp.mmostore.service;

import com.swp.mmostore.entity.ActionType;
import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;


    @Transactional
    public boolean deductMoney(Integer userId, BigDecimal amount, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra số dư
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư không đủ");
        }

        // Trừ tiền
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        User admin = userRepository.findById(33)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin"));
        admin.setBalance(admin.getBalance().add(amount));
        userRepository.save(admin);

        return true;
    }
}
