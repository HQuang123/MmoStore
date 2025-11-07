package com.swp.mmostore.service;

import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.dto.ShopViewDTO;
import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    public ShopViewDTO findShopViewById(Integer shopId){
        return shopRepository.findShopViewById(shopId);
    }

    public Page<Shop> findPaginatedAndFiltered(int page, int size, String keyword, Boolean isDeleted) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Integer> shopIds = shopRepository.findShopIdsFiltered(keyword, isDeleted, pageable);
        if (shopIds.isEmpty()) return Page.empty(pageable);

        List<Shop> shops = shopRepository.findAllByIdWithUser(shopIds.getContent());
        return new PageImpl<>(shops, pageable, shopIds.getTotalElements());
    }

    @Transactional
    public void toggleShopStatus(Integer shopId) {
        shopRepository.findById(shopId).ifPresent(shop -> {
            boolean newStatus = !shop.getIsDeleted();
            shop.setIsDeleted(newStatus);
            shopRepository.save(shop);

            // Update all products belonging to this shop
            productRepository.updateProductDeletedStatusByShopId(shopId, newStatus);

        });
    }

    public Shop save(Shop shop) {
        return shopRepository.save(shop);
    }

    public Shop findByUserId(Integer userId) {
        return shopRepository.findByUser_UserId(userId);
    }



    public Page<ProductSalesDTO> getSoldProductsByShop(Integer shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findSoldProductsByShop(shopId, pageable);
    }


    public List<ProductSalesDTO> getAllSoldProductsByShop(Integer shopId) {
        return orderRepository.findAllSoldProductsByShop(shopId);
    }


    public ShopStatisticDTO getStatisticdData(Integer shopId) {

        long totalOrders = orderRepository.countByShopId(shopId);
        long totalProducts = productRepository.countByShopId(shopId);
        BigDecimal totalRevenue = orderRepository.sumRevenueByShop(shopId);
        long pendingOrders = orderRepository.countByShopIdAndStatus(shopId, "PENDING");
        long completedOrders = orderRepository.countByShopIdAndStatus(shopId, "COMPLETED");
        long canceledOrders = orderRepository.countByShopIdAndStatus(shopId, "FAILED");

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
