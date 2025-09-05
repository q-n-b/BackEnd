package qnb.like.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
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
public class LikeQuestionDto {
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

    // ✅ 추가된 필드
    private String status;

    private BookSimpleDto book;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public static LikeQuestionDto from(
            Question q,
            boolean isScrapped,
            boolean isLiked
    ) {
        return LikeQuestionDto.builder()
                .questionId(q.getQuestionId() != null ? q.getQuestionId().longValue() : null)
                .questionContent(q.getQuestionContent())
                .scrapCount(q.getScrapCount())
                .likeCount(q.getLikeCount())
                .answerCount(q.getAnswerCount())
                .userId(q.getUser() != null ? q.getUser().getUserId() : null)
                .userNickname(q.getUser() != null ? q.getUser().getUserNickname() : null)
                .profileUrl(
                        (q.getUser() != null && q.getUser().getProfileUrl() != null && !q.getUser().getProfileUrl().isEmpty())
                                ? q.getUser().getProfileUrl()
                                : DEFAULT_PROFILE_URL
                )
                .isScrapped(isScrapped)
                .isLiked(isLiked)
                .createdAt(q.getCreatedAt())
                .status(q.getStatus() != null ? q.getStatus().name() : null)
                .book(q.getBook() != null ? BookSimpleDto.from(q.getBook()) : null)
                .build();
    }
}
