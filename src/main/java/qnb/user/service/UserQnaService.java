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

import java.util.*;

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
                List<Answer> answers = answerRepository.findByQuestion_QuestionId(Long.valueOf(q.getQuestionId()));
                resultMap.putIfAbsent(Long.valueOf(q.getQuestionId()), UserQnaResponseDto.fromQuestion(q, answers));
            }
        }

        // 사용자가 작성한 답변 목록 (중복 질문 제거)
        if (type == null || type.equals("ANSWER")) {
            List<Answer> myAnswers = answerRepository.findByUser_UserId(userId);

            // 상태 우선순위 정의
            Map<String, Integer> stateOrder = Map.of(
                    "BEFORE", 0,
                    "READING", 1,
                    "AFTER", 2
            );

            for (Answer a : myAnswers) {
                if (a.getQuestion() == null || a.getQuestion().getQuestionId() == null) {
                    continue;
                }

                Long questionId = a.getQuestion().getQuestionId().longValue();

                if (!resultMap.containsKey(questionId)) {
                    Question q = questionRepository.findById(questionId.intValue())
                            .orElseThrow(QuestionNotFoundException::new);

                    // 내가 쓴 답변만 추출 + 정렬
                    List<Answer> filtered = myAnswers.stream()
                            .filter(ans -> ans.getQuestion() != null &&
                                    ans.getQuestion().getQuestionId().longValue() == questionId)
                            .sorted(
                                    Comparator.comparing(
                                            (Answer ans) -> stateOrder.getOrDefault(ans.getAnswerState(), 99)
                                    ).thenComparing(Answer::getCreatedAt, Comparator.reverseOrder())
                            )
                            .toList();

                    resultMap.put(questionId, UserQnaResponseDto.fromAnswer(q, filtered));
                }
            }
        }


        if (resultMap.isEmpty()) {
            throw new QnaNotFoundException("작성한 질문 또는 답변이 없습니다.");
        }

        return new ArrayList<>(resultMap.values());
    }
}