package com.example.qnb.login.security;

import com.example.qnb.login.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public String getNickname() {
        return user.getUserNickname();
    }

    public Long getUserId() {
        return user.getUserId(); // ← Entity에 따라 필드명 확인
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getUserPassword(); // User 엔티티 비밀번호 필드명 확인
    }

    @Override
    public String getUsername() {
        return user.getUserEmail(); // User 엔티티 이메일 필드명 확인
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
