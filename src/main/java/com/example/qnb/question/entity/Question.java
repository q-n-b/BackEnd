package com.example.qnb.question.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment
    private Integer questionId;

    @Column(nullable = false)
    private Integer bookId; // 도서 ID (Book과의 연관관계 설정 가능)

    @Column(nullable = false)
    private Long userId; // 질문을 등록한 사용자 ID

    @Column(nullable = false)
    private String userNickname; //사용자 닉네임

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionContent; // 질문 내용

    private Integer likeCount; // 좋아요 수

    private Integer scrapCount; // 스크랩 수

    private LocalDateTime createdAt; // 작성일시
}