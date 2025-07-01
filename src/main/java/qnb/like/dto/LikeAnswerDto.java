package qnb.like.dto;
//답변 1개에 대한 정보 DTO

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private boolean isLiked;
    private LocalDateTime createdAt;
    private BookSimpleDto book;
    private QuestionSimpleDto question;

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
                .userId(answer.getUser().getUserId())
                .userNickname(answer.getUser().getUserNickname())
                .profileUrl(answer.getUser().getProfileUrl())
                .isLiked(isLiked)
                .createdAt(answer.getCreatedAt())
                .book(BookSimpleDto.from(answer.getQuestion().getBook()))
                .question(QuestionSimpleDto.from(answer.getQuestion()))
                .build();
    }
}
