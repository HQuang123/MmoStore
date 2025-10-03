package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Minimal OAuth2UserService that adds ROLE_USER authority to any authenticated OAuth2 user
 * (e.g., Google) so that they can access endpoints protected with hasRole("USER").
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //the id token retrieved will be used to fetch the user details from gmail
    @Autowired
    private LoginRegistrationService userService;

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
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "email"
        );
    }

}
