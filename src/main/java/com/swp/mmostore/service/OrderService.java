package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderStatisticDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.WalletTransactionEvent;
import com.swp.mmostore.entity.ActionType;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserRepository userRepository;
    private final WalletProducer walletProducer;

    public Order createNewOrder(int quantity, double totalPrice, String userEmail, Integer productId) {
        Order order = new Order();
        order.setQuantity(quantity);
        order.setTotalPrice(BigDecimal.valueOf(totalPrice));
        Product product = new Product();
        product.setProductId(productId);
        order.setProduct(product);
        order.setStatus("PENDING");
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
        Order order1 = orderRepository.save(order);
        //add to queue to process
        WalletTransactionEvent event = new WalletTransactionEvent();
        event.setTransactionId(order.getOrderId());
        event.setType(ActionType.Order_payment);
        walletProducer.sendTransactionEvent(event);

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



    public Page<OrderStatisticDTO> getOrderHistory(
            Integer userId,
            String orderId,
            String productName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String status,
            String paymentMethod,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            Pageable pageable
    ) {
        return orderRepository.findOrderHistoryByUserId(
                Long.valueOf(userId),
                orderId,
                productName,
                startDateTime,
                endDateTime,
                status,
                paymentMethod,
                minTotal,
                maxTotal,
                pageable
        );
    }

    public Page<ShopOrderHistoryDTO> getOrderHistoryByShop(Integer shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findOrderHistoryByShop(shopId, pageable);
    }



    public Page<ShopOrderHistoryDTO> getFilteredOrders(
            Integer shopId,
            Integer minQuantity,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String status,
            Pageable pageable
    ) {
        return orderRepository.findFilteredOrdersByShop(
                shopId,
                minQuantity,
                minTotal,
                maxTotal,
                startDate,
                endDate,
                status,
                pageable
        );
    }

    public List<ShopOrderHistoryDTO> getFilteredOrdersNoPaging(
            Integer shopId,
            Integer minQuantity,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String status
    ) {
        return orderRepository.findFilteredOrdersByShopNoPaging(
                shopId, minQuantity, minTotal, maxTotal, startDate, endDate, status
        );
    }


}
