package com.swp.mmostore.service;

import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public Order createNewOrder(int quantity, double totalPrice, String userEmail) {
        Order order = new Order();
        order.setQuantity(quantity);
        order.setTotalPrice(BigDecimal.valueOf(totalPrice));

        //get user
        Integer userId = userRepository.findByEmail(userEmail).getUserId();

        order.setCreateBy(userId);
        order.setCreateAt(LocalDateTime.now());

        User user = new User();
        user.setUserId(userId);
        order.setUser(user);

        return createPendingOrder(order);
    }


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
