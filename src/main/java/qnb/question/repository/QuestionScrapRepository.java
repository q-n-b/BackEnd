package qnb.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qnb.question.entity.QuestionScrap;

import java.util.Optional;

public interface QuestionScrapRepository extends JpaRepository<QuestionScrap, Long> {
    Optional<QuestionScrap> findByUserIdAndQuestion_QuestionId(Long userId, Integer questionId);
}

