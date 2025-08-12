package qnb.book.repository;

//추천 도서 레포지토리

import org.springframework.data.jpa.repository.Modifying;
import qnb.book.entity.UserRecommendedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import qnb.user.entity.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRecommendedBookRepository extends JpaRepository<UserRecommendedBook, Integer> {

    @Query("SELECT r FROM UserRecommendedBook r WHERE r.book.genre = :genre")
    List<UserRecommendedBook> findByBookGenre(@Param("genre") String genre);

    List<UserRecommendedBook> findAllByUserOrderByRecommendedAtDesc(User user);

    Optional<UserRecommendedBook> findTopByUser_UserIdOrderByRecommendedAtDesc(Long userId);

    Optional<UserRecommendedBook> findTopByUser_UserIdAndBook_BookIdOrderByRecommendedAtDesc(
            Long userId, Integer bookId);

    // 주간 확정 후보 1권 (이미 읽은 책 제외 + 최신순)
    @Query("""
        SELECT r FROM UserRecommendedBook r
        WHERE r.user.userId = :userId
          AND NOT EXISTS (
            SELECT 1 FROM UserBookRead br
            WHERE br.user.userId = r.user.userId
              AND br.book.bookId = r.book.bookId
          )
        ORDER BY r.recommendedAt DESC, r.recommendId DESC
        """)
    List<UserRecommendedBook> findTopCandidateForWeekly(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    // 배치용: 추천 풀이 존재하는 사용자 ID 목록
    @Query("SELECT DISTINCT r.user.userId FROM UserRecommendedBook r")
    List<Long> findDistinctUserIds();

    @Modifying
    @Query("DELETE FROM UserRecommendedBook r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}


