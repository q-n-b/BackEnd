package qnb.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qnb.book.entity.UserRecommendedBook;
import qnb.book.repository.UserRecommendedBookRepository;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyFeaturedJobService {

    private final UserRecommendedBookRepository recRepo; // 기존 레포지토리 재활용
    private final JdbcTemplate jdbc;                     // 주간 테이블은 JDBC로 INSERT

    private static LocalDate thisMondayKST() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * 매주 월요일 09:00 (Asia/Seoul) 실행
     * - 최신 추천 1권 확정 저장
     * - 이미 읽은 책 제외
     * - 같은 주/유저 중복은 UNIQUE 제약으로 보호
     */
    @Scheduled(cron = "0 0 9 ? * MON", zone = "Asia/Seoul")
    @Transactional
    public void publishWeeklyFeatured() {
        LocalDate weekStart = thisMondayKST();
        List<Long> userIds = recRepo.findDistinctUserIds();

        var top1 = PageRequest.of(0, 1);

        for (Long userId : userIds) {
            // 유저별 Top-1 후보 (이미 읽은 책 제외 + 최신순)
            Optional<UserRecommendedBook> candidate =
                    recRepo.findTopCandidateForWeekly(userId, top1).stream().findFirst();

            if (candidate.isEmpty()) continue;

            Integer bookId = candidate.get().getBook().getBookId();

            // 주간 확정 테이블에 삽입 (중복 시 그대로 유지)
            jdbc.update("""
                INSERT INTO user_weekly_featured_book (user_id, book_id, week_start)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE book_id = VALUES(book_id)
                """,
                    userId, bookId, weekStart
            );
        }
    }
}
