package qnb.user.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qnb.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import qnb.user.entity.UserBookRead;
import qnb.user.entity.UserBookReading;
import qnb.user.entity.UserBookWish;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByUserEmail(String userEmail);

    //추천 도서 재로딩 시 이번주 캐시가 있는 사용자만 추출
    @Query("""
  SELECT DISTINCT r.user.userId
  FROM UserRecommendedBook r
  WHERE r.user.hasReadingTaste = true
    AND r.recommendedAt >= :from
    AND r.recommendedAt <  :to
""")
    List<Long> findEligibleUserIdsForThisWeek(@Param("from") LocalDateTime from,
                                              @Param("to")   LocalDateTime to);

}
