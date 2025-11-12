package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import com.swp.mmostore.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
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

        // Start with the original exception
        AuthenticationException customException = exception;

        if (user != null) {
            // Case 1: User is marked as deleted
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                customException = new LockedException("Tài khoản chưa tồn tại, hãy đăng ký mới.");
            }
            // Case 2: User is not verified
            else if (!user.getStatus()) {
                customException = new LockedException("Tài khoản chưa được xác thực. Hãy vào email để xác thực.");
            }
            // Case 3: User account is ALREADY locked
            else if (!user.getAccountStatusNonLocked()) {
                if (loginRegistrationService.isUnlockAccountTimeExpired(user)) {
                    // Assuming your service unlocks the user here
                    customException = new LockedException("Tài khoản của bạn đã được mở khóa! Vui lòng thử đăng nhập lại.");
                } else {
                    customException = new LockedException("Tài khoản của bạn đã khóa do nhập sai nhiều lần !");
                }
            }
            // Case 4: User is active, not locked, BUT password was wrong
            // This is the "Bad Credentials" check you were looking for.
            else if (exception instanceof BadCredentialsException) {

                // Check if attempts are still remaining before locking
                if (user.getAccountFailedAttempt() <= AppConstant.ATTEMPT_COUNT) {

                    loginRegistrationService.userFailedAttemptIncrease(user); //2
                    long attempts = user.getAccountFailedAttempt(); // Get the newly incremented value
                    long attemptsLeft = AppConstant.ATTEMPT_COUNT - attempts;
                    customException = new LockedException("Sai mật khẩu. Bạn còn " + attemptsLeft + " lần thử.");
                }
                // This is the final failed attempt that will lock the account
                else {
                    loginRegistrationService.userAccountLock(user);
                    customException = new LockedException("Tài khoản của bạn đã bị khóa sau " + AppConstant.ATTEMPT_COUNT + " failed attempts.");
                }
            }
        }
        // Case 5: User not found
        else {
            // Override the default "Bad credentials" for a non-existent user
            customException = new UsernameNotFoundException("Tài khoản chưa tồn tại");
        }

        // Set the failure URL and pass the new, custom exception to the super method
        super.setDefaultFailureUrl("/login?error");
        super.onAuthenticationFailure(request, response, customException);
    }
}