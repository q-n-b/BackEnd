package qnb.book.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import qnb.book.entity.UserRecommendedBook;
import qnb.recommend.dto.RecommendedPick;
import qnb.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRecommendedBookRepository extends JpaRepository<UserRecommendedBook, Integer> {

    @Query("SELECT r FROM UserRecommendedBook r WHERE r.book.genre = :genre")
    List<UserRecommendedBook> findByBookGenre(@Param("genre") String genre);

    List<UserRecommendedBook> findAllByUserOrderByRecommendedAtDesc(User user);

    Optional<UserRecommendedBook> findTopByUser_UserIdOrderByRecommendedAtDesc(Long userId);

    Optional<UserRecommendedBook> findTopByUser_UserIdAndBook_BookIdOrderByRecommendedAtDesc(
            Long userId, Integer bookId);

    /**
     * 주간 확정 후보 N건 (이미 읽은 책 제외 + 점수 우선 정렬)
     * - pageable = PageRequest.of(0, 1) 주면 1건만
     */
    @Query("""
        SELECT r
        FROM UserRecommendedBook r
        WHERE r.user.userId = :userId
          AND NOT EXISTS (
            SELECT 1
            FROM UserBookRead br
            WHERE br.user.userId = r.user.userId
              AND br.book.bookId = r.book.bookId
          )
        ORDER BY
          CASE WHEN r.score IS NULL THEN 1 ELSE 0 END,
          r.score DESC,
          r.recommendedAt DESC,
          r.book.bookId ASC
        """)
    List<UserRecommendedBook> findTopCandidateForWeekly(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 배치용: 추천 풀이 존재하는 사용자 ID 목록
    @Query("SELECT DISTINCT r.user.userId FROM UserRecommendedBook r")
    List<Long> findDistinctUserIds();

    @Modifying
    @Query("DELETE FROM UserRecommendedBook r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 이번 주(기간) 캐시 중 최고 점수 1건 (프로젝션 반환)
     * - 필드/별칭: bookId, score, recommendedAt (RecommendedPick과 동일)
     */
    @Query("""
      SELECT
        r.book.bookId     AS bookId,
        r.score           AS score,
        r.recommendedAt   AS recommendedAt
      FROM UserRecommendedBook r
      WHERE r.user.userId = :userId
        AND r.recommendedAt >= :from AND r.recommendedAt < :to
      ORDER BY
        CASE WHEN r.score IS NULL THEN 1 ELSE 0 END,
        r.score DESC,
        r.recommendedAt DESC,
        r.book.bookId ASC
      """)
    Optional<RecommendedPick> pickTopOfWeek(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * 랜덤 fallback (이번 주 캐시 중)
     * - RAND()는 dialect에 따라 function() 필요
     */
    @Query("""
      SELECT
        r.book.bookId     AS bookId,
        r.score           AS score,
        r.recommendedAt   AS recommendedAt
      FROM UserRecommendedBook r
      WHERE r.user.userId = :userId
        AND r.recommendedAt >= :from AND r.recommendedAt < :to
      ORDER BY function('RAND')
      """)
    List<RecommendedPick> pickRandomOfWeek(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
