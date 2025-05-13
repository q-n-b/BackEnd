package com.example.qnb.login.service;

import com.example.qnb.login.dto.UserPreferenceRequestDto;
import com.example.qnb.login.entity.User;
import com.example.qnb.login.entity.UserPreference;
import com.example.qnb.login.repository.UserPreferenceRepository;
import com.example.qnb.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public void savePreference(UserPreferenceRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
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
