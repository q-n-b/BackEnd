package com.example.qnb.login.controller;

import com.example.qnb.login.JWT.JwtTokenProvider;
import com.example.qnb.login.dto.LoginRequestDto;
import com.example.qnb.login.dto.LoginResponseDto;
import com.example.qnb.login.dto.SignupRequestDto;
import com.example.qnb.login.entity.RefreshToken;
import com.example.qnb.login.entity.User;
import com.example.qnb.login.repository.RefreshTokenRepository;
import com.example.qnb.login.repository.UserRepository;
import com.example.qnb.login.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원가입 API
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("data") SignupRequestDto request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            // 1. 이메일 형식 유효성 검사
            if (!request.getUserEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "INVALID_EMAIL", "errorMessage", "이메일 형식이 올바르지 않습니다.")
                );
            }

            // 2. 이메일 중복 체크
            if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("errorCode", "EMAIL_ALREADY_EXISTS", "errorMessage", "이미 사용 중인 이메일입니다.")
                );
            }

            // 3. 비밀번호 일치 확인
            if (!request.getUserPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "PASSWORD_MISMATCH", "errorMessage", "비밀번호가 일치하지 않습니다.")
                );
            }

            // 4. 필수값 누락 확인
            if (request.getUserEmail() == null || request.getUserPassword() == null ||
                    request.getConfirmPassword() == null || request.getName() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "MISSING_FIELD", "errorMessage", "필수 입력 항목이 누락되었습니다.")
                );
            }

            // 5. 프로필 이미지 저장 처리
            String profileUrl = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
                String savePath = "/your/save/path/" + fileName;
                profileImage.transferTo(new File(savePath));
                profileUrl = "/images/" + fileName;
            }
            request.setProfileUrl(profileUrl);

            // 6. 회원 등록
            User user = userService.registerUser(request);

            // 7. Access Token & Refresh Token 발급
            String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserEmail());


            // 8. RefreshToken 저장
            RefreshToken token = new RefreshToken(user.getUserEmail(), refreshToken);
            refreshTokenRepository.save(token);

            // 9. 응답 반환
            return ResponseEntity.ok(Map.of(
                    "message", "회원가입이 완료되었습니다.",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("errorCode", "INTERNAL_SERVER_ERROR", "errorMessage", "서버 오류가 발생했습니다.")
            );
        }
    }

    
    //로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<User> optionalUser = userRepository.findByUserEmail(request.getUserEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("errorCode", "INVALID_CREDENTIALS", "errorMessage", "이메일 또는 비밀번호가 일치하지 않습니다.")
            );
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("errorCode", "INVALID_CREDENTIALS", "errorMessage", "이메일 또는 비밀번호가 일치하지 않습니다.")
            );
        }

        // 1. AccessToken & RefreshToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserEmail());

        // 2. RefreshToken 저장 (기존 사용자면 덮어쓰기)
        RefreshToken token = new RefreshToken(user.getUserEmail(), refreshToken);
        refreshTokenRepository.save(token);

        // 3. 응답 반환
        return ResponseEntity.ok(Map.of(
                "message", "로그인에 성공했습니다.",
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

}
