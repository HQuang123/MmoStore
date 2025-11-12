package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        // Tìm user trong database
        User user = userRepository.findActiveByEmail(email);

        if (user == null) {
            logger.warn("Không tìm thấy tài khoản với email: {}", email);
            throw new UsernameNotFoundException("Không tìm thấy tài khoản");
        }

        // Kiểm tra isDeleted
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            logger.warn("Tài khoản {} đã bị xóa (isDeleted = 1)", email);
            throw new DisabledException("Tài khoản đã bị xóa và không thể đăng nhập");
        }

        // Kiểm tra status (xác minh email)
        if (!user.getStatus()) {
            logger.warn("Tài khoản {} chưa được xác minh", email);
            throw new DisabledException("Tài khoản chưa được xác minh. Hãy đăng ký lại hoặc xác thực email");
        }

        // Kiểm tra tài khoản bị khóa
        if (Boolean.FALSE.equals(user.getAccountStatusNonLocked())) {
            logger.warn("Tài khoản {} bị khóa do nhiều lần đăng nhập sai", email);
            throw new LockedException("Tài khoản đã bị khóa. Vui lòng thử lại sau hoặc liên hệ admin");
        }

        // Lấy danh sách quyền
        List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        logger.info("Đăng nhập thành công cho user: {}", email);

        // Trả về đối tượng UserDetails mặc định của Spring
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
