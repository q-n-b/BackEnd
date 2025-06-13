/*
package qnb.question.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qnb.question.service.QuestionService;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor

//질문 생성 API
public class QuestionCreateController {
    private final QuestionService questionService;

    @PostMapping("/{bookId}/generate-question")
    public ResponseEntity<?> generateQuestion(@PathVariable Integer bookId) {
        questionService.generateQuestion(bookId);
        return ResponseEntity.ok().body(Map.of(
                "message", "질문이 성공적으로 생성되었습니다."
        ));
    }
}
*/
