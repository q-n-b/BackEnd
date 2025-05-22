package com.example.qnb.question.dto;
//질문 Response DTO

import com.example.qnb.question.entity.Question;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class QuestionResponseDto {
    private final Integer bookId;          // 도서 ID
    private final Long userId;             // 질문 작성자 ID
    private final Integer questionId;      // 질문 ID
    private final String userNickname;     // 질문 작성자 닉네임
    private final String profileUrl;
    private final String questionContent;  // 질문 내용
    private final Integer answerCount;
    private final Integer likeCount;       // 좋아요 수
    private final Integer scrapCount;      // 스크랩 수
    private final LocalDateTime createdAt; // 생성일시

    // 생성자: Question 엔티티로부터 값 추출
    public QuestionResponseDto(Question question,int answerCount, String profileUrl) {
        this.questionId = question.getQuestionId();
        this.bookId = question.getBookId();
        this.userId = question.getUser().getUserId();          //  User 객체에서 꺼냄
        this.userNickname = question.getUser().getUserNickname(); // 닉네임도 마찬가지
        this.profileUrl = profileUrl;
        this.questionContent = question.getQuestionContent();
        this.answerCount = answerCount;
        this.likeCount = question.getLikeCount();
        this.scrapCount = question.getScrapCount();
        this.createdAt = question.getCreatedAt();
    }
}
