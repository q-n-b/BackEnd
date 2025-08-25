package qnb.recommend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qnb.recommend.entity.UserWeeklyFeaturedBook;

import java.time.LocalDate;
import java.util.Optional;

public interface UserWeeklyFeaturedBookRepository extends JpaRepository<UserWeeklyFeaturedBook, Long> {
    Optional<UserWeeklyFeaturedBook> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);
}

