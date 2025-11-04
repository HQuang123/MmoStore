package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import com.swp.mmostore.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

//This method will handle login failure
@Configuration
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    LoginRegistrationService loginRegistrationService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("username");
        User user = loginRegistrationService.getUserByEmail(email);

        if (user != null) {
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                exception = new LockedException("Account does not exist! Please register first.");
            } else if (!user.getStatus()) {
                exception = new LockedException("Your account is inactive! Please verify your email.");
            } else if (!user.getAccountStatusNonLocked()) {
                if (loginRegistrationService.isUnlockAccountTimeExpired(user)) {
                    exception = new LockedException("Your account is unlocked, you can login now!");
                } else {
                    exception = new LockedException("Your account is locked! Please try again later.");
                }
            } else if (user.getAccountFailedAttempt() >= AppConstant.ATTEMPT_COUNT) {
                loginRegistrationService.userAccountLock(user);
                exception = new LockedException("Your account has been locked after 3 failed attempts.");
            }
        } else {
            exception = new UsernameNotFoundException("Account does not exist! Please register first.");
        }

        super.setDefaultFailureUrl("/login?error");
        super.onAuthenticationFailure(request, response, exception);
    }



}
