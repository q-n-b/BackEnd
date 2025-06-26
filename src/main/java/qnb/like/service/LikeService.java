package qnb.like.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.common.exception.InvalidLikeTypeException;
import qnb.common.exception.TargetNotFoundException;
import qnb.like.dto.LikeResponseDto;
import qnb.like.entity.UserAnswerLike;
import qnb.like.entity.UserQuestionLike;
import qnb.like.repository.UserAnswerLikeRepository;
import qnb.like.repository.UserQuestionLikeRepository;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserQuestionLikeRepository userQuestionLikeRepository;
    private final UserAnswerLikeRepository userAnswerLikeRepository;

    @Transactional
    public LikeResponseDto toggleLike(Long userId, String type, Long targetId) {

        if (type == null) {
            throw new InvalidLikeTypeException();
        }

        //사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(TargetNotFoundException::new);

        // 타입에 따라 각각 처리
        return switch (type.toUpperCase()) {
            case "QUESTION" -> toggleQuestionLike(user, targetId);
            case "ANSWER" -> toggleAnswerLike(user, targetId);
            //지원하지 않는 타입
            default -> throw new InvalidLikeTypeException();
        };
    }

    //질문 좋아요 토글 처리
    private LikeResponseDto toggleQuestionLike(User user, Long questionId) {
        Question question = questionRepository.findById(Math.toIntExact(questionId))
                .orElseThrow(TargetNotFoundException::new);

        boolean alreadyLiked = userQuestionLikeRepository.existsByUser_UserIdAndQuestion_QuestionId(
                user.getUserId(), question.getQuestionId());

        if (alreadyLiked) {
            // 이미 좋아요 한 경우 → 취소
            userQuestionLikeRepository.deleteByUser_UserIdAndQuestion_QuestionId(user.getUserId(),
                    question.getQuestionId());

            // 좋아요 수 감소
            question.decreaseLikeCount();
            return new LikeResponseDto("QUESTION", questionId, question.getLikeCount(), false);
        }

        else {
            // 처음 좋아요 누른 경우
            userQuestionLikeRepository.save(UserQuestionLike.builder().user(user).question(question).build());

            //좋아요 수 증가
            question.increaseLikeCount();
            return new LikeResponseDto("QUESTION", questionId, question.getLikeCount(), true);
        }
    }

    //답변 좋아요 토글 처리
    private LikeResponseDto toggleAnswerLike(User user, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(TargetNotFoundException::new);

        boolean alreadyLiked = userAnswerLikeRepository.existsByUser_UserIdAndAnswer_AnswerId(
                user.getUserId(), answer.getAnswerId().intValue());

        if (alreadyLiked) {
            // 이미 좋아요 한 경우 → 취소
            userAnswerLikeRepository.deleteByUser_UserIdAndAnswer_AnswerId(user.getUserId(),
                    answer.getAnswerId().intValue());

            // 좋아요 수 감소
            answer.decreaseLikeCount();
            return new LikeResponseDto("ANSWER", answerId, answer.getLikeCount(), false);
        }

        else {
            // 처음 좋아요 누른 경우
            userAnswerLikeRepository.save(UserAnswerLike.builder().user(user).answer(answer).build());

            // 좋아요 수 증가
            answer.increaseLikeCount();
            return new LikeResponseDto("ANSWER", answerId, answer.getLikeCount(), true);
        }
    }
}
