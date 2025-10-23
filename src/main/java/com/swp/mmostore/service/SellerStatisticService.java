package com.swp.mmostore.service;

import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerStatisticService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ShopStatisticDTO getStatisticdData(Integer shopId) {

        long totalOrders = orderRepository.countByShopId(shopId);
        long totalProducts = productRepository.countByShopId(shopId);
        BigDecimal totalRevenue = orderRepository.sumRevenueByShop(shopId);
        long pendingOrders = orderRepository.countByShopIdAndStatus(shopId, "PENDING");
        long completedOrders = orderRepository.countByShopIdAndStatus(shopId, "COMPLETED");
        long canceledOrders = orderRepository.countByShopIdAndStatus(shopId, "CANCELED");

        return new ShopStatisticDTO(
                totalOrders,
                totalProducts,
                totalRevenue,
                pendingOrders,
                completedOrders,
                canceledOrders
        );
    }
}
