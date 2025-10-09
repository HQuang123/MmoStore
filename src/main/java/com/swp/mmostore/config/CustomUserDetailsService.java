package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import org.slf4j.Logger; // Import the logger
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger =  LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username: {}", username);
        User user = userRepository.findByEmail(username);
        if(user == null){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomUserDetails(user);
        //return the user details object which contains (password, username, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities-role)
    }
}
