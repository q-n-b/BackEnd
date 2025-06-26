package qnb.like.entity;

import jakarta.persistence.*;
import lombok.*;
import qnb.answer.entity.Answer;
import qnb.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_answer_like",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "answer_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAnswerLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public UserAnswerLike(User user, Answer answer) {
        this.user = user;
        this.answer = answer;
    }
}
