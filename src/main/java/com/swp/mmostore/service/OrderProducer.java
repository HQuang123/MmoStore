package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.repository.OrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final OrderRepository orderRepository;

    private Integer lastSentOrderId = 0;

    private final String STATUS_PENDING = "PENDING";

    @PostConstruct
    public void fetchPendingOrder() {
        List<OrderEvent> newPendingOrders = orderRepository.findByStatusAfter(STATUS_PENDING, lastSentOrderId);
        for (OrderEvent order : newPendingOrders) {
            kafkaTemplate.send("order-events", order);
        }

        if (!newPendingOrders.isEmpty()) {
            lastSentOrderId = newPendingOrders.getLast().getOrderId();
        }
    }

    public void sendNewOrder(OrderEvent order) {
        kafkaTemplate.send("order-events", order);
    }
}
