package qnb.question.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class QuestionListItemDto {

    private Integer questionId;
    private Integer bookId;

    private Long userId;            // 작성자(또는 GPT 시스템 사용자)
    private String userNickname;    // "사용자" / "GPT" 등
    private String profileUrl;      // null 가능

    private String questionContent;

    private int answerCount;        // 집계치
    private int likeCount;
    private int scrapCount;

    private String status;          // "READY" | "GENERATING" | "FAILED"
    private LocalDateTime createdAt;

    // 직렬화는 isLiked / isScrapped 로 맞춘다
    @JsonProperty("isLiked")
    private boolean liked;

    @JsonProperty("isScrapped")
    private boolean scrapped;

    // 정적 팩토리(서비스에서 사용하기 편하게)
    public static QuestionListItemDto of(
            Integer questionId,
            Integer bookId,
            Long userId,
            String userNickname,
            String profileUrl,
            String questionContent,
            int answerCount,
            int likeCount,
            int scrapCount,
            String status,
            LocalDateTime createdAt,
            boolean isLiked,
            boolean isScrapped
    ) {
        return QuestionListItemDto.builder()
                .questionId(questionId)
                .bookId(bookId)
                .userId(userId)
                .userNickname(userNickname)
                .profileUrl(profileUrl)
                .questionContent(questionContent)
                .answerCount(answerCount)
                .likeCount(likeCount)
                .scrapCount(scrapCount)
                .status(status)
                .createdAt(createdAt)
                .liked(isLiked)
                .scrapped(isScrapped)
                .build();
    }
}
