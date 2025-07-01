package qnb.scrap.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.book.dto.BookSimpleDto;
import qnb.question.entity.Question;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ScrapQuestionDto {
    private Long questionId;
    private String questionContent;
    private int scrapCount;
    private int likeCount;
    private int answerCount;
    private Long userId;
    private String userNickname;
    private String profileUrl;

    private boolean isScrapped;
    private boolean isLiked;

    @JsonGetter("isScrapped")
    public boolean getIsScrapped() {
        return isScrapped;
    }

    @JsonGetter("isLiked")
    public boolean getIsLiked() {
        return isLiked;
    }

    private LocalDateTime createdAt;
    private BookSimpleDto book;

    public static ScrapQuestionDto from(
            Question q,
            boolean isScrapped,
            boolean isLiked
    ) {
        return ScrapQuestionDto.builder()
                .questionId(q.getQuestionId() != null ? q.getQuestionId().longValue() : null)
                .questionContent(q.getQuestionContent())
                .scrapCount(q.getScrapCount())
                .likeCount(q.getLikeCount())
                .answerCount(q.getAnswerCount())
                .userId(q.getUser() != null ? q.getUser().getUserId() : null)
                .userNickname(q.getUser() != null ? q.getUser().getUserNickname() : null)
                .profileUrl(q.getUser() != null ? q.getUser().getProfileUrl() : null)
                .isScrapped(isScrapped)
                .isLiked(isLiked)
                .createdAt(q.getCreatedAt())
                .book(q.getBook() != null ? BookSimpleDto.from(q.getBook()) : null)
                .build();
    }

}
