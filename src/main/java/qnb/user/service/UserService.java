package qnb.user.service;

import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.LoginRequiredException;
import qnb.common.exception.PasswordMismatchException;
import qnb.question.repository.QuestionRepository;
import qnb.user.dto.SignupRequestDto;
import qnb.user.dto.UserInfoResponseDto;
import qnb.user.entity.User;
import qnb.user.entity.UserPreference;
import qnb.user.repository.UserPreferenceRepository;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import qnb.common.exception.UserNotFoundException;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;


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
}