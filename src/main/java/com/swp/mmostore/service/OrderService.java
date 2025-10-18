package com.swp.mmostore.service;

import com.swp.mmostore.entity.Order;
import com.swp.mmostore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public Page<Order> getOrdersByUser(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        return orderRepository.findByUser_UserId(userId, pageable);
    }


    public Page<Order> findByUserAndOrderId(Integer userId, String orderId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        try {
            Integer id = Integer.parseInt(orderId);
            return orderRepository.findByUserAndOrderId(userId, id, pageable);
        } catch (NumberFormatException e) {
            return Page.empty(pageable); // nếu orderId không hợp lệ
        }
    }

    public Page<Order> findByUserAndDateRange(Integer userId, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return orderRepository.findByUserAndDateRange(userId, start, end, pageable);
    }

}
