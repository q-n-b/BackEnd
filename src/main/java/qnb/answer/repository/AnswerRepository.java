package qnb.answer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //키워드로 답변 검색(요약 버전)
    @Query(value = "SELECT a FROM Answer a " +
            "JOIN a.question q " +
            "JOIN q.book b " +
            "WHERE a.answerContent LIKE %:keyword% OR b.title LIKE %:keyword%")
    List<Answer> findAnswersForSummary(@Param("keyword") String keyword);

    //키워드로 답변 검색 (full 버전)
    @Query("SELECT a FROM Answer a " +
            "WHERE a.answerContent LIKE %:keyword% " +
            "OR a.question.questionContent LIKE %:keyword% " +
            "OR a.question.book.title LIKE %:keyword%")
    Page<Answer> searchAnswers(@Param("keyword") String keyword, Pageable pageable);


}
