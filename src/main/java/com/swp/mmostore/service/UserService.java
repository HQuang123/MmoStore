package com.swp.mmostore.service;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<User> findPaginatedAndFiltered(int page, int size, String role, String status, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        if ((role == null || role.isEmpty()) &&
                (status == null || status.isEmpty()) &&
                (keyword == null || keyword.isEmpty())) {
            return userRepository.findAll(pageable);
        }

        Boolean statusValue = (status != null && !status.isEmpty()) ? Boolean.valueOf(status) : null;

        return userRepository.findFiltered(role, statusValue, keyword, pageable);
    }


    public void toggleUserStatus(Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            Boolean current = user.getStatus();
            user.setStatus(current == null || !current);
            userRepository.save(user);
        });
    }

}

