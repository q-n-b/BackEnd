package com.example.qnb.login.controller;

import com.example.qnb.login.JWT.JwtTokenProvider;
import com.example.qnb.login.dto.LoginRequestDto;
import com.example.qnb.login.dto.LoginResponseDto;
import com.example.qnb.login.dto.SignupRequestDto;
import com.example.qnb.login.entity.User;
import com.example.qnb.login.repository.UserRepository;
import com.example.qnb.login.service.UserService;

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
@CrossOrigin(origins = "localhost:3000")
@RequestMapping("/api/users")

public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    // 회원가입 API
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("data") SignupRequestDto request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            // 이메일 형식 유효성 검사
            if (!request.getUserEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "INVALID_EMAIL", "errorMessage", "이메일 형식이 올바르지 않습니다.")
                );
            }

            // 이메일 중복 체크
            if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        Map.of("errorCode", "EMAIL_ALREADY_EXISTS", "errorMessage", "이미 사용 중인 이메일입니다.")
                );
            }

            // 비밀번호 일치 확인
            if (!request.getUserPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "PASSWORD_MISMATCH", "errorMessage", "비밀번호가 일치하지 않습니다.")
                );
            }

            // 필수값 누락 확인
            if (request.getUserEmail() == null || request.getUserPassword() == null ||
                    request.getConfirmPassword() == null || request.getName() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("errorCode", "MISSING_FIELD", "errorMessage", "필수 입력 항목이 누락되었습니다.")
                );
            }

            // 프로필 이미지 저장 처리
            String profileUrl = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
                String savePath = "/your/save/path/" + fileName; // 실제 서버 경로로 변경해야 함
                profileImage.transferTo(new File(savePath));
                profileUrl = "/images/" + fileName; // 클라이언트가 접근할 수 있는 URL 경로
            }

            // DTO에 profileUrl을 주입 (필요하면 DTO에 setProfileUrl 추가)
            request.setProfileUrl(profileUrl);

            // 서비스로 위임
            userService.registerUser(request);

            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("errorCode", "INTERNAL_SERVER_ERROR", "errorMessage", "서버 오류가 발생했습니다.")
            );
        }
    }


    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        Optional<User> optionalUser = userRepository.findByUserEmail(request.getUserEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());

        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }
}
