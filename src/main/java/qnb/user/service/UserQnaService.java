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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserQnaService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public List<UserQnaResponseDto> getUserQnaList(Long userId, String type) {
        List<UserQnaResponseDto> result = new ArrayList<>();
        Map<Long, UserQnaResponseDto> resultMap = new LinkedHashMap<>();

        // 사용자가 작성한 질문 목록
        if (type == null || type.equals("QUESTION")) {
            List<Question> myQuestions = questionRepository.findByUser_UserId(userId);

            for (Question q : myQuestions) {
                List<Answer> answers = answerRepository.findByQuestionId(Long.valueOf(q.getQuestionId()));
                resultMap.putIfAbsent(Long.valueOf(q.getQuestionId()), UserQnaResponseDto.fromQuestion(q, answers));
            }
        }

        // 사용자가 작성한 답변 목록 (중복 질문 제거)
        if (type == null || type.equals("ANSWER")) {
            List<Answer> myAnswers = answerRepository.findByUserId(userId);

            for (Answer a : myAnswers) {
                Integer questionId = a.getQuestionId().intValue();
                if (!resultMap.containsKey(questionId)) {
                    Question q = questionRepository.findById(questionId)
                            .orElseThrow(QuestionNotFoundException::new);

                    List<Answer> answers = answerRepository.findByQuestionId(Long.valueOf(questionId));
                    resultMap.put(Long.valueOf(questionId), UserQnaResponseDto.fromAnswer(q, answers));

                }
            }
        }

        if (resultMap.isEmpty()) {
            throw new QnaNotFoundException("작성한 질문 또는 답변이 없습니다.");
        }


        return new ArrayList<>(resultMap.values());
    }
}