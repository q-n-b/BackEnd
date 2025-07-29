package qnb.answer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import qnb.like.entity.UserAnswerLike;
import qnb.question.entity.Question;
import qnb.user.entity.User;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Column(nullable = false)
    private String answerState; // BEFORE, AFTER

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "answer")
    private List<UserAnswerLike> likes = new ArrayList<>();

    @Builder
    public Answer(Question question, User user, String answerContent, String answerState) {
        this.question = question;
        this.user = user;
        this.answerContent = answerContent;
        this.answerState = answerState;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
