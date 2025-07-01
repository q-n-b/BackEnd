package qnb.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.common.dto.PageInfoDto;
import qnb.common.exception.InvalidQueryParameterException;
import qnb.like.dto.LikeAnswerDto;
import qnb.like.dto.LikeAnswersResponseDto;
import qnb.like.dto.LikeQuestionDto;
import qnb.like.dto.LikeQuestionsResponseDto;
import qnb.like.repository.UserAnswerLikeRepository;
import qnb.like.repository.UserQuestionLikeRepository;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.scrap.dto.ScrapQuestionDto;
import qnb.scrap.dto.ScrapQuestionsResponseDto;
import qnb.scrap.repository.UserQuestionScrapRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserQuestionLikeRepository userQuestionLikeRepository;
    private final UserQuestionScrapRepository userQuestionScrapRepository;
    private final UserAnswerLikeRepository userAnswerLikeRepository;



    public Object getFavorites(Long userId, String type, String target, Pageable pageable) {

        // SCRAP 조회
        if (type.equalsIgnoreCase("SCRAP")) {
            return getScrapQuestions(userId, pageable);
        }

        // LIKE 조회
        else if (type.equalsIgnoreCase("LIKE")) {
            if (target == null) {
                throw new InvalidQueryParameterException("target 파라미터가 필요합니다.");
            }

            if (target.equalsIgnoreCase("QUESTION")) {
                return getLikedQuestions(userId, pageable);
            } else if (target.equalsIgnoreCase("ANSWER")) {
                return getLikedAnswers(userId, pageable);
            } else {
                throw new InvalidQueryParameterException("유효하지 않은 target 값입니다.");
            }
        }

        // 잘못된 type 값
        else {
            throw new InvalidQueryParameterException("유효하지 않은 type 값입니다.");
        }
    }

    //내가 스크랩한 질문 목록 조회
    private ScrapQuestionsResponseDto getScrapQuestions(Long userId, Pageable pageable) {
        Page<Question> scraps = questionRepository.findScrappedQuestionsByUserId(userId, pageable);

        List<ScrapQuestionDto> dtos = scraps.stream()
                .map(q -> {
                    boolean isScrapped = userQuestionScrapRepository.existsByUserIdAndQuestion_QuestionId(
                            userId, q.getQuestionId());

                    boolean isLiked = userQuestionLikeRepository.existsByUser_UserIdAndQuestion_QuestionId(
                            userId, q.getQuestionId());

                    return ScrapQuestionDto.from(q, isScrapped, isLiked);
                })
                .toList();

        return new ScrapQuestionsResponseDto(
                dtos,
                new PageInfoDto(
                        scraps.getNumber() + 1,
                        scraps.getTotalPages(),
                        scraps.getTotalElements()
                )
        );
    }


    //내가 좋아요한 질문 목록 조회
    private LikeQuestionsResponseDto getLikedQuestions(Long userId, Pageable pageable) {
        Page<Question> likes = questionRepository.findLikedQuestionsByUserId(userId, pageable);

        //  각 Question에 대해 isLiked, isScrapped 여부 확인하고 DTO로 매핑
        List<LikeQuestionDto> dtos = likes.stream()
                .map(q -> {

                    // 좋아요 여부
                    boolean isLiked = userQuestionLikeRepository.existsByUser_UserIdAndQuestion_QuestionId(userId, q.getQuestionId());
                    // 스크랩 여부
                    boolean isScrapped = userQuestionScrapRepository.existsByUserIdAndQuestion_QuestionId(userId, q.getQuestionId());

                    // DTO 생성
                    return LikeQuestionDto.from(q, isLiked, isScrapped);
                })
                .toList();

        return new LikeQuestionsResponseDto(
                new LikeQuestionsResponseDto.Likes(dtos),
                new PageInfoDto(
                        likes.getNumber() + 1,
                        likes.getTotalPages(),
                        likes.getTotalElements()
                )
        );
    }

    //내가 좋아요한 답변 목록 조회
    private LikeAnswersResponseDto getLikedAnswers(Long userId, Pageable pageable) {
        Page<Answer> likes = answerRepository.findLikedAnswersByUserId(userId, pageable);

        List<LikeAnswerDto> answerDtos = likes.stream()
                .map(answer -> {
                    boolean isLiked = userAnswerLikeRepository.existsByUser_UserIdAndAnswer_AnswerId(
                            userId, answer.getAnswerId().intValue());
                    return LikeAnswerDto.from(answer, isLiked);
                })
                .toList();

        return new LikeAnswersResponseDto(
                new LikeAnswersResponseDto.Likes(answerDtos),
                new PageInfoDto(
                        likes.getNumber() + 1,
                        likes.getTotalPages(),
                        likes.getTotalElements()
                )
        );
    }

}
