package com.swp.mmostore.service;

import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ShopRepository;
import com.swp.mmostore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopService shopService;

    public Page<User> findPaginatedAndFiltered(int page, int size, String role, String status, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        if ((role == null || role.isEmpty()) &&
                (status == null || status.isEmpty()) &&
                (keyword == null || keyword.isEmpty())) {
            return userRepository.findAll(pageable);
        }

        Boolean statusValue = (status != null && !status.isEmpty()) ? Boolean.valueOf(status) : null;

        return userRepository.findFilteredWithShop(role, statusValue, keyword, pageable);
    }

    @Transactional
    public void toggleUserStatus(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            // Toggle user active/inactive
            boolean newStatus = !Boolean.TRUE.equals(user.getStatus());
            boolean deleteStatus = !Boolean.TRUE.equals(user.getIsDeleted());
            user.setStatus(newStatus);
            user.setIsDeleted(deleteStatus);
            userRepository.save(user);

            // If user has a shop, also toggle its status
            Shop shop = user.getShop();
            if (shop != null) {
                // Call ShopService instead of saving manually ✅
                shopService.toggleShopStatus(shop.getShopId());
            }
        });
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }



}

