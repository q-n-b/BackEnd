package qnb.user.service;

import qnb.user.dto.SignupRequestDto;
import qnb.user.dto.UserInfoResponseDto;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import qnb.common.exception.UserNotFoundException;


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

    public UserInfoResponseDto getMyInfo(User user) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        return new UserInfoResponseDto(foundUser);
    }
}