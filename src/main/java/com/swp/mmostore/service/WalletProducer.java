package com.swp.mmostore.service;

import com.swp.mmostore.dto.WalletTransactionEvent;
import com.swp.mmostore.dto.WithdrawRequest;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletProducer {
    private final KafkaTemplate<String, WalletTransactionEvent> kafkaTemplate1;
    private final KafkaTemplate<String, WithdrawRequest> kafkaTemplate2;

    private String topic;

    public void sendTransactionEvent(WalletTransactionEvent event) {
        kafkaTemplate1.send("wallet-transaction", event);
    }

    public void sendWithdrawRequest(User user, Withdrawal withdrawal) {
        kafkaTemplate2.send("withdraw-request", new WithdrawRequest(user.getUserId(), withdrawal.getId()));
    }
}
