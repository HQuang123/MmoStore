package com.swp.mmostore.config;

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
import java.util.Set;

/**
 * Minimal OAuth2UserService that adds ROLE_USER authority to any authenticated OAuth2 user
 * (e.g., Google) so that they can access endpoints protected with hasRole("USER").
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    //the id token retrieved will be used to fetch the user details from gmail
    //delegate  will be used to fetch the user details from the provider
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Copy existing authorities and add ROLE_USER
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oAuth2User.getAuthorities());
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Use "sub" (Google subject) as key attribute if present, else fall back to "id" or "email"
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        if (nameAttributeKey == null || nameAttributeKey.isBlank()) {
            nameAttributeKey = oAuth2User.getAttributes().containsKey("sub") ? "sub" :
                    (oAuth2User.getAttributes().containsKey("id") ? "id" : "email");
        }

        return new DefaultOAuth2User(mappedAuthorities, oAuth2User.getAttributes(), nameAttributeKey);
    }
}
