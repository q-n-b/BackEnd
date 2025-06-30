package qnb.like.dto;
//답변 1개에 대한 정보 DTO

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

    public static LikeAnswerDto from(Answer a) {
        return LikeAnswerDto.builder()
                .answerId(a.getAnswerId())
                .answerContent(a.getAnswerContent())
                .answerState(a.getAnswerState())
                .likeCount(a.getLikeCount())
                .userId(a.getUser().getUserId())
                .userNickname(a.getUser().getUserNickname() != null ? a.getUser().getUserNickname() : "")
                .profileUrl(a.getUser().getProfileUrl() != null ? a.getUser().getProfileUrl() : "")
                .isLiked(true)
                .createdAt(a.getCreatedAt())
                .book(BookSimpleDto.from(a.getQuestion().getBook()))
                .question(QuestionSimpleDto.from(a.getQuestion()))
                .build();
    }
}
