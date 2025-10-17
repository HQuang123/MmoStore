package com.swp.mmostore.service;

import com.swp.mmostore.entity.Order;
import com.swp.mmostore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public Order createPendingOrder(Order order){
        order.setStatus("PENDING");
        orderRepository.save(order);
        return order;
    }

    public Order findOrder(int orderId){
        return orderRepository.findById(orderId).orElse(null);
    }

    public void markSuccessOrder(Order order){
        order.setStatus("SUCCESS");
        orderRepository.save(order);
    }

    public void markFailedOrder(Order order){
        order.setStatus("FAILED");
        orderRepository.save(order);
    }
}
