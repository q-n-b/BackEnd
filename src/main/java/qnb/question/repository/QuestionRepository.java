package qnb.question.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import qnb.question.entity.Question;
import qnb.question.repository.projection.BookQuestionCount;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByUser_UserId(Long userId);

    @Query("""
        SELECT q
        FROM Question q
        JOIN FETCH q.book b
        JOIN FETCH q.user u
        WHERE q.questionId = :id
    """)
    Optional<Question> findWithBookAndUserById(@Param("id") Integer id);

    @Query("SELECT q FROM Question q WHERE LOWER(q.questionContent) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Question> findQuestionsForSummary(@Param("keyword") String keyword);

    // 질문 검색시 사용됨
    @Query("SELECT q FROM Question q " +
            "WHERE q.questionContent LIKE %:keyword% " +
            "OR q.book.title LIKE %:keyword% " +
            "OR q.book.author LIKE %:keyword%")
    Page<Question> searchQuestions(@Param("keyword") String keyword, Pageable pageable);


    // 내가 스크랩한 질문 목록
    @Query("SELECT q FROM Question q JOIN q.scraps s WHERE s.userId = :userId")
    Page<Question> findScrappedQuestionsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 내가 좋아요한 질문 목록
    @Query("SELECT q FROM Question q JOIN q.likes l WHERE l.user.userId = :userId")
    Page<Question> findLikedQuestionsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 계정 탈퇴에서 사용되는 메소드
    void deleteByUser_UserId(Long userId);

    //도서 전체보기에서 사용되는 질문 개수 집계용
    @Query("""
        SELECT q.book.bookId AS bookId, COUNT(q) AS questionCount
        FROM Question q
        WHERE q.book.bookId IN :bookIds
        GROUP BY q.book.bookId
    """)
    List<BookQuestionCount> countByBookIds(@Param("bookIds") List<Long> bookIds);

    //-----GPT 관련 메소드-----
    // GPT 시스템 사용자 기준 단건 조회
    Optional<Question> findByBook_BookIdAndUser_UserId(Integer bookId, Long userId);

    // (생성용) 레이스 줄이기용 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Question q where q.book.bookId = :bookId and q.user.userId = :userId")
    Optional<Question> findForUpdateByBookIdAndUserId(@Param("bookId") Integer bookId, @Param("userId") Long userId);

    // (재시도용) 재시도 전용 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Question q where q.questionId = :id")
    Optional<Question> findByIdForUpdate(@Param("id") Integer id);

    // 폴링 최적화를 위한 경량 Projection
    interface QuestionStatusView {
        Integer getQuestionId();
        Question.QuestionStatus getStatus();
    }

    //현재 돌아가는 화면/정렬에서 바로 쓰는 용도 (닉네임 기반)
    // 최신순 정렬 시 GPT 우선 정렬 (닉네임 기반 - 현재 사용하고 있는 방식)
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

    // 좋아요 정렬 시 GPT 우선 정렬 (닉네임 기반 - 기존 유지)
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

    // ======GPT 우선 정렬의 userId 버전 (기존 닉네임 기반은 그대로 두고, 필요 시 이 메서드 사용)=======
    //GPT 전용 User 계정을 공식적으로 쓸 때, ID 기반으로 바꿔가는 용도
    @Query("""
    SELECT q FROM Question q
    WHERE q.book.bookId = :bookId
    ORDER BY 
        CASE WHEN q.user.userId = :gptUserId THEN 0 ELSE 1 END,
        q.createdAt DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByCreatedAtDescV2(
            @Param("bookId") Integer bookId,
            @Param("gptUserId") Long gptUserId,
            Pageable pageable
    );

    @Query("""
    SELECT q FROM Question q
    WHERE q.book.bookId = :bookId
    ORDER BY 
        CASE WHEN q.user.userId = :gptUserId THEN 0 ELSE 1 END,
        q.likeCount DESC
    """)
    Page<Question> findWithGptTopByBookIdOrderByLikeCountDescV2(
            @Param("bookId") Integer bookId,
            @Param("gptUserId") Long gptUserId,
            Pageable pageable
    );
}
