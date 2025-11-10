package com.swp.mmostore.service;

import com.swp.mmostore.entity.ShopFee;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ShopFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ShopFeeService {
    @Autowired
    private ShopFeeRepository shopFeeRepository;

    @Autowired
    private WalletService walletService;

    @Transactional
    public void chargeRegistrationFee(User user) {
        BigDecimal feeAmount = BigDecimal.valueOf(200_000);

        // Trừ tiền user và cộng vào admin ID 33
        walletService.deductMoney(user.getUserId(),feeAmount, "Phí đăng ký mở shop");

        // Lưu log phí
        ShopFee shopFee = new ShopFee();
        shopFee.setUser(user);
        shopFee.setFeeType(ShopFee.FeeType.REGISTRATION);
        shopFee.setAmount(feeAmount);
        shopFee.setStatus(ShopFee.FeeStatus.PAID);
        shopFee.setCreateBy(user.getUserId());
        shopFee.setCreateAt(LocalDateTime.now());

        shopFeeRepository.save(shopFee);
    }
}
