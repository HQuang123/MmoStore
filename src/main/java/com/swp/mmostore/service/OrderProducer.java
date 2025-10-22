package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final OrderRepository orderRepository;

    private LocalDateTime lastSentTime = LocalDateTime.MIN;

    @Scheduled(fixedRate = 5000)
    public void sendPendingOrders() {

        List<OrderEvent> newPendingOrders = orderRepository.findByStatusAfter("PENDING", lastSentTime);
        for (OrderEvent order : newPendingOrders) {
            kafkaTemplate.send("order-events", order);
        }

        if (!newPendingOrders.isEmpty()) {
            lastSentTime = LocalDateTime.now();
        }
    }
}
