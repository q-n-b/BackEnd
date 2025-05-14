package com.example.qnb.login.service;

import com.example.qnb.login.security.UserDetailsImpl;
import com.example.qnb.login.entity.User;
import com.example.qnb.login.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetailsImpl loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserDetailsImpl(user);
    }
}