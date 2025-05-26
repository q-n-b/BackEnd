package com.example.qnb.answer.controller;


import com.example.qnb.common.exception.LoginRequiredException;
import com.example.qnb.user.security.UserDetailsImpl;
import com.example.qnb.answer.service.AnswerService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerModifyController {
    private final AnswerService answerService;

    //답변 삭제 API
    @DeleteMapping("/{answerId}")
    public ResponseEntity<Map<String, Object>> deleteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new LoginRequiredException(); // 401
        }

        Long loginUserId = userDetails.getUser().getUserId();

        answerService.deleteAnswer(answerId, loginUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("answerId", answerId);
        response.put("message", "답변이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response); // 200 OK
    }
}
