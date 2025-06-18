package qnb.user.entity;

import jakarta.persistence.*;
import qnb.book.entity.Book;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_book_reading")
public class UserBookReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
}