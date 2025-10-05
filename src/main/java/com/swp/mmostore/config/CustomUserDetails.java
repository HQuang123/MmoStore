package com.swp.mmostore.config;

import com.swp.mmostore.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }
    //provide authentication object know the role - authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(user.getRole());
        return Arrays.asList(simpleGrantedAuthority);
    }

    //provide the password for authentication from the database
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    //provide the username for authentication from database
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // null hoặc true => cho phép đăng nhập
        return user.getAccountStatusNonLocked() == null || user.getAccountStatusNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isEnabled() {
        // null hoặc true => coi như enable
        return user.getStatus() == null || user.getStatus();
    }

}
