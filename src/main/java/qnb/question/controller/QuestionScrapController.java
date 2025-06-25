package qnb.question.controller;

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
import qnb.question.dto.QuestionScrapResponseDto;
import qnb.question.service.QuestionScrapService;
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("스크랩이 완료되었습니다.", result));
    }
}
