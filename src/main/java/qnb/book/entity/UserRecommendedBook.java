package qnb.book.entity;

import jakarta.persistence.*;
import lombok.Data;
import qnb.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_recommended_book")
public class UserRecommendedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id")
    private Integer recommendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 도서 연관관계 설정 (Book과 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String reason;   // 추천 이유 (예: question, search)
    private String keyword;  // 추천 키워드 (예: 아포칼립스)

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;

    protected UserRecommendedBook() {
    }

    public UserRecommendedBook(User user, Book book, String reason, String keyword) {
        this.user = user;
        this.book = book;
        this.reason = reason;
        this.keyword = keyword;
        this.recommendedAt = LocalDateTime.now();
    }

}
