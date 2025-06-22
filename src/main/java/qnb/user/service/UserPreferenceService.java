package qnb.user.service;

import org.springframework.security.access.AccessDeniedException;
import qnb.user.dto.UserPreferenceRequestDto;
import qnb.user.entity.User;
import qnb.user.entity.UserPreference;
import qnb.user.repository.UserPreferenceRepository;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public void savePreference(Long userId, UserPreferenceRequestDto dto) {

        // 사용자 조회 및 없을 경우 예외 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 취향조사를 한 경우 차단
        if (user.isHasReadingTaste()) {
            throw new AccessDeniedException("이미 취향조사를 완료한 사용자입니다.");
        }

        // 취향조사 완료 상태로 변경
        user.setHasReadingTaste(true);

        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setReadingAmount(dto.getReadingAmount());
        preference.setImportantFactor(dto.getImportantFactor());
        preference.setPreferredGenres(dto.getPreferredGenres());
        preference.setPreferredKeywords(dto.getPreferredKeywords());
        preference.setPreferredBookId(dto.getBookId());

        // 사용자, 취향 정보 저장
        userRepository.save(user);
        preferenceRepository.save(preference);
    }
}
