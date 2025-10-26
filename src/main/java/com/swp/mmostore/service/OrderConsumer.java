package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.entity.Item;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ItemRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderConsumer {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final String STATUS_FAILED = "FAILED";
    private final String STATUS_COMPLETED = "COMPLETED";

    @Transactional
    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void consumeOrder(OrderEvent event) {
        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null) {
            System.out.println("User not found for order: " + event.getOrderId());
        }

        //user dont have enough balance
        if (user.getBalance().compareTo(event.getTotalAmount()) < 0) {
            orderRepository.updateOrderStatus(event.getOrderId(), STATUS_FAILED);
            System.out.println("Insufficient balance for order: " + event.getOrderId());
            return;
        }

        List<Item> items = itemRepository.findUnsoldItem(event.getProductId(), PageRequest.of(0, event.getQuantity()));

        //no item in stock
        if (items.size() < event.getQuantity()) {
            orderRepository.updateOrderStatus(event.getOrderId(), STATUS_FAILED);
            System.out.println("Not enough items in stock for order: " + event.getOrderId());
            return;
        }

        //process order
        user.setBalance(user.getBalance().subtract(event.getTotalAmount()));
        items.forEach(item -> {
            item.setIsSold(true);
            Order order = orderRepository.findById(event.getOrderId()).orElseThrow();
            item.setOrder(order);
            itemRepository.save(item);
        });
        userRepository.save(user);
        orderRepository.updateOrderStatus(event.getOrderId(), STATUS_COMPLETED);
        System.out.println("Order processed successfully: " + event.getOrderId());
    }
}
