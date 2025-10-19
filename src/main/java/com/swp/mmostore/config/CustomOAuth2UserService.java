package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.LoginRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Minimal OAuth2UserService that adds ROLE_USER authority to any authenticated OAuth2 user
 * (e.g., Google) so that they can access endpoints protected with hasRole("USER").
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //the id token retrieved will be used to fetch the user details from gmail
    @Autowired
    private LoginRegistrationService userService;
    private UserRepository userRepository;

    public CustomOAuth2UserService() {

    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String phone = "";
        //this variable will be used to check the id of user of a specific provider
        String providerId = oAuth2User.getAttribute("sub");
        String provider = "google";
        String password =   UUID.randomUUID().toString();
        String profileImg = oAuth2User.getAttribute("picture");
        User user = userService.findByProviderId(providerId);
        if (user == null){
            user = new User(name, email,phone,password,provider,providerId);
            user.setProfileImage(profileImg);
        }

        userService.saveUser(user);
        User savedUser = userService.findByProviderId(providerId);
        int userId = savedUser.getUserId();
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes()); //oauth2user map is immutable
        attributes.put("userId", userId);
        List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new DefaultOAuth2User(
                authorities, //change from oauth2user.getAuthorities() to athorities because oauth2user.getAuthorities() value  is DefaultOAuth2userAuthority, not ROLE_USER, ROLE_SELLER,
                //spring security config: authenticated user with role ROLE_USER can access /user/* but oauth2User was given role DefaultOAuth2userAuthority --> 404 error even when login with google
                //Fix solution: change oAuth2User.getAuthorities() to List<GrantedAuthority> authorities
                //oAuth2User.getAttributes(),
                attributes,
                "email"
        );
    }

}
