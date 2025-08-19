package qnb.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import qnb.book.entity.Book;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "user_book_read")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserBookRead {

    public static final String SOURCE_SURVEY = "SURVEY";
    public static final String SOURCE_READ   = "USER_ACTION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 설문조사/실제 읽은 책인지 구분
    @Column(name = "source", nullable = false, length = 20)
    private String source; // "SURVEY" or "USER_ACTION"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 빌더로 null이 넘어오더라도 최종 방어
    @PrePersist
    private void prePersist() {
        if (this.source == null || this.source.isBlank()) {
            this.source = SOURCE_READ; // DB DEFAULT와 반드시 동일하게
        }
        Objects.requireNonNull(this.user, "user must not be null");
        Objects.requireNonNull(this.book, "book must not be null");
    }

    // 편의 팩토리(서비스 레벨에서 명시적으로 쓰면 가독성↑, 회귀↓)
    public static UserBookRead ofSurvey(User user, Book book) {
        return UserBookRead.builder()
                .user(user)
                .book(book)
                .source(SOURCE_SURVEY)
                .build();
    }

    public static UserBookRead ofRead(User user, Book book) {
        return UserBookRead.builder()
                .user(user)
                .book(book)
                .source(SOURCE_READ)
                .build();
    }
}
