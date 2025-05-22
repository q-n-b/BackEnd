package com.example.qnb.question.controller;

import com.example.qnb.book.service.BookService;
import com.example.qnb.common.exception.BookNotFoundException;
import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.question.dto.QuestionRequestDto;
import com.example.qnb.question.dto.QuestionResponseDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.question.service.QuestionService;
import com.example.qnb.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books/{bookId}")
public class QuestionController {

    private final QuestionService questionService;
    private final BookService bookService;

    // 질문 등록 API
    @PostMapping("/questions")
    public ResponseEntity<?> registerQuestion(
            @PathVariable Integer bookId,
            @RequestBody @Valid QuestionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 예외 전환 (직접 응답 대신 throw)
        if (userDetails == null) {
            throw new LoginRequiredException();
        }

        if (!bookService.existsById(bookId)) {
            throw new BookNotFoundException();
        }

        Long userId = userDetails.getUserId();
        String profileUrl = userDetails.getprofileUrl();

        Question savedQuestion = questionService.createQuestion(userId, bookId, dto);

        int answerCount = 0;
        QuestionResponseDto responseDto = new QuestionResponseDto(savedQuestion, answerCount, profileUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", responseDto,
                "message", "질문이 등록되었습니다."
        ));
    }

    //질문 수정 API
    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer questionId,
            @RequestBody @Valid QuestionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            throw new LoginRequiredException(); // 401 처리
        }

        Long userId = userDetails.getUserId();
        String profileUrl = userDetails.getprofileUrl();

        Question updatedQuestion = questionService.updateQuestion(questionId, userId, dto);
        int answerCount = 0; // 나중에 answerCount 받게하기

        QuestionResponseDto responseDto = new QuestionResponseDto(updatedQuestion, answerCount, profileUrl);

        return ResponseEntity.ok(Map.of(
                "data", responseDto,
                "message", "질문이 성공적으로 수정되었습니다."
        ));
    }

}

