// 시작 시 GPT 시스템 사용자 보장 + userId 주입 (멀티 인스턴스 안전 버전)
package qnb.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import qnb.user.entity.Gender;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class GptUserInitializer {

    private final UserRepository userRepository;
    private final GptUserProperties props;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner ensureGptUser() {
        return args -> initGptUser();
    }

    /**
     * 이메일(UNIQUE) 기준으로 GPT 시스템 계정을 보장.
     * - 존재: userId 주입
     * - 부재: 랜덤 비번 + 해시 저장으로 생성 (동시 생성 충돌은 재조회로 회복)
     */
    @Transactional
    protected void initGptUser() {
        final String email = props.getEmail();

        User gpt = userRepository.findByUserEmail(email).orElse(null);
        if (gpt == null) {
            try {
                String randomRaw = UUID.randomUUID().toString();
                String hashed = passwordEncoder.encode(randomRaw);

                // 필수 컬럼들 채우기
                User u = new User();
                u.setUserEmail(email);
                u.setUserPassword(hashed);
                u.setName("System");
                u.setUserNickname(props.getNickname()); // "GPT"
                u.setBirthDate(LocalDate.of(1970, 1, 1));
                u.setGender(Gender.M);           // 예: Gender.MALE / FEMALE 중 하나
                u.setPhoneNumber("000-0000-0000");         // 시스템용 더미 번호

                gpt = userRepository.save(u);

            } catch (DataIntegrityViolationException e) {
                // 다중 인스턴스 레이스 → UNIQUE 충돌 시 재조회
                gpt = userRepository.findByUserEmail(email)
                        .orElseThrow(() -> new IllegalStateException("GPT 유저 생성 충돌 후 재조회 실패"));
            }
        }

        props.setUserId(gpt.getUserId());
    }
}