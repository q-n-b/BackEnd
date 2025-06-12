package com.example.qnb.user.controller;

import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.user.dto.UserPreferenceRequestDto;
import com.example.qnb.user.security.UserDetailsImpl;
import com.example.qnb.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    // 사용자 취향 저장 API
    @PostMapping
    public ResponseEntity<?> save(@RequestBody @Valid UserPreferenceRequestDto dto,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new LoginRequiredException(); // 전역 핸들러에서 401 응답
        }

        Long userId = userDetails.getUserId();
        preferenceService.savePreference(userId, dto);

        return ResponseEntity.ok(Map.of(
                "message", "사용자 취향 정보가 성공적으로 저장되었습니다."
        ));
    }
}


