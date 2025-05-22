package com.example.qnb.answer.controller;

import com.example.qnb.answer.dto.AnswerRequestDto;
import com.example.qnb.answer.dto.AnswerResponseDto;
import com.example.qnb.answer.service.AnswerService;
import com.example.qnb.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> registerAnswer(@PathVariable Long questionId,
                                            @RequestBody AnswerRequestDto dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AnswerResponseDto response = answerService.registerAnswer(
                questionId,
                userDetails.getUserId(),
                userDetails.getUserNickname(),
                userDetails.getProfileUrl(),
                dto
        );
        return ResponseEntity.status(201).body(
                java.util.Map.of(
                        "data", response,
                        "message", "답변이 등록되었습니다."
                )
        );
    }
}