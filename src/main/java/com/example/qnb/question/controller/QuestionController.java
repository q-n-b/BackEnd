package com.example.qnb.question.controller;

import com.example.qnb.book.service.BookService;
import com.example.qnb.login.security.UserDetailsImpl;
import com.example.qnb.question.dto.QuestionRequestDto;
import com.example.qnb.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class QuestionController {

    private final QuestionService questionService;
    private final BookService bookService;

    //질문 등록 API
    @PostMapping("/{bookId}/questions")
    public ResponseEntity<?> registerQuestion(
            @PathVariable Integer bookId,
            @RequestBody @Valid QuestionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 401 Unauthorized: 로그인 안 된 경우
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "errorCode", "UNAUTHORIZED",
                    "message", "로그인이 필요합니다."
            ));
        }

        try {
            Long userId = userDetails.getUserId();
            String userNickname = userDetails.getNickname();

            // 404 Not Found: 존재하지 않는 bookId인 경우
            if (!bookService.existsById(bookId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "errorCode", "BOOK_NOT_FOUND",
                        "message", "질문을 등록할 책을 찾을 수 없습니다."
                ));
            }

            // 질문 생성 서비스 호출
            questionService.createQuestion(userId, userNickname, bookId, dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "questionId", null,  // 생성된 ID가 있다면 여기에 반환 가능
                    "message", "질문이 등록되었습니다."
            ));
        } catch (Exception e) {
            e.printStackTrace(); // 서버 디버깅용 로그 출력

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "errorCode", "INTERNAL_ERROR",
                    "message", "서버 에러가 발생했습니다."
            ));
        }
    }


    //400 Bad Request: 유효성 검사 실패 (예: 질문 내용이 비어있음)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MISSING_QUESTION_CONTENT",
                "message", errorMessage
        ));
    }
}
