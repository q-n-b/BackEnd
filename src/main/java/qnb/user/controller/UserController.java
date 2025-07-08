package qnb.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import qnb.common.exception.*;
import qnb.user.JWT.JwtTokenProvider;
import qnb.user.dto.*;
import qnb.user.entity.RefreshToken;
import qnb.user.entity.User;
import qnb.user.repository.RefreshTokenRepository;
import qnb.user.repository.UserRepository;
import qnb.user.security.UserDetailsImpl;
import qnb.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

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

        // 1. 이메일 형식 유효성 검사
        if (!request.getUserEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailFormatException();
        }

        // 2. 이메일 중복 체크
        if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        // 3. 비밀번호 일치 확인
        if (!request.getUserPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        // 4. 필수값 누락 확인
        if (request.getUserEmail() == null || request.getUserPassword() == null ||
                request.getConfirmPassword() == null || request.getName() == null) {
            throw new MissingFieldException();
        }

        // 5. 프로필 이미지 저장 처리
        String profileUrl = null;
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
                String savePath = "/your/save/path/" + fileName;
                profileImage.transferTo(new File(savePath));
                profileUrl = "/images/" + fileName;
            }
            request.setProfileUrl(profileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("errorCode", "IMAGE_SAVE_FAILED", "errorMessage", "프로필 이미지 저장 중 오류가 발생했습니다.")
            );
        }

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
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto request) {

        // 1. 필수 입력값 누락 확인
        if (request.getUserEmail() == null || request.getUserPassword() == null) {
            throw new MissingFieldException();
        }

        // 2. 이메일 존재 확인
        Optional<User> optionalUser = userRepository.findByUserEmail(request.getUserEmail());
        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = optionalUser.get();

        // 3. 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.getUserPassword(), user.getUserPassword())) {
            throw new InvalidCredentialsException();
        }

        // 4. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserEmail());

        // 5. RefreshToken 저장
        RefreshToken token = new RefreshToken(user.getUserEmail(), refreshToken);
        refreshTokenRepository.save(token);

        // 6. 성공 응답
        return ResponseEntity.ok(Map.of(
                "message", "로그인에 성공했습니다.",
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    //내 정보 조회 반환 API
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new UserNotFoundException();
        }

        User user = Optional.ofNullable(userDetails)
                .map(UserDetailsImpl::getUser)
                .orElseThrow(UserNotFoundException::new);

        UserInfoResponseDto response = userService.getMyInfo(user);

        return ResponseEntity.ok(response);
    }

    //비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDto request,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.changePassword(userDetails.getUserId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Collections.singletonMap("message", "비밀번호가 변경되었습니다."));
    }

    //닉네임 변경
    @PatchMapping("/me/nickname")
    public ResponseEntity<Map<String, String>> changeNickname(
            @RequestBody ChangeNicknameRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        userService.changeNickname(userDetails.getUserId(), request.getUserNickname());

        return ResponseEntity.ok(Map.of("message", "닉네임이 변경되었습니다."));
    }
}
