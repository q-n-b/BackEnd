package qnb.like.dto;
//답변 1개에 대한 정보 DTO

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.answer.entity.Answer;
import qnb.book.dto.BookSimpleDto;
import qnb.question.dto.QuestionSimpleDto;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LikeAnswerDto {

    private Long answerId;
    private String answerContent;
    private String answerState;
    private int likeCount;
    private Long userId;
    private String userNickname;
    private String profileUrl;
    private LocalDateTime createdAt;
    private BookSimpleDto book;
    private QuestionSimpleDto question;

    @Getter(AccessLevel.NONE)
    private boolean isLiked;

    @JsonProperty("isLiked")
    public boolean getIsLiked() {
        return isLiked;
    }

    public static LikeAnswerDto from(
            Answer answer,
            boolean isLiked
    ) {
        return LikeAnswerDto.builder()
                .answerId(answer.getAnswerId())
                .answerContent(answer.getAnswerContent())
                .answerState(answer.getAnswerState())
                .likeCount(answer.getLikeCount())
                .userId(answer.getUser() != null ? answer.getUser().getUserId() : null)
                .userNickname(answer.getUser() != null ? answer.getUser().getUserNickname() : null)
                .profileUrl(answer.getUser() != null ? answer.getUser().getProfileUrl() : null)
                .isLiked(isLiked)
                .createdAt(answer.getCreatedAt())
                .book(answer.getQuestion() != null && answer.getQuestion().getBook() != null ? BookSimpleDto.from(answer.getQuestion().getBook()) : null)
                .question(answer.getQuestion() != null ? QuestionSimpleDto.from(answer.getQuestion()) : null)
                .build();
    }

}
