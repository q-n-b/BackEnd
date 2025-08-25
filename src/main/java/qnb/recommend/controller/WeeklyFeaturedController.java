package qnb.recommend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qnb.recommend.entity.UserWeeklyFeaturedBook;
import qnb.recommend.service.WeeklyFeaturedService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class WeeklyFeaturedController {

    private final WeeklyFeaturedService service;

    //이번 주 Featured 1건을 “확정”하거나(없으면 생성) “그대로 반환”하는 멱등 엔드포인트
    @PostMapping("/{userId}/weekly-featured")
    public ResponseEntity<?> createOrGetThisWeek(@PathVariable Long userId) {
        UserWeeklyFeaturedBook res = service.upsertThisWeekFeatured(userId);
        if (res == null) {
            return ResponseEntity.status(409).body(
                    new ApiMessage("이번 주 캐시(user_recommended_book)가 비어 있어 선정 불가. " +
                            "이번 주 캐시 재적재 후 재시도하세요.")
            );
        }
        return ResponseEntity.ok(res);
    }

    record ApiMessage(String message) {}
}

