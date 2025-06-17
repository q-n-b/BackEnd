package qnb.user.controller;
//내가 남긴 Q&A 조회에서 사용할 컨트롤러


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qnb.user.dto.UserQnaResponseDto;
import qnb.user.security.UserDetailsImpl;
import qnb.user.service.UserQnaService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserQnaController {

    private final UserQnaService qnaService;

    @GetMapping("/qna")
    public ResponseEntity<List<UserQnaResponseDto>> getUserQnaList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String type) {

        Long userId = userDetails.getUserId();
        List<UserQnaResponseDto> qnaList = qnaService.getUserQnaList(userId, type);
        return ResponseEntity.ok(qnaList);
    }
}
