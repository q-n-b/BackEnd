package qnb.user.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.*;
import qnb.question.repository.QuestionRepository;
import qnb.user.dto.SignupRequestDto;
import qnb.user.dto.UserInfoResponseDto;
import qnb.user.entity.User;
import qnb.user.repository.UserPreferenceRepository;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;


    //회원가입
    public User registerUser(SignupRequestDto request) {
        User user = new User();
        user.setUserEmail(request.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setName(request.getName());
        user.setUserNickname(request.getUserNickname());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfileUrl(request.getProfileUrl());
        return userRepository.save(user);
    }

    //내 정보 조회
    public UserInfoResponseDto getMyInfo(User user) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        return new UserInfoResponseDto(foundUser);
    }

    //비밀번호 변경
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(LoginRequiredException::new);

        if (!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
            throw new PasswordMismatchException();
        }

        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    //닉네임 변경
    public void changeNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.setUserNickname(newNickname);
        userRepository.save(user);
    }

    //계정 탈퇴
    @Transactional
    public void deleteUser(Long userId) {

        // 1. 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(LoginRequiredException::new);

        // 2. 연관 데이터 삭제
        userPreferenceRepository.deleteByUser_UserId(userId);
        answerRepository.deleteByUser_UserId(userId);
        questionRepository.deleteByUser_UserId(userId);

        //3. 유저 삭제
        userRepository.delete(user);
    }

    //프로필 이미지 변경
    public User updateProfileImage(Long userId, MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new InvalidImageFormatException();
        }

        String contentType = profileImage.getContentType();
        if (!Objects.equals(contentType, "image/jpeg") && !Objects.equals(contentType, "image/png")) {
            throw new InvalidImageFormatException();
        }

        if (profileImage.getSize() > (5 * 1024 * 1024)) {
            throw new FileSizeExceededException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(LoginRequiredException::new);

        try {
            // S3에 저장할 파일 이름
            String fileName = "user/profile/" + UUID.randomUUID() + "_" + profileImage.getOriginalFilename();

            // S3에 업로드 요청
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(profileImage.getInputStream(), profileImage.getSize())
            );

            // S3 URL 생성 (https://)
            String profileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;

            user.setProfileUrl(profileUrl);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

}