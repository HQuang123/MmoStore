package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.dto.WalletTransactionEvent;
import com.swp.mmostore.entity.Item;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ItemRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderConsumer {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final WalletProducer walletProducer;

    private final String STATUS_FAILED = "FAILED";
    private final String STATUS_COMPLETED = "COMPLETED";


    @Transactional
    @KafkaListener(topics = "order-events", groupId = "order-group")
    public void consumeOrder(OrderEvent event) {

        System.out.println("Order event received: " + event.getOrderId());
        if (event.getOrderId() == null || event.getProductId() == null || event.getUserId() == null) {
            return;
        }

        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null) {
            System.out.println("User not found for order: " + event.getOrderId());
            return;
        }

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            System.out.println("Order not found: " + event.getOrderId());
            return;
        }

        if (STATUS_FAILED.equalsIgnoreCase(order.getStatus())) {
            System.out.println("Order already marked as FAILED: " + event.getOrderId());
            return;
        }

        List<Item> items = itemRepository.findUnsoldItem(event.getProductId(), PageRequest.of(0, order.getQuantity()));

        //no item in stock
        if (items.size() < order.getQuantity()) {
            orderRepository.updateOrderStatus(event.getOrderId(), STATUS_FAILED);

            WalletTransactionEvent transactionEvent = new WalletTransactionEvent();
            transactionEvent.setTransactionId(order.getOrderId());
            transactionEvent.setType(com.swp.mmostore.entity.ActionType.Order_refund);
            transactionEvent.setUserId(event.getUserId());
            walletProducer.sendTransactionEvent(transactionEvent);
            System.out.println("Not enough items in stock for order: " + event.getOrderId());
            return;
        }

        //process order
        items.forEach(item -> {
            item.setIsSold(true);
            item.setOrder(order);
            itemRepository.save(item);
        });
        orderRepository.updateOrderStatus(event.getOrderId(), STATUS_COMPLETED);
        System.out.println("Order processed successfully: " + event.getOrderId());
    }
}
