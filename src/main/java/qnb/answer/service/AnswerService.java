package qnb.answer.service;

import qnb.answer.dto.AnswerRequestDto;
import qnb.answer.dto.AnswerResponseDto;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.InvalidCredentialsException;
import qnb.common.exception.UnauthorizedAccessException;
import qnb.common.exception.AnswerNotFoundException;
import qnb.common.exception.UserNotFoundException;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;


    //답변 등록
    public AnswerResponseDto registerAnswer(Long questionId, Long userId, String userNickname, String profileUrl, AnswerRequestDto dto) {
        Answer answer = Answer.builder()
                .questionId(questionId)
                .userId(userId)
                .answerContent(dto.getAnswerContent())
                .answerState(dto.getAnswerState())
                .build();

        Answer saved = answerRepository.save(answer);

        Question question = questionRepository.findById(questionId.intValue())
                .orElseThrow();

        return new AnswerResponseDto(questionId, saved, userId.toString(), userNickname, profileUrl);
    }

    //답변 수정
    @Transactional
    public AnswerResponseDto updateAnswer(Long answerId, AnswerRequestDto dto, Long loginUserId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new); // 404

        if (!answer.getUserId().equals(loginUserId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        // 유효성 검사 (비어있거나 제한 위반 시)
        if (dto.getAnswerContent() == null || dto.getAnswerContent().trim().isEmpty()
                || dto.getAnswerContent().length() > 1000) {
            throw new InvalidCredentialsException(); // 400
        }

        answer.setAnswerContent(dto.getAnswerContent());
        answer.setAnswerState(dto.getAnswerState());

        User user = userRepository.findById(answer.getUserId())
                .orElseThrow(UserNotFoundException::new);


        return new AnswerResponseDto(
                answer.getQuestionId(),
                answer,
                String.valueOf(answer.getUserId()), // 또는 answer.getUser().getUserId() → String 변환
                user.getUserNickname(),
                user.getProfileUrl()
        );

    }

    //답변 삭제
    @Transactional
    public void deleteAnswer(Long answerId, Long loginUserId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new); // 404

        if (!answer.getUserId().equals(loginUserId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        answerRepository.delete(answer);

        Question question = questionRepository.findById(answer.getQuestionId().intValue())
                .orElseThrow();
    }

}