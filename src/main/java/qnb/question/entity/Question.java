package qnb.question.entity;

import qnb.book.entity.Book;
import qnb.like.entity.UserQuestionLike;
import qnb.scrap.entity.QuestionScrap;
import qnb.user.entity.User;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import qnb.question.model.QuestionStatus; // 새 enum 추가

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(
        name = "question",
        indexes = {
                @Index(name = "idx_question_book", columnList = "book_id")
        },
        // GPT 시스템 사용자와의 조합을 유일하게(한 도서당 GPT 질문 1개)
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_question_book_user", columnNames = {"book_id", "user_id"})
        }
)
public class Question {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // GPT 시스템 사용자 포함
    private User user;

    @Column(columnDefinition = "TEXT")
    private String questionContent; // 생성 완료(READY) 시 채움

    // 생성 상태: GENERATING / READY / FAILED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuestionStatus status = QuestionStatus.READY; // 기본값(기존 데이터 호환)

    //안정적으로 사용하기 위해 0으로 초기화
    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer scrapCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "question")
    private List<QuestionScrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "question")
    private List<UserQuestionLike> likes = new ArrayList<>();

    @Column(name = "answer_count", nullable = false)
    private int answerCount = 0;

    // == 편의 메서드 ==
    public void markGenerating() { this.status = QuestionStatus.GENERATING; }
    public void markReady(String content) { this.questionContent = content; this.status = QuestionStatus.READY; }
    public void markFailed() { this.status = QuestionStatus.FAILED; }

    public void increaseScrapCount() { this.scrapCount++; }
    public void decreaseScrapCount() { if (this.scrapCount > 0) this.scrapCount--; }
    public void increaseLikeCount() { this.likeCount++; }
    public void decreaseLikeCount() { if (this.likeCount > 0) this.likeCount--; }
}
