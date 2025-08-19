package qnb.question.dto;

import qnb.book.dto.BookResponseDto;
import qnb.question.entity.Question;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QuestionResponseDto {
    private final BookResponseDto book;    // 도서
    private final Long userId;             // 질문 작성자 ID
    private final Integer questionId;      // 질문 ID
    private final String userNickname;     // 질문 작성자 닉네임
    private final String profileUrl;       // 사용자 프로필
    private final String questionContent;  // 질문 내용
    private final Integer answerCount;     // 답변 수
    private final Integer likeCount;       // 좋아요 수
    private final Integer scrapCount;      // 스크랩 수
    private final Question.QuestionStatus status;   // 질문 상태 (READY/GENERATING/FAILED)
    private final LocalDateTime createdAt; // 생성일시


    // 디폴트 프로필 이미지 S3 URL
    private static final String DEFAULT_PROFILE_URL =
            "https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/default/profile.jpeg";

    public QuestionResponseDto(
            BookResponseDto book, Long userId, Integer questionId,
            String userNickname, String profileUrl, String questionContent,
            Integer answerCount, Integer likeCount, Integer scrapCount, Question.QuestionStatus status,
            LocalDateTime createdAt) {

        this.book = book;
        this.userId = userId;
        this.questionId = questionId;
        this.userNickname = userNickname;
        // profileUrl이 null이거나 빈 문자열이면 디폴트 이미지 사용
        this.profileUrl = (profileUrl != null && !profileUrl.isEmpty())
                ? profileUrl
                : DEFAULT_PROFILE_URL;
        this.questionContent = questionContent;
        this.answerCount = answerCount;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static QuestionResponseDto from(Question question, int answerCount) {
        return new QuestionResponseDto(
                BookResponseDto.from(question.getBook()),
                question.getUser().getUserId(),
                question.getQuestionId(),
                question.getUser().getUserNickname(),
                question.getUser().getProfileUrl(), // 생성자에서 디폴트 처리
                question.getQuestionContent(),
                answerCount,
                question.getLikeCount(),
                question.getScrapCount(),
                question.getStatus(),
                question.getCreatedAt()
        );
    }
}