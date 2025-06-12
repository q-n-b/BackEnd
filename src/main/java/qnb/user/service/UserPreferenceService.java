package com.example.qnb.user.service;

import com.example.qnb.user.dto.UserPreferenceRequestDto;
import com.example.qnb.user.entity.User;
import com.example.qnb.user.entity.UserPreference;
import com.example.qnb.user.repository.UserPreferenceRepository;
import com.example.qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public void savePreference(Long userId, UserPreferenceRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//DB에 조회하고 없으면 예외 던져서 에러 처리함

        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setReadingAmount(dto.getReadingAmount());
        preference.setImportantFactor(dto.getImportantFactor());
        preference.setPreferredGenres(dto.getPreferredGenres());
        preference.setPreferredKeywords(dto.getPreferredKeywords());
        preference.setBookId(dto.getBookId());

        preferenceRepository.save(preference); //모든 값이 설정된 객체를 DB에 저장
    }
}
