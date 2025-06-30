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
import qnb.scrap.dto.ScrapDto;
import qnb.scrap.dto.ScrapResponseDto;
import qnb.scrap.repository.UserQuestionScrapRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserQuestionLikeRepository userQuestionLikeRepository;
    private final UserQuestionScrapRepository userQuestionScrapRepository;


    public Object getFavorites(Long userId, String type, String target, Pageable pageable) {

        // SCRAP 조회
        if (type.equalsIgnoreCase("SCRAP")) {
            return getScraps(userId, pageable);
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

    //내가 스크랩한 질문 목록
    private ScrapResponseDto getScraps(Long userId, Pageable pageable) {
        Page<Question> scraps = questionRepository.findScrappedQuestionsByUserId(userId, pageable);

        List<ScrapDto> scrapDtos = scraps.stream()
                .map(ScrapDto::from)
                .toList();

        return new ScrapResponseDto(
                scrapDtos,
                new PageInfoDto(
                        scraps.getNumber() + 1,
                        scraps.getTotalPages(),
                        scraps.getTotalElements()
                )
        );
    }


    //내가 좋아요한 질문 목록
    private LikeQuestionsResponseDto getLikedQuestions(Long userId, Pageable pageable) {
        Page<Question> likes = questionRepository.findLikedQuestionsByUserId(userId, pageable);

        List<LikeQuestionDto> likeDtos = likes.stream()
                .map(LikeQuestionDto::from)
                .toList();

        return new LikeQuestionsResponseDto(
                likeDtos,
                new PageInfoDto(
                        likes.getNumber() + 1,
                        likes.getTotalPages(),
                        likes.getTotalElements()
                )
        );
    }

    //내가 좋아요한 답변 목록
    private LikeAnswersResponseDto getLikedAnswers(Long userId, Pageable pageable) {
        Page<Answer> likes = answerRepository.findLikedAnswersByUserId(userId, pageable);

        List<LikeAnswerDto> likeDtos = likes.stream()
                .map(LikeAnswerDto::from)
                .toList();

        return new LikeAnswersResponseDto(
                new LikeAnswersResponseDto.Likes(likeDtos),
                new PageInfoDto(
                likes.getNumber() + 1,
                likes.getTotalPages(),
                likes.getTotalElements()
        )
        );
    }
}
