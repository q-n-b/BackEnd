package qnb.book.repository;

//추천 도서 레포지토리

import qnb.book.entity.UserRecommendedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import qnb.user.entity.User;

import java.util.Optional;
import java.util.List;

// UserRecommendedBookRepository.java
@Repository
public interface UserRecommendedBookRepository extends JpaRepository<UserRecommendedBook, Integer> {

    // 추천 도서 중 장르 기준으로 필터링
    @Query("SELECT r FROM UserRecommendedBook r WHERE r.book.genre = :genre")
    List<UserRecommendedBook> findByBookGenre(@Param("genre") String genre);

    List<UserRecommendedBook> findAllByUserOrderByRecommendedAtDesc(User user);

    //추천받은 책 중에 가장 최근 추천 받은 책 가져오기
    Optional<UserRecommendedBook> findTopByUser_UserIdOrderByRecommendedAtDesc(Long userId);

}

