package qnb.recommend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qnb.recommend.dto.WeeklyFeaturedFlatViewDto;
import qnb.recommend.entity.UserWeeklyFeaturedBook;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserWeeklyFeaturedBookRepository extends JpaRepository<UserWeeklyFeaturedBook, Long> {
    Optional<UserWeeklyFeaturedBook> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);

    //MY 페이지의 <추천 도서 리스트> 조회시 사용
    @Query("""
    select new qnb.recommend.dto.WeeklyFeaturedFlatViewDto(
        b.bookId, 
        b.isbn13,
        b.title,
        b.author,
        b.imageUrl,
        b.genre,
        b.publisher,
        b.publishedYear,
        uwb.weekStartDate 
    )
    from UserWeeklyFeaturedBook uwb
    join Book b on b.bookId = uwb.bookId
    where uwb.userId = :userId
    order by uwb.weekStartDate desc
""")
    List<WeeklyFeaturedFlatViewDto> findAllFlatByUserId(@Param("userId") Long userId);

}

