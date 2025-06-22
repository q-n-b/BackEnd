package qnb.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer readingAmount;

    private Integer importantFactor;

    @ElementCollection
    @CollectionTable(
            name = "user_preference_genres",
            joinColumns = @JoinColumn(name = "user_preference_id")
    )
    @Column(name = "preferred_genre")
    private List<Integer> preferredGenres;

    @ElementCollection
    @CollectionTable(
            name = "user_preference_keywords",
            joinColumns = @JoinColumn(name = "user_preference_id")
    )
    @Column(name = "keyword")
    private List<String> preferredKeywords;

    @ElementCollection
    @CollectionTable(
            name = "user_preference_book_ids",
            joinColumns = @JoinColumn(name = "user_preference_id")
    )
    @Column(name = "book_id")
    private List<Integer> preferredBookId;
}
