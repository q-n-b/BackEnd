// service/WeeklyFeaturedService.java
package qnb.recommend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qnb.common.util.WeekUtils;
import qnb.recommend.dto.RecommendedPick;
import qnb.recommend.dto.WeeklyFeaturedDto;
import qnb.recommend.entity.UserWeeklyFeaturedBook;
import qnb.book.repository.UserRecommendedBookRepository;
import qnb.recommend.repository.UserWeeklyFeaturedBookRepository;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyFeaturedService {

    private final UserRecommendedBookRepository recommendedRepo;
    private final UserWeeklyFeaturedBookRepository weeklyRepo;

    //추천 도서 이번주 확정본 생성
    /**
     * 이번 주 사용자 1인의 Featured Book을 확정/저장하고 결과 반환.
     * - 이미 확정되어 있으면 그대로 반환(멱등)
     * - 없으면 이번 주 캐시 중 score 최고 1건 픽
     * - 최고점 없으면 랜덤 1건 픽(FALLBACK)
     * - 그래도 없으면 null 반환(상위에서 처리)
     */
    @Transactional
    public UserWeeklyFeaturedBook upsertThisWeekFeatured(Long userId) {
        var weekStart = WeekUtils.thisWeekMondayKST(); // LocalDate (KST 기준)
        var startZdt  = WeekUtils.thisWeekStartZdt();  // 이번 주 월요일 00:00 KST
        var endZdt    = WeekUtils.nextWeekStartZdt();  // 다음 주 월요일 00:00 KST

        // 1) 이미 확정되어 있으면 반환 (멱등성)
        var existing = weeklyRepo.findByUserIdAndWeekStartDate(userId, weekStart);
        if (existing.isPresent()) return existing.get();

        // 2) 이번 주 캐시에서 최고점 1건 (Pageable로 제한)
        var topList = recommendedRepo.pickTopOfWeek(
                userId,
                startZdt.toLocalDateTime(),
                endZdt.toLocalDateTime(),
                PageRequest.of(0, 1)
        );

        RecommendedPick pick = topList.isEmpty() ? null : topList.get(0);

        // 3) 랜덤 폴백(이번 주 캐시에서) - 역시 1건만
        if (pick == null) {
            var randoms = recommendedRepo.pickRandomOfWeek(
                    userId,
                    startZdt.toLocalDateTime(),
                    endZdt.toLocalDateTime(),
                    PageRequest.of(0, 1)
            );
            pick = randoms.isEmpty() ? null : randoms.get(0);
        }

        if (pick == null) {
            // 이번 주 캐시에 아무것도 없다 → 상위 컨트롤러에서 409 등으로 처리
            return null;
        }

        // 4) 저장 (동시성 유니크 충돌 시 멱등 재조회)
        var entity = UserWeeklyFeaturedBook.builder()
                .userId(userId)
                .bookId(pick.getBookId())
                .weekStartDate(weekStart)
                .score(pick.getScore())
                .createdAt(LocalDateTime.now())
                .build();

        try {
            return weeklyRepo.save(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return weeklyRepo.findByUserIdAndWeekStartDate(userId, weekStart)
                    .orElseThrow(() -> e);
        }
    }

    //추천 도서 이번주 확정본 조회
    @Transactional(readOnly = true)
    public Optional<WeeklyFeaturedDto> getWeeklyFeatured(Long userId, LocalDate weekStartParam) {
        LocalDate weekStart = WeekUtils.resolveWeekStart(weekStartParam);
        return weeklyRepo.findByUserIdAndWeekStartDate(userId, weekStart)
                .map(WeeklyFeaturedDto::from);
    }
}

