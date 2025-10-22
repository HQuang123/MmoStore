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

    private Integer lastSentOrderId = 0;

    private final String STATUS_PENDING = "PENDING";

    @Scheduled(fixedRate = 5000)
    public void sendPendingOrders() {

        List<OrderEvent> newPendingOrders = orderRepository.findByStatusAfter(STATUS_PENDING, lastSentOrderId);
        for (OrderEvent order : newPendingOrders) {
            kafkaTemplate.send("order-events", order);
        }

        if (!newPendingOrders.isEmpty()) {
            lastSentOrderId = newPendingOrders.getLast().getOrderId();
        }
    }
}
