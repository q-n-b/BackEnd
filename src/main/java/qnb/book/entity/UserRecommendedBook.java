package qnb.book.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import qnb.user.entity.User;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
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

    private String keyword;  // 추천 키워드

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;

    @Column(name = "score")
    private Double score; // null 허용

    protected UserRecommendedBook() {
    }

    public UserRecommendedBook(User user, Book book, String keyword) {
        this.user = user;
        this.book = book;
        this.keyword = keyword;
        this.recommendedAt = LocalDateTime.now();
    }
}
