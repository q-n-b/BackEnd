package qnb.like.dto;
//질문 1개에 대한 정보 DTO
//질문 ID, 내용, 좋아요/스크랩 여부 등 하나의 질문에 대한 정보만 담음

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.book.dto.BookSimpleDto;
import qnb.question.entity.Question;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LikeQuestionDto {
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

    public static LikeQuestionDto from(Question q) {
        return LikeQuestionDto.builder()
                .questionId(q.getQuestionId().longValue())
                .questionContent(q.getQuestionContent())
                .scrapCount(q.getScrapCount())
                .likeCount(q.getLikeCount())
                .answerCount(q.getAnswerCount())
                .userId(q.getUser().getUserId())
                .userNickname(q.getUser().getUserNickname())
                .profileUrl(q.getUser().getProfileUrl())
                .isScrapped(false)          // 항상 false 혹은 표시 안함
                .isLiked(true)              // 항상 true
                .createdAt(q.getCreatedAt())
                .book(BookSimpleDto.from(q.getBook()))
                .build();
    }

}
