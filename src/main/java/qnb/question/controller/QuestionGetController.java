package qnb.question.controller;

import org.springframework.web.bind.annotation.*;
import qnb.question.dto.QuestionDetailResponseDto;
import qnb.question.dto.QuestionPageResponseDto;
import qnb.user.security.UserDetailsImpl;
import qnb.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionGetController {

    private final QuestionService questionService;

    //최신 질문 조회 API
    @GetMapping("/recent")
    public ResponseEntity<QuestionPageResponseDto> getRecentQuestions(
            @RequestParam(defaultValue = "1") int page, //page 기본값 1
            @RequestParam(defaultValue = "5") int size)//page당 질문수 기본값 5
    {
        // page는 최소 1, size는 최소 1 최대 50 제한
        int safePage = Math.max(page-1, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        QuestionPageResponseDto response = questionService.getRecentQuestions(page, size);
        return ResponseEntity.ok(response);
    }

    //질문 상세 조회 API
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailResponseDto> getQuestionDetail(
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "latest") String sort) {

        QuestionDetailResponseDto response = questionService.getQuestionDetail(questionId, sort);
        return ResponseEntity.ok(response);
    }
}
