package qnb.answer.entity;

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

    @Column(name = "user_id")
    private Long userId;  // 아무 어노테이션도 붙이지 마

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Column(nullable = false)
    private String answerState; // BEFORE, AFTER

    private int likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
