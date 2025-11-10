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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        Page<Map<String, Object>> rawPage = orderRepository.findOrderHistoryByUserIdNative(
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

        List<OrderStatisticDTO> dtoList = rawPage.stream().map(map -> {
            Integer orderIdVal = ((Number) map.get("orderId")).intValue();
            String productNameVal = (String) map.get("productName");

            // ✅ Xử lý kiểu ngày giờ linh hoạt
            Object createAtObj = map.get("createAt");
            LocalDateTime createAtVal = null;
            if (createAtObj instanceof Timestamp ts) {
                createAtVal = ts.toLocalDateTime();
            } else if (createAtObj instanceof LocalDateTime ldt) {
                createAtVal = ldt;
            }

            int quantityVal = ((Number) map.get("quantity")).intValue();
            String statusVal = (String) map.get("status");

            BigDecimal unitPriceVal = (BigDecimal) map.get("unitPrice");   // ✅ thêm
            BigDecimal totalPriceVal = (BigDecimal) map.get("totalPrice");

            Integer shopIdVal = null;                                     // ✅ thêm
            Object shopIdObj = map.get("shopId");
            if (shopIdObj instanceof Number num) {
                shopIdVal = num.intValue();
            }

            String shopNameVal = (String) map.get("shopName");             // ✅ thêm

            // ✅ Xử lý JSON_ARRAYAGG → List<Map<String, String>>
            List<Map<String, String>> valuesVal;
            try {
                String json = (String) map.get("itemValues");
                if (json == null || json.isBlank()) {
                    valuesVal = Collections.emptyList();
                } else {
                    // B1: parse chuỗi JSON_ARRAYAGG thành List<String>
                    List<String> rawList = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                    // B2: parse từng chuỗi con thành Map
                    valuesVal = rawList.stream().map(str -> {
                        try {
                            return objectMapper.readValue(str, new TypeReference<Map<String, String>>() {});
                        } catch (Exception e) {
                            return Collections.<String, String>emptyMap();
                        }
                    }).toList();
                }
            } catch (Exception e) {
                valuesVal = Collections.emptyList();
            }

            // ✅ Tạo DTO với đủ 9 trường
            return new OrderStatisticDTO(
                    orderIdVal,
                    productNameVal,
                    createAtVal,
                    quantityVal,
                    statusVal,
                    unitPriceVal,
                    totalPriceVal,
                    shopIdVal,
                    shopNameVal,
                    valuesVal
            );
        }).toList();

        return new PageImpl<>(dtoList, pageable, rawPage.getTotalElements());
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
