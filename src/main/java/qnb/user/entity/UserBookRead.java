package qnb.user.entity;

import jakarta.persistence.*;
import lombok.*;
import qnb.book.entity.Book;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_book_read")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserBookRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    //설문조사/ 실제 읽은 책인지 구분
    @Column(nullable = false)
    private String source; // SURVEY or READ

    private LocalDateTime createdAt;
}
