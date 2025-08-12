package qnb.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import qnb.common.dto.S3Uploader;
import qnb.common.exception.*;
import qnb.common.JWT.JwtTokenProvider;
import qnb.user.dto.*;
import qnb.user.entity.RefreshToken;
import qnb.user.entity.User;
import qnb.user.event.UserBookReadAddedEvent;
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Uploader s3Uploader;
    private final ApplicationEventPublisher eventPublisher;

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

        // 5. 프로필 이미지 → S3 업로드
        String profileUrl = null;
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                profileUrl = s3Uploader.upload(profileImage, "profiles");
            }
            request.setProfileUrl(profileUrl); // 프로필 URL 주입
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("errorCode", "IMAGE_UPLOAD_FAILED", "errorMessage", "프로필 이미지 업로드 중 오류가 발생했습니다.")
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

    //계정 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            userService.deleteUser(userDetails.getUserId());
            return ResponseEntity.noContent().build();  // 204 No Content
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("errorCode", "UNAUTHORIZED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorCode", "INTERNAL_SERVER_ERROR", "message", "계정 탈퇴 처리 중 서버 오류가 발생했습니다."));
        }
    }

    //프로필 이미지 변경
    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestPart("profileImage") MultipartFile profileImage) {

        User user = userDetails.getUser();

        try {
            User updatedUser = userService.updateProfileImage(user.getUserId(), profileImage);

            return ResponseEntity.ok(Map.of(
                    "data", Map.of(
                            "userId", updatedUser.getUserId(),
                            "nickname", updatedUser.getUserNickname(),
                            "profileUrl", updatedUser.getProfileUrl()
                    ),
                    "message", "프로필 이미지가 변경되었습니다."
            ));

        } catch (InvalidImageFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "errorCode", "INVALID_IMAGE_FORMAT",
                    "message", "이미지 파일이 없거나 지원되지 않는 형식입니다. (JPEG, PNG만 허용)"
            ));
        } catch (FileSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                    "errorCode", "FILE_TOO_LARGE",
                    "message", "이미지 파일의 크기가 너무 큽니다. 최대 5MB까지 업로드 가능합니다."
            ));
        }
    }

    //프로필 이미지 삭제
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<?> deleteProfileImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Value("${qnb.profile.default-url}") String defaultProfileUrl) {

        User user = userDetails.getUser();

        // 이미 기본 이미지인 경우
        if (user.getProfileUrl() == null || defaultProfileUrl.equals(user.getProfileUrl())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "errorCode", "PROFILE_IMAGE_NOT_FOUND",
                    "message", "삭제할 프로필 이미지가 존재하지 않습니다."
            ));
        }

        try {
            // 1. 기존 이미지 S3에서 삭제
            s3Uploader.delete(user.getProfileUrl());

            // 2. DB에서 프로필 URL을 기본 이미지로 변경
            user.setProfileUrl(defaultProfileUrl);
            userRepository.save(user);

            // 3. 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                    "data", Map.of(
                            "userId", user.getUserId(),
                            "nickname", user.getUserNickname(),
                            "profileUrl", defaultProfileUrl
                    ),
                    "message", "프로필 이미지가 삭제되어 기본 이미지로 변경되었습니다."
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "errorCode", "PROFILE_DELETE_FAILED",
                    "message", "프로필 이미지 삭제 중 오류가 발생했습니다."
            ));
        }
    }

    // 수동 벡터 재생성 요청
    @PostMapping("/{userId}/generate-vector")
    public ResponseEntity<?> regenerateUserVector(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        // 이벤트 발행 → AFTER_COMMIT 리스너에서 ML 호출
        eventPublisher.publishEvent(new UserBookReadAddedEvent(userId));

        return ResponseEntity.ok(Map.of(
                "message", "유저 벡터 재생성 요청이 전송되었습니다.",
                "userId", userId
        ));
    }
}
