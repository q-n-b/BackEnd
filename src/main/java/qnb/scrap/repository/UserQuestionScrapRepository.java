package qnb.scrap.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qnb.scrap.entity.QuestionScrap;

import java.util.Optional;

public interface UserQuestionScrapRepository extends JpaRepository<QuestionScrap, Long> {
    Optional<QuestionScrap> findByUserIdAndQuestion_QuestionId(Long userId, Integer questionId);

    //존재 여부만 true/false로 반환하는 메소드
    boolean existsByUserIdAndQuestion_QuestionId(Long userId, Integer questionId);

    //삭제하는 메소드
    void deleteByUserIdAndQuestion_QuestionId(Long userId, Integer questionId);
}

