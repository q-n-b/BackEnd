package qnb.question.controller;

import org.springframework.web.bind.annotation.*;
import qnb.question.dto.QuestionDetailResponseDto;
import qnb.question.dto.QuestionPageResponseDto;
import qnb.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionGetController {

    private final QuestionService questionService;

    //최신 질문 조회 API
    @GetMapping("/recent")
    public ResponseEntity<QuestionPageResponseDto> getRecentQuestions(
            @RequestParam(defaultValue = "1") int page, //page 기본값 1
            @RequestParam(defaultValue = "5") int size) //page당 질문수 기본값 5
    {
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
