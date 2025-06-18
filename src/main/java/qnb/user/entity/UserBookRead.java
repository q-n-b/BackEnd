package qnb.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import qnb.book.entity.Book;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_book_read")
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

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
}
