package com.example.qnb.login.service;

import com.example.qnb.login.dto.SignupRequestDto;
import com.example.qnb.login.entity.User;
import com.example.qnb.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(SignupRequestDto request) {
        User user = new User();
        user.setUserEmail(request.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setName(request.getName());
        user.setUserNickname(request.getUserNickname());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfileUrl(request.getProfileUrl());
        return userRepository.save(user);
    }
}