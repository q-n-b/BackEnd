package qnb.answer.repository;

import qnb.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    static List<Answer> findByQuestionId(Long questionId);

    int countByQuestionQuestionId(Long questionId);
}
