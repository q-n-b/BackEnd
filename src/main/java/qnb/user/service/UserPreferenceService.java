package qnb.user.service;

import jakarta.transaction.Transactional;
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

    @Transactional
    public void savePreference(Long userId, UserPreferenceRequestDto dto) {
        // 사용자 조회 및 없을 경우 예외 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        System.out.println("🟡 userId = " + userId);
        System.out.println("🟢 DB에서 조회한 user.hasReadingTaste = " + user.getHasReadingTaste());


        // 이미 취향조사를 한 경우 차단
        if (user.getHasReadingTaste()) {
            throw new AccessDeniedException("이미 취향조사를 완료한 사용자입니다.");
        }

        // 취향 정보 저장
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setReadingAmount(dto.getReadingAmount());
        preference.setImportantFactor(dto.getImportantFactor());
        preference.setPreferredGenres(dto.getPreferredGenres());
        preference.setPreferredKeywords(dto.getPreferredKeywords());
        preference.setPreferredBookId(dto.getPreferredBookId());

        if ((dto.getPreferredGenres() != null && !dto.getPreferredGenres().isEmpty()) ||
                (dto.getPreferredKeywords() != null && !dto.getPreferredKeywords().isEmpty()) ||
                (dto.getPreferredBookId() != null && !dto.getPreferredBookId().isEmpty())){

            // 데이터가 하나라도 제대로 들어왔을 때만 true 설정
            preferenceRepository.save(preference);
            user.setHasReadingTaste(true);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("유효한 취향 정보가 없습니다.");
        }

    }
}
