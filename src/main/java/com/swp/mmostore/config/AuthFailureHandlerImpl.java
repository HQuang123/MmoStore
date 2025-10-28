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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

//This method will handle login failure
@Configuration
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    LoginRegistrationService loginRegistrationService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");
        User user= loginRegistrationService.getUserByEmail(email);
        if(user != null ){
            //if user email exists
            if(user.getStatus()){ //get account status
                if(user.getAccountStatusNonLocked()){
                    //if account is not locked, -> login failed -> increase failed attempt
                    if(user.getAccountFailedAttempt() < AppConstant.ATTEMPT_COUNT){
                        loginRegistrationService.userFailedAttemptIncrease(user);
                    }
                    //if attemp = 3 -> lock account
                    else{
                        loginRegistrationService.userAccountLock(user);
                        exception = new LockedException("Your account is locked! Failed attempt 3");
                    }
                }else{
                    if(loginRegistrationService.isUnlockAccountTimeExpired(user)){
                        exception = new LockedException("Your account is unlocked, now you can not login to system");
                    }
                    else{
                        exception = new LockedException("Your account is locked! Please try after sometimes");
                    }
                }
            }
            else {
                exception = new LockedException("Your account is inactive! Please contact admin");
            }
        }
        else{
            exception = new LockedException("Your account does not exist! Please register first");
        }
        super.setDefaultFailureUrl("/login?error");
        //cai nay modify AuthenticationException (LockedException) -> dua vao SpringSecurity_LastException
        super.onAuthenticationFailure(request, response, exception);
        //Comment this line to redirect to login page when login failed to avoid resubmission
        //response.sendRedirect("/login?error");
    }


}
