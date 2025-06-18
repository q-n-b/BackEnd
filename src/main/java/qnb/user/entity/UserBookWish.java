package qnb.user.entity;

import jakarta.persistence.*;
import qnb.book.entity.Book;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_book_wish")
public class UserBookWish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    private LocalDateTime createdAt;

}