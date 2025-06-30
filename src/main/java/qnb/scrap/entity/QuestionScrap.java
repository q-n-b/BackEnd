package qnb.scrap.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import qnb.question.entity.Question;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_scrap",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "question_id"})})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private LocalDateTime createdAt = LocalDateTime.now();
}
