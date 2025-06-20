package qnb.question.entity;

import qnb.book.entity.Book;
import qnb.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question")
public class Question {

    //@Column(nullable = false)
    //private Integer bookId; // 도서 ID (Book과의 연관관계 설정 가능)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment
    private Integer questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; //userId,userNikckname,profileurl 대체

    @Column(columnDefinition = "TEXT")
    private String questionContent; // 질문 내용

    //안정적으로 사용하기 위해 0으로 초기화
    private Integer likeCount=0; // 좋아요 수

    private Integer scrapCount=0; // 스크랩 수

    @CreationTimestamp //작성일시 save()할 때 자동으로 시간 넣어주는 어노테이션
    private LocalDateTime createdAt; // 작성일시

    @Column(name = "answer_count", nullable = false)
    private int answerCount = 0;

}