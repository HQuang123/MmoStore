package com.swp.mmostore.repository;

import com.swp.mmostore.entity.PasswordResetToken;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.entity.WithdrawalOtp;
import lombok.With;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface WithdrawalOtpRepository extends JpaRepository<WithdrawalOtp, Integer> {
    WithdrawalOtp findByTokenAndWithdrawal(String token, Withdrawal withdrawal);
    WithdrawalOtp findByWithdrawal(Withdrawal withdrawal);
}
