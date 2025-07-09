package qnb.answer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qnb.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestion_QuestionId(Long questionId);

    List<Answer> findByUser_UserId(Long userId);

    int countByQuestion_QuestionId(Long questionId);

    @Query("SELECT a.question.questionId, COUNT(a) " +
            "FROM Answer a " +
            "WHERE a.question.questionId IN :questionIds " +
            "GROUP BY a.question.questionId")
    List<Object[]> countAnswersByQuestionIds(@Param("questionIds") List<Integer> questionIds);

    //키워드로 답변 검색(요약 버전)
    @Query(value = "SELECT a FROM Answer a WHERE LOWER(a.answerContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Answer> findAnswersForSummary(@Param("keyword") String keyword);

    //키워드로 답변 검색 (full 버전)
    @Query("SELECT a FROM Answer a WHERE LOWER(a.answerContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Answer> searchAnswers(@Param("keyword") String keyword, Pageable pageable);

    //내가 좋아요한 답변 목록
    @Query("SELECT a FROM Answer a JOIN a.likes l WHERE l.user.id = :userId")
    Page<Answer> findLikedAnswersByUserId(@Param("userId") Long userId, Pageable pageable);

}
