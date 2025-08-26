// service/WeeklyFeaturedService.java
package qnb.recommend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qnb.common.util.WeekUtils;
import qnb.recommend.dto.RecommendedPick;
import qnb.recommend.entity.UserWeeklyFeaturedBook;
import qnb.book.repository.UserRecommendedBookRepository;
import qnb.recommend.repository.UserWeeklyFeaturedBookRepository;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyFeaturedService {

    private final UserRecommendedBookRepository recommendedRepo;
    private final UserWeeklyFeaturedBookRepository weeklyRepo;

    /**
     * 이번 주 사용자 1인의 Featured Book을 확정/저장하고 결과 반환.
     * - 이미 확정되어 있으면 그대로 반환(멱등)
     * - 없으면 이번 주 캐시 중 score 최고 1건 픽
     * - 최고점 없으면 랜덤 1건 픽(FALLBACK)
     * - 그래도 없으면 null 반환(상위에서 처리)
     */
    @Transactional
    public UserWeeklyFeaturedBook upsertThisWeekFeatured(Long userId) {
        var weekStart = WeekUtils.thisWeekMondayKST();                 // LocalDate
        var startZdt  = WeekUtils.thisWeekStartZdt();                   // ZonedDateTime
        var endZdt    = WeekUtils.nextWeekStartZdt();

        // 1) 이미 확정되어 있으면 반환 (멱등성)
        var existing = weeklyRepo.findByUserIdAndWeekStartDate(userId, weekStart);
        if (existing.isPresent()) return existing.get();

        // 2) 이번 주 캐시에서 최고점 1건
        var topOpt = recommendedRepo.pickTopOfWeek(
                userId,
                startZdt.toLocalDateTime(),
                endZdt.toLocalDateTime()
        );

        RecommendedPick pick = topOpt.orElseGet(() -> {
            // 3) 랜덤 폴백(이번 주 캐시에서)
            List<RecommendedPick> randoms = recommendedRepo.pickRandomOfWeek(
                    userId, startZdt.toLocalDateTime(), endZdt.toLocalDateTime()
            );
            return randoms.isEmpty() ? null : randoms.get(0);
        });

        if (pick == null) {
            // 이번 주 캐시에 아무것도 없다 → 상위에서 재적재(or 사용자에게 안내)하도록 null
            return null;
        }

        // 4) 저장
        var entity = UserWeeklyFeaturedBook.builder()
                .userId(userId)
                .bookId(pick.getBookId())
                .weekStartDate(weekStart)
                .score(pick.getScore())
                .createdAt(LocalDateTime.now())
                .build();

        return weeklyRepo.save(entity);
    }
}

