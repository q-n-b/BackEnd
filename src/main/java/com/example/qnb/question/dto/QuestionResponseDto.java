package com.example.qnb.question.dto;

import com.example.qnb.book.dto.BookResponseDto;
import com.example.qnb.question.entity.Question;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class QuestionResponseDto {
    private final BookResponseDto book;        // 도서
    private final Long userId;             // 질문 작성자 ID
    private final Integer questionId;      // 질문 ID
    private final String userNickname;     // 질문 작성자 닉네임
    private final String profileUrl;
    private final String questionContent;  // 질문 내용
    private final Integer answerCount;
    private final Integer likeCount;       // 좋아요 수
    private final Integer scrapCount;      // 스크랩 수
    private final LocalDateTime createdAt; // 생성일시

    public QuestionResponseDto(
            BookResponseDto book, Long userId, Integer questionId,
            String userNickname, String profileUrl, String questionContent,
            Integer answerCount, Integer likeCount, Integer scrapCount,
            LocalDateTime createdAt) {

        this.book = book;
        this.userId = userId;
        this.questionId = questionId;
        this.userNickname = userNickname;
        this.profileUrl = profileUrl;
        this.questionContent = questionContent;
        this.answerCount = answerCount;
        this.likeCount = likeCount;
        this.scrapCount = scrapCount;
        this.createdAt = createdAt;
    }

    public static QuestionResponseDto from(Question question, int answerCount) {
        return new QuestionResponseDto(
                BookResponseDto.from(question.getBook()),
                question.getUser().getUserId(),
                question.getQuestionId(),
                question.getUser().getUserNickname(),
                question.getUser().getProfileUrl(),
                question.getQuestionContent(),
                answerCount,
                question.getLikeCount(),
                question.getScrapCount(),
                question.getCreatedAt()
        );
    }
}




