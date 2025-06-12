package qnb.user.service;

import qnb.user.security.UserDetailsImpl;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
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