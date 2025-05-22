package com.example.qnb.user.controller;

import com.example.qnb.user.dto.UserPreferenceRequestDto;
import com.example.qnb.user.security.UserDetailsImpl;
import com.example.qnb.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/preferences")
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    public UserPreferenceController(UserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    // ✅ 사용자 취향 저장 API
    @PostMapping
    public ResponseEntity<?> save(@RequestBody @Valid UserPreferenceRequestDto dto,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of(
                                "errorCode", "UNAUTHORIZED",
                                "errorMessage", "인증 정보가 없습니다. 로그인 후 다시 시도해 주세요."
                        )
                );
            }

            Long userId = userDetails.getUserId();
            preferenceService.savePreference(userId, dto);

            return ResponseEntity.ok(Map.of(
                    "message", "사용자 취향 정보가 성공적으로 저장되었습니다."
            ));

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

    // ✅ 필수 입력값 누락 시 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("필수 입력 항목이 누락되었습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "errorCode", "MISSING_FIELD",
                        "errorMessage", errorMessage
                )
        );
    }

    // ✅ 접근 권한 부족 (ex. 토큰은 있으나 ROLE 부족 등)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of(
                        "errorCode", "FORBIDDEN",
                        "errorMessage", "접근 권한이 없습니다."
                )
        );
    }
}


