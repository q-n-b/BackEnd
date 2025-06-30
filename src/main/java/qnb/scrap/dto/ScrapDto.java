package qnb.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.book.dto.BookSimpleDto;
import qnb.question.entity.Question;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ScrapDto {
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
    private LocalDateTime createdAt;
    private BookSimpleDto book;

    public static ScrapDto from(Question q) {
        return ScrapDto.builder()
                .questionId(q.getQuestionId().longValue())
                .questionContent(q.getQuestionContent())
                .scrapCount(q.getScrapCount())
                .likeCount(q.getLikeCount())
                .answerCount(q.getAnswerCount())
                .userId(q.getUser().getUserId())
                .userNickname(q.getUser().getUserNickname())
                .profileUrl(q.getUser().getProfileUrl())
                .isScrapped(true)             // 항상 true
                .isLiked(false)               // 항상 false 혹은 표시 안함
                .createdAt(q.getCreatedAt())
                .book(BookSimpleDto.from(q.getBook()))
                .build();
    }

}
