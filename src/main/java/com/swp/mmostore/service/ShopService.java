package com.swp.mmostore.service;

import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.repository.ShopRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    public Page<Shop> findPaginatedAndFiltered(int page, int size, String keyword, Boolean isDeleted) {
        Pageable pageable = PageRequest.of(page, size);
        return shopRepository.findFiltered(keyword, isDeleted, pageable);
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
}

