package qnb.question.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qnb.question.service.QuestionRetryService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionRetryController {

    private final QuestionRetryService questionRetryService;

    @PostMapping("/{questionId}/retry")
    public ResponseEntity<?> retry(@PathVariable Integer questionId) {
        Integer id = questionRetryService.retry(questionId);
        return ResponseEntity.ok(Map.of(
                "questionId", id,
                "message", "질문 생성 재시도를 시작했습니다."
        ));
    }
}
