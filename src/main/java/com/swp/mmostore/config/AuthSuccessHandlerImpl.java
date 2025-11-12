package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Configuration
public class AuthSuccessHandlerImpl implements AuthenticationSuccessHandler {
    @Autowired
    LoginRegistrationService loginRegistrationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> role = AuthorityUtils.authorityListToSet(authorities);
        String email = authentication.getName();
        User user = loginRegistrationService.getUserByEmail(email);
        if (user != null && user.getAccountFailedAttempt() > 0) {
            loginRegistrationService.resetFailedAttempts(user);
        }

        if(role.contains("ROLE_ADMIN")){
            response.sendRedirect("/admin");
        }else{ //if role is ROLE_USER
            response.sendRedirect("/");
        };
    }
}
