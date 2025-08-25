package qnb.recommend.scheduler;

/*매주 한 번 “이번 주 추천 도서(Featured)”를 자동 확정해 주는 “배치 작업”
user_recommended_book(캐시)에서 각 사용자별 최고 점수 1권을 골라
user_weekly_featured_book(히스토리)에 저장하는 스케줄러*/

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import qnb.recommend.service.WeeklyFeaturedService;
import qnb.user.repository.UserRepository;
import qnb.common.util.WeekUtils;


@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyFeaturedScheduler {

    private final WeeklyFeaturedService service;
    private final UserRepository userRepository;

    /**
     * 매주 월요일 09:00 KST
     * cron은 서버 TZ에 따라 다르게 동작하므로 zone을 명시
     */
    @Scheduled(cron = "0 0 9 ? * MON", zone = "Asia/Seoul")
    public void runBatch() {
        var from = WeekUtils.thisWeekStartZdt().toLocalDateTime(); // KST 기준 월요일 00:00
        var to   = WeekUtils.nextWeekStartZdt().toLocalDateTime(); // KST 기준 다음주 월요일 00:00

        var userIds = userRepository.findEligibleUserIdsForThisWeek(from, to);
        log.info("[WeeklyFeaturedScheduler] start, eligibleUsers={}", userIds.size());

        for (Long uid : userIds) {
            try {
                var res = service.upsertThisWeekFeatured(uid);
                if (res == null) log.warn("No weekly pick (empty cache) for user_id={}", uid);
            } catch (Exception e) {
                log.error("Weekly pick failed for user_id={}", uid, e);
            }
        }
        log.info("[WeeklyFeaturedScheduler] end");
    }
}

