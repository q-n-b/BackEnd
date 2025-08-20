package qnb.answer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.answer.entity.Answer;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
public class AnswerListItemDto {
    private Long questionId;
    private Long answerId;
    private String userId;
    private String userNickname;
    private String profileUrl;
    private String answerContent;
    private String answerState;
    private int likeCount;
    private String createdAt;

    @JsonProperty("isLiked")
    private boolean liked;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public static AnswerListItemDto of(Answer answer,
                                       String userId,
                                       String nickname,
                                       String profileUrl,
                                       boolean isLiked) {
        Long qid = null;
        if (answer.getQuestion() != null && answer.getQuestion().getQuestionId() != null) {
            qid = answer.getQuestion().getQuestionId().longValue();
        }
        return AnswerListItemDto.builder()
                .questionId(qid)
                .answerId(answer.getAnswerId())
                .userId(userId != null ? userId : "unknown")
                .userNickname(nickname != null ? nickname : "알 수 없음")
                .profileUrl((profileUrl != null && !profileUrl.isEmpty()) ?
                        profileUrl : DEFAULT_PROFILE_URL)
                .answerContent(answer.getAnswerContent())
                .answerState(answer.getAnswerState())
                .likeCount(answer.getLikeCount())
                .createdAt(Optional.ofNullable(answer.getCreatedAt()).
                        map(LocalDateTime::toString).orElse(null))
                .liked(isLiked)
                .build();
    }
}
