package qnb.recommend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "user_weekly_featured_book",
        uniqueConstraints = @UniqueConstraint(name="ux_user_week", columnNames = {"user_id","week_start_date"}))
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserWeeklyFeaturedBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="book_id", nullable=false)
    private Long bookId;

    @Column(name="week_start_date", nullable=false)
    private LocalDate weekStartDate; // 월요일

    @Column(name="score")
    private Double score;

    @Column(name="source")
    private String source; // 'TOP_OF_WEEK' or 'FALLBACK_RANDOM'

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
}

