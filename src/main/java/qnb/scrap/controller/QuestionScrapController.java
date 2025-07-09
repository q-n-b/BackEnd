package qnb.scrap.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qnb.common.dto.ApiResponse;
import qnb.common.exception.LoginRequiredException;
import qnb.scrap.dto.QuestionScrapResponseDto;
import qnb.scrap.service.QuestionScrapService;
import qnb.user.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionScrapController {

    private final QuestionScrapService questionScrapService;

    @PostMapping("/{questionId}/scrap")
    public ResponseEntity<ApiResponse<QuestionScrapResponseDto>> scrapQuestion(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new LoginRequiredException();
        }

        Long userId = userDetails.getUserId();
        QuestionScrapResponseDto result = questionScrapService.toggleScrap(userId, questionId);

        // scrapped 상태에 따라 메시지 선택
        String message = result.isScrapped()
                ? "스크랩이 완료되었습니다."
                : "스크랩이 취소되었습니다.";

        return ResponseEntity.status(HttpStatus.CREATED)  // 201로 명시
                .body(ApiResponse.success(message, result));
    }
}
