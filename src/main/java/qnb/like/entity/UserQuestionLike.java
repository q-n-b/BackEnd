package qnb.like.entity;

import jakarta.persistence.*;
import lombok.*;
import qnb.question.entity.Question;
import qnb.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_like",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "question_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuestionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public UserQuestionLike(User user, Question question) {
        this.user = user;
        this.question = question;
    }
}
