package qnb.recommend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import qnb.recommend.entity.UserWeeklyFeaturedBook;
import qnb.recommend.service.WeeklyFeaturedService;
import qnb.user.security.UserDetailsImpl;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class WeeklyFeaturedController {

    private final WeeklyFeaturedService service;

    //추천 도서 이번주 확정본 생성
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

    //추천 도서 이번주 확정본 조회
    @GetMapping("/weekly-featured")
    public ResponseEntity<?> getWeeklyFeatured(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        Long userId = principal.getUser().getUserId();

        return service.getWeeklyFeatured(userId, weekStartDate)
                .map(dto -> ResponseEntity.ok(Map.of(
                        "data", dto,
                        "message", "이번 주 확정본을 반환합니다."
                )))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private record ApiMessage(String message) {}
}
