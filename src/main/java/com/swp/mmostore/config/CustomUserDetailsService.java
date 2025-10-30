package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import org.slf4j.Logger; // Import the logger
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản");
        }
        if(!user.getStatus()){
            throw new UsernameNotFoundException("Tài khoản chưa được xác minh. Hãy đăng ký lại");
        }

        List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());


        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), //user details will be saved in the authentication object of spring context
                user.getPassword(),
                authorities
        );
    }
}

