package qnb.question.repository;

import qnb.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByUser_UserId(Long userId);

    // 최신순 정렬 + GPT 우선
    //userNickname이 "GPT"인 질문은 CASE WHEN에서 0 → 가장 우선순위
    @Query("""
    SELECT q FROM Question q
    WHERE q.book.bookId = :bookId
    ORDER BY 
        CASE WHEN q.user.userNickname = 'GPT' THEN 0 ELSE 1 END, 
        q.createdAt DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByCreatedAtDesc(
            @Param("bookId") Integer bookId,
            Pageable pageable
    );

    //좋아요순 정렬+ GPT 우선
    @Query("""
    SELECT q FROM Question q
    WHERE q.book.bookId = :bookId
    ORDER BY 
        CASE WHEN q.user.userNickname = 'GPT' THEN 0 ELSE 1 END, 
        q.likeCount DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByLikeCountDesc(
            @Param("bookId") Integer bookId,
            Pageable pageable
    );

    //키워드로 질문 검색 (요약)
    @Query("SELECT q FROM Question q WHERE q.questionContent LIKE %:keyword% OR q.book.title LIKE %:keyword%")
    List<Question> findQuestionsForSummary(@Param("keyword") String keyword);

    @Query("SELECT q FROM Question q JOIN q.book b")
    Page<Question> findAllWithBook(Pageable pageable);

    //키워드로 질문 검색(full)
    @Query("SELECT q FROM Question q JOIN q.book b " +
            "WHERE q.questionContent LIKE %:keyword% " +
            "OR b.title LIKE %:keyword%")
    Page<Question> searchQuestions(@Param("keyword") String keyword, Pageable pageable);



}
