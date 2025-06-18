package qnb.answer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qnb.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestionId(Long questionId);

    List<Answer> findByUserId(Long userId);

    @Query("SELECT a.question.questionId, COUNT(a) " +
            "FROM Answer a " +
            "WHERE a.question.questionId IN :questionIds " +
            "GROUP BY a.question.questionId")
    List<Object[]> countAnswersByQuestionIds(@Param("questionIds") List<Integer> questionIds);



}
