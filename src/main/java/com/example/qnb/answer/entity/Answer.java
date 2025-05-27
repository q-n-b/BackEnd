package com.example.qnb.answer.entity;

import com.example.qnb.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    private Long userId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Column(nullable = false)
    private String answerState; // BEFORE, AFTER

    private int likeCount = 0;

    private LocalDateTime createdAt;

    //DB 저장 직전에 자동으로 createdAt이 세팅됨
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
