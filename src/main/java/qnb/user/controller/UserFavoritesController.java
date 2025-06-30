package qnb.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qnb.common.dto.ApiResponse;
import qnb.user.security.UserDetailsImpl;
import qnb.user.service.UserFavoriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/favorites")
public class UserFavoritesController {

    private final UserFavoriteService userFavoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFavorites(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "type", defaultValue = "SCRAP") String type,
            @RequestParam(value = "target", required = false) String target,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUserId();
        PageRequest pageable = PageRequest.of(page - 1, size);

        Object result = userFavoriteService.getFavorites(userId, type, target, pageable);
        return ResponseEntity.ok(ApiResponse.success("조회 성공", result));
    }
}

