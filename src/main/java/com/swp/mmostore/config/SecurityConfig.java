package com.swp.mmostore.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    @Lazy
    AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    @Lazy
    AuthenticationFailureHandler authenticationFailureHandler;


    @Autowired
    @Lazy
    CustomOAuth2UserService customOAuth2UserService;


    @Bean
    //DAOAuthenticationProvider use the information of user from CustomUserDetailsService to compare  between user input and the stored password in database
    public DaoAuthenticationProvider authProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    //CustomUserDetailService takes UserDetails object from method loadUserByUsername
    //both CustomUserDetails and User implements the interface UserDetails

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(req -> req
                    .requestMatchers("/user/api/**").hasRole("USER")
                .requestMatchers("/user/**").hasRole("USER") //localhost:8080/user/product?productId=1
                    .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/admin/**").hasRole("ADMIN") //localhost:8080/product --> ko authen -> bam nut Mua hang
                  //GetMapping   localhot:8080/user//
                    //localhost:8080/ --> localhost:8080/products --> /products?productId=1 -> /products?
                .requestMatchers("/**").permitAll()
            )
            .authenticationProvider(authProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login-process")
                .failureHandler(authenticationFailureHandler)
                .successHandler(authenticationSuccessHandler)
            )
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) //take the access key-> send to userinfo endpoint to get the user details
            )
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logoutMessage=successful").permitAll());
        return http.build();
    }
}