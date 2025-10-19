package com.swp.mmostore.service;

import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositRepository depositRepository;
    public Deposit createPendingDeposit(Deposit deposit){
        deposit.setStatus(DepositStatus.Pending);
        depositRepository.save(deposit);
        return deposit;
    }


    public void markSuccessDeposit(Deposit deposit){
        deposit.setStatus(DepositStatus.Completed);
        depositRepository.save(deposit);
    }

    public void markFailedDeposit(Deposit deposit){
        deposit.setStatus(DepositStatus.Failed);
        depositRepository.save(deposit);
    }

    public void markCancelDeposit(Deposit deposit){
        deposit.setStatus(DepositStatus.Cancelled);
        depositRepository.save(deposit);
    }
}
