package com.example.qnb.question.controller;

import com.example.qnb.book.dto.BookResponseDto;
import com.example.qnb.common.exception.BookNotFoundException;
import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.common.exception.QuestionNotFoundException;
import com.example.qnb.question.dto.QuestionRequestDto;
import com.example.qnb.question.dto.QuestionResponseDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.question.service.QuestionService;
import com.example.qnb.user.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionModifyController {

    private final QuestionService questionService;

    //질문 수정 API
    @PutMapping("/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Integer questionId,
            @RequestBody @Valid QuestionRequestDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            throw new LoginRequiredException(); // 401 처리
        }

        Long userId = userDetails.getUserId();

        Question updatedQuestion = questionService.updateQuestion(questionId, userId, dto);
        int answerCount = 0; // TODO: 실제 답변 수 계산 로직 넣기

        QuestionResponseDto responseDto = new QuestionResponseDto(
                BookResponseDto.from(updatedQuestion.getBook()),
                updatedQuestion.getUser().getUserId(),
                updatedQuestion.getQuestionId(),
                updatedQuestion.getUser().getUserNickname(),
                updatedQuestion.getUser().getProfileUrl(),
                updatedQuestion.getQuestionContent(),
                answerCount,
                updatedQuestion.getLikeCount(),
                updatedQuestion.getScrapCount(),
                updatedQuestion.getCreatedAt()
        );

        return ResponseEntity.ok(Map.of(
                "data", responseDto,
                "message", "질문이 성공적으로 수정되었습니다."
        ));
    }


    //질문 삭제 API
    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Integer questionId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            throw new LoginRequiredException(); // 401
        }

        Long userId = userDetails.getUserId();

        questionService.deleteQuestion(questionId, userId);

        return ResponseEntity.ok(Map.of(
                "questionId", questionId,
                "message", "질문이 성공적으로 삭제되었습니다."
        ));
    }
}
