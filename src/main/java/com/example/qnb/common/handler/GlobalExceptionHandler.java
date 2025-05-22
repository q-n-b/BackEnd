package com.example.qnb.common.handler;

import com.example.qnb.common.exception.BookNotFoundException;
import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.common.exception.QuestionNotFoundException;
import com.example.qnb.common.exception.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //질문 찾을 수 없을 때
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<?> handleNotFound(QuestionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "errorCode", "QUESTION_NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    //해당 책을 찾을 수 없을 때
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<?> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "errorCode", "BOOK_NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    //권한이 없을 경우
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "errorCode", "UNAUTHORIZED",
                "message", ex.getMessage()
        ));
    }

    //질문 내용이 유효한 경우가 아닌 경우
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MISSING_QUESTION_CONTENT",
                "message", errorMessage
        ));
    }

    //로그인 필요할 시
    @ExceptionHandler(LoginRequiredException.class)
    public ResponseEntity<?> handleLoginRequired(LoginRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "errorCode", "UNAUTHORIZED",
                "message", ex.getMessage()
        ));
    }

    //서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleServerError(Exception ex) {
        ex.printStackTrace(); // 디버깅용
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "INTERNAL_ERROR",
                "message", "서버 에러가 발생했습니다."
        ));
    }


}
