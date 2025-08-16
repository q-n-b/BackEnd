package qnb.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qnb.common.config.GptUserProperties;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import qnb.user.security.UserDetailsImpl;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GptUserProperties gptProps; // 시스템 계정 이메일/ID 보관

    /**
     * 스프링 시큐리티 표준 진입점 (아이디/이메일 기반 인증)
     * - GPT 시스템 이메일은 즉시 차단
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // GPT 시스템 계정 로그인 차단
        if (email != null && email.equalsIgnoreCase(gptProps.getEmail())) {
            throw new BadCredentialsException("시스템 계정은 로그인할 수 없습니다.");
        }

        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 필요 시 비활성/잠김 계정 차단 로직 추가 가능
        // if (!user.isActive()) { throw new DisabledException("비활성 계정"); }

        return new UserDetailsImpl(user);
    }

    //JWT 등에서 userId로 사용자 주체를 복원할 때 사용
    public UserDetailsImpl loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserDetailsImpl(user);
    }
}
