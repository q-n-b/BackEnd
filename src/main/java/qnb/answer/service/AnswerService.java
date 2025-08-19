package qnb.answer.service;

import lombok.extern.slf4j.Slf4j;
import qnb.answer.dto.AnswerRequestDto;
import qnb.answer.dto.AnswerResponseDto;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.InvalidCredentialsException;
import qnb.common.exception.UnauthorizedAccessException;
import qnb.common.exception.AnswerNotFoundException;
import qnb.common.exception.UserNotFoundException;
import qnb.common.exception.QuestionNotFoundException;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    //답변 등록
    public AnswerResponseDto registerAnswer(Long questionId, Long userId, String userNickname, String profileUrl, AnswerRequestDto dto) {
        // 질문 조회
        Question question = questionRepository.findById(questionId.intValue())
                .orElseThrow(QuestionNotFoundException::new);

        // 사용자 조회 추가
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 답변 생성
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setUser(user);
        answer.setAnswerContent(dto.getAnswerContent());
        answer.setAnswerState(dto.getAnswerState());

        // 로그 추가
        log.info("answer createdAt BEFORE save: {}", answer.getCreatedAt());

        // 저장
        Answer saved = answerRepository.save(answer);

        // 로그 추가
        log.info("answer createdAt AFTER save: {}", saved.getCreatedAt());

        // 질문의 답변 수 증가
        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);

        // 응답 반환
        return new AnswerResponseDto(questionId, saved, userId.toString(), userNickname, profileUrl);
    }

    //답변 수정
    @Transactional
    public AnswerResponseDto updateAnswer(Long answerId, AnswerRequestDto dto, Long loginUserId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new); // 404

        if (!answer.getUser().getUserId().equals(loginUserId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        // 유효성 검사 (비어있거나 제한 위반 시)
        if (dto.getAnswerContent() == null || dto.getAnswerContent().trim().isEmpty()
                || dto.getAnswerContent().length() > 1000) {
            throw new InvalidCredentialsException(); // 400
        }
        answer.setAnswerContent(dto.getAnswerContent());
        answer.setAnswerState(dto.getAnswerState());

        User user = userRepository.findById(answer.getUser().getUserId())
                .orElseThrow(UserNotFoundException::new);

        answer.getQuestion().getQuestionId();

        return new AnswerResponseDto(
                answer.getQuestion().getQuestionId().longValue(),
                answer,
                String.valueOf(answer.getUser().getUserId()), // 또는 answer.getUser().getUserId() → String 변환
                user.getUserNickname(),
                user.getProfileUrl()
        );

    }

    //답변 삭제
    @Transactional
    public void deleteAnswer(Long answerId, Long loginUserId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new); // 404

        if (!answer.getUser().getUserId().equals(loginUserId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        Question question = questionRepository.findById(answer.getQuestion().getQuestionId())
                .orElseThrow(QuestionNotFoundException::new);

        //질문 답변 수 감소
        question.setAnswerCount(Math.max(0, question.getAnswerCount() - 1));
        questionRepository.save(question);

        //응답 반환
        answerRepository.delete(answer);
    }

}