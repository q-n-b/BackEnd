package qnb.answer.dto;

import qnb.answer.entity.Answer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class AnswerResponseDto {
    private Long questionId;
    private Long answerId;
    private String userId;
    private String userNickname;
    private String profileUrl;
    private String answerContent;
    private String answerState;
    private int likeCount;
    private String createdAt;

    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public AnswerResponseDto(Long questionId, Answer answer, String userId, String nickname, String profileUrl) {
        this.questionId = questionId;
        this.answerId = answer.getAnswerId();
        this.userId = userId;
        this.userNickname = nickname;

        // profileUrl이 null이면 디폴트 이미지 사용
        this.profileUrl = (profileUrl != null && !profileUrl.isEmpty())
                ? profileUrl
                : DEFAULT_PROFILE_URL;

        this.answerContent = answer.getAnswerContent();
        this.answerState = answer.getAnswerState();
        this.likeCount = answer.getLikeCount();
        this.createdAt = Optional.ofNullable(answer.getCreatedAt())
                .map(LocalDateTime::toString)
                .orElse(null);
    }

    public static AnswerResponseDto from(Answer answer, String userId, String nickname, String profileUrl) {
        Long questionId = null;
        if (answer.getQuestion() != null && answer.getQuestion().getQuestionId() != null) {
            questionId = answer.getQuestion().getQuestionId().longValue();
        }

        return new AnswerResponseDto(
                questionId,
                answer,
                userId != null ? userId : "unknown",
                nickname != null ? nickname : "알 수 없음",
                profileUrl // null 여부는 생성자에서 처리
        );
    }
}
