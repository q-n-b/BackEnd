package qnb.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.QuestionNotFoundException;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.user.dto.UserQnaResponseDto;
import qnb.common.exception.QnaNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQnaService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public List<UserQnaResponseDto> getUserQnaList(Long userId, String type) {
        List<UserQnaResponseDto> result = new ArrayList<>();

        if (type == null || type.equals("QUESTION")) {
            List<Question> myQuestions = questionRepository.findByUser_UserId(userId);

            for (Question q : myQuestions) {
                List<Answer> answers = answerRepository.findByQuestionId(Long.valueOf(q.getQuestionId()));
                result.add(UserQnaResponseDto.fromQuestion(q, answers));
            }
        }

        if (type == null || type.equals("ANSWER")) {
            List<Answer> myAnswers = answerRepository.findByUserId(userId);

            for (Answer a : myAnswers) {
                Question q = questionRepository.findById(Math.toIntExact(a.getQuestionId()))

                        .orElseThrow(QuestionNotFoundException::new);

                List<Answer> answers = answerRepository.findByQuestionId(Long.valueOf(q.getQuestionId()));

                result.add(UserQnaResponseDto.fromAnswer(q, answers));
            }
        }

        if (result.isEmpty()) {
            throw new QnaNotFoundException("작성한 질문 또는 답변이 없습니다.");
        }

        return result;
    }
}