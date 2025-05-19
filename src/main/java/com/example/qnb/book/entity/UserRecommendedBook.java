package com.example.qnb.book.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_recommended_book") // 테이블 이름 오타 수정
public class UserRecommendedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    private Integer recommendId;

    // 도서 연관관계 설정 (Book과 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String reason;   // 추천 이유 (예: question, search)
    private String keyword;  // 추천 키워드 (예: 아포칼립스)

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;
}
