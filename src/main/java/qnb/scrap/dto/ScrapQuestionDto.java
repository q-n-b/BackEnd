package qnb.scrap.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
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

    @Getter(AccessLevel.NONE)
    private boolean isScrapped;

    @Getter(AccessLevel.NONE)
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

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

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
                // profileUrl이 null 또는 빈 문자열이면 디폴트 이미지 적용
                .profileUrl(
                        (q.getUser() != null && q.getUser().getProfileUrl() != null && !q.getUser().getProfileUrl().isEmpty())
                                ? q.getUser().getProfileUrl()
                                : DEFAULT_PROFILE_URL
                )
                .isScrapped(isScrapped)
                .isLiked(isLiked)
                .createdAt(q.getCreatedAt())
                .book(q.getBook() != null ? BookSimpleDto.from(q.getBook()) : null)
                .build();
    }

}
