package qnb.like.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import qnb.common.dto.ApiResponse;
import qnb.common.exception.LoginRequiredException;
import qnb.like.dto.LikeResponseDto;
import qnb.like.service.LikeService;
import qnb.user.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<LikeResponseDto>> toggleLike(
            @PathVariable String type,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new LoginRequiredException();
        }

        LikeResponseDto response = likeService.toggleLike(userDetails.getUserId(), type, id);

        String message = response.isLiked()
                ? "좋아요가 반영되었습니다."
                : "좋아요가 취소되었습니다.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
    }
}
