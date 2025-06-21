package qnb.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data

public class UserPreference {

    @Id //이 필드가 기본키임을 의미
    @GeneratedValue(strategy = GenerationType.IDENTITY) //자동으로 값 증가
    private Long id;

    @OneToOne //1:1관계 의미
    @JoinColumn(name = "user_id") //User 테이블의 user_id 참조
    private User user;

    private Integer readingAmount;

    private Integer importantFactor;

    @ElementCollection
    private List<Integer> preferredGenres;

    @ElementCollection
    private List<String> preferredKeywords;

    @ElementCollection
    @CollectionTable(name = "user_preference_book_id")
    private List<Integer> bookId; // bookId로 저장
}

