package com.example.qnb.login.controller;

import com.example.qnb.login.dto.UserPreferenceRequestDto;
import com.example.qnb.login.service.UserPreferenceService;
import com.example.qnb.login.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody @Valid UserPreferenceRequestDto dto,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Long userId = userDetails.getUserId(); // 인증 정보에서 userId 꺼냄
            preferenceService.savePreference(userId, dto);
            return ResponseEntity.ok(Map.of("message", "사용자 취향 정보가 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "errorCode", "INTERNAL_SERVER_ERROR",
                            "errorMessage", "서버 오류가 발생했습니다."
                    )
            );
        }
    }

    // 유효성 검사 실패 시 (예: 필수 항목 누락)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "errorCode", "MISSING_FIELD",
                        "errorMessage", errorMessage
                )
        );
    }
}

