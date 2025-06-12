package qnb.question.controller;

import qnb.question.dto.QuestionPageResponseDto;
import qnb.user.security.UserDetailsImpl;
import qnb.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionGetController {

    private final QuestionService questionService;

    //최신 질문 조회 API
    @GetMapping("/recent")
    public ResponseEntity<QuestionPageResponseDto> getRecentQuestions(
            @RequestParam(defaultValue = "1") int page, //page 기본값 1
            @RequestParam(defaultValue = "5") int size, //page당 질문수 기본값 5
            @AuthenticationPrincipal UserDetailsImpl userDetails) {


        QuestionPageResponseDto response = questionService.getRecentQuestions(page, size);
        return ResponseEntity.ok(response);
    }
}
