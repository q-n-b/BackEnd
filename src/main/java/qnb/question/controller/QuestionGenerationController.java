package qnb.question.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import qnb.question.model.QuestionStatus;
import qnb.question.service.QuestionGenerationService;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class QuestionGenerationController {

    private final QuestionGenerationService generationService;

    @PostMapping("/{bookId}/generate-question")
    public ResponseEntity<?> generate(@PathVariable Integer bookId) {
        var r = generationService.generate(bookId);

        // 200 OK (이미 GENERATING 진행 중)
        if (r.getHttpStatus() == 200) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("questionId", r.getQuestionId());
            body.put("status", r.getStatus().name());
            body.put("message", "해당 도서에 대한 질문이 생성 중입니다.");
            body.put("nextCheckAfterSec", r.getNextCheckAfterSec());
            return ResponseEntity.ok(body);
        }

        // 201 Created (새 드래프트 생성: READY 또는 GENERATING)
        if (r.getHttpStatus() == 201) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("questionId", r.getQuestionId());
            body.put("status", r.getStatus().name());

            if (r.getStatus() == QuestionStatus.READY) {
                body.put("message", "질문이 성공적으로 생성되었습니다.");
            } else {
                body.put("message", "질문 생성이 시작되었습니다.(백그라운드 처리 중)");
                body.put("nextCheckAfterSec", r.getNextCheckAfterSec());
            }

            return ResponseEntity
                    .created(URI.create("/api/questions/" + r.getQuestionId()))
                    .body(body);
        }

        // 409 Conflict (이미 READY 존재)
        if (r.getHttpStatus() == 409) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("errorCode", r.getErrorCode());
            body.put("message", r.getErrorMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        // 방어: 예상치 못한 경우 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "errorCode", "InternalErrorException",
                        "message", "질문 생성 중 오류가 발생했습니다."
                ));
    }
}
