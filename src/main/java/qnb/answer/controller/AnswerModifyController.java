package qnb.answer.controller;


import qnb.answer.dto.AnswerRequestDto;
import qnb.answer.dto.AnswerResponseDto;
import qnb.common.exception.LoginRequiredException;
import qnb.user.security.UserDetailsImpl;
import qnb.answer.service.AnswerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerModifyController {
    private final AnswerService answerService;

    //답변 수정
    @PutMapping("/{answerId}")
    public ResponseEntity<Map<String, Object>> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody @Valid AnswerRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            throw new LoginRequiredException(); // 401
        }

        AnswerResponseDto responseDto = answerService.updateAnswer(
                answerId, requestDto, userDetails.getUser().getUserId()
        );

        return ResponseEntity.ok(Map.of(
                "data", responseDto,
                "message", "답변이 성공적으로 수정되었습니다."
        ));
    }

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
