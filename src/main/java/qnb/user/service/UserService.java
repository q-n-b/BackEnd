package qnb.user.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.File;
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
            String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();

            // 실제 저장 경로 설정 (맥북 기준 홈 디렉토리)
            String saveDir = System.getProperty("user.home") + "/profile-images/";  // 예: /Users/yourname/profile-images/

            // 디렉터리 존재하지 않으면 생성
            File directory = new File(saveDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 저장 경로에 파일 생성
            String savePath = saveDir + fileName;
            profileImage.transferTo(new File(savePath));

            // DB에 저장할 URL 경로 (프론트에서 불러올 경로 기준)
            String profileUrl = "/images/" + fileName;  // 실제로는 정적 리소스 서빙이 필요함

            user.setProfileUrl(profileUrl);
            return userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

    }

}