package com.swp.mmostore.util;

import com.swp.mmostore.config.CustomUserDetails;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class MockSecurityUtils {
    private MockSecurityUtils() {
        // prevent instantiation
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof OAuth2User oAuth2User) {
            return oAuth2User.getAttribute("email");
        }

        return principal.toString();
    }
}
