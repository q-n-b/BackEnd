package qnb.question.repository;

import qnb.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByUser_UserId(Long userId);

    //최신순 정렬 시 GPT 우선 정렬
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

    //좋아요 정렬 시 GPT 우선 정렬
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

    @Query("SELECT q FROM Question q WHERE LOWER(q.questionContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Question> findQuestionsForSummary(@Param("keyword") String keyword);

    @Query("SELECT q FROM Question q WHERE LOWER(q.questionContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> searchQuestions(@Param("keyword") String keyword, Pageable pageable);

    //내가 스크랩한 질문 목록
    @Query("SELECT q FROM Question q JOIN q.scraps s WHERE s.userId = :userId")
    Page<Question> findScrappedQuestionsByUserId(@Param("userId") Long userId, Pageable pageable);

    //내가 좋아요한 질문 목록
    @Query("SELECT q FROM Question q JOIN q.likes l WHERE l.user.id = :userId")
    Page<Question> findLikedQuestionsByUserId(@Param("userId") Long userId, Pageable pageable);

    //계정 탈퇴에서 사용되는 메소드
    void deleteByUser_UserId(Long userId);
}
