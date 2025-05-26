package com.example.qnb.answer.controller;

import com.example.qnb.answer.dto.AnswerRequestDto;
import com.example.qnb.answer.dto.AnswerResponseDto;
import com.example.qnb.answer.service.AnswerService;
import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.common.exception.MissingFieldException;
import com.example.qnb.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class AnswerRegisterController {

    private final AnswerService answerService;

    //답변 등록
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> registerAnswer(@PathVariable Long questionId,
                                            @RequestBody AnswerRequestDto dto,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        //로그인 안되었을 때 오류
        if (userDetails == null) {
            throw new LoginRequiredException();
        }

        if (dto.getAnswerContent() == null || dto.getAnswerContent().trim().isEmpty()) {
            throw new MissingFieldException("답변 내용이 비어 있습니다.");  // 원하는 메시지
        }

        if (dto.getAnswerState() == null) {
            throw new MissingFieldException("독서 상태가 선택되지 않았습니다.");
        }



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