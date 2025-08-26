package qnb.recommend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import qnb.recommend.entity.UserWeeklyFeaturedBook;
import qnb.recommend.service.WeeklyFeaturedService;
import qnb.user.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class WeeklyFeaturedController {

    private final WeeklyFeaturedService service;

    /**
     * 이번 주 Featured 1건을 “확정”하거나(없으면 생성) “그대로 반환”하는 멱등 엔드포인트
     * - 사용자 식별: Bearer 토큰 (@AuthenticationPrincipal)
     */
    @PostMapping("/weekly-featured")
    public ResponseEntity<?> createOrGetThisWeek(@AuthenticationPrincipal UserDetailsImpl principal) {
        Long userId = principal.getUser().getUserId();

        UserWeeklyFeaturedBook res = service.upsertThisWeekFeatured(userId);
        if (res == null) {
            return ResponseEntity.status(409).body(
                    new ApiMessage("이번 주 캐시(user_recommended_book)가 비어 있어 선정 불가. " +
                            "이번 주 캐시 재적재 후 재시도하세요.")
            );
        }
        return ResponseEntity.ok(res);
    }

    private record ApiMessage(String message) {}
}
