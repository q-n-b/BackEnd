package qnb.question.controller;

import qnb.book.dto.BookResponseDto;
import qnb.book.service.BookService;
import qnb.common.exception.BookNotFoundException;
import qnb.common.exception.LoginRequiredException;
import qnb.question.dto.QuestionRequestDto;
import qnb.question.dto.QuestionResponseDto;
import qnb.question.entity.Question;
import qnb.question.service.QuestionService;
import qnb.user.security.UserDetailsImpl;
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
public class QuestionRegisterController {

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
        String profileUrl = userDetails.getProfileUrl();

        Question savedQuestion = questionService.createQuestion(userId, bookId, dto);

        int answerCount = 0;
        QuestionResponseDto responseDto = new QuestionResponseDto(
                BookResponseDto.from(savedQuestion.getBook()),
                savedQuestion.getUser().getUserId(),
                savedQuestion.getQuestionId(),
                savedQuestion.getUser().getUserNickname(),
                savedQuestion.getUser().getProfileUrl(),
                savedQuestion.getQuestionContent(),
                answerCount,
                savedQuestion.getLikeCount(),
                savedQuestion.getScrapCount(),
                savedQuestion.getStatus(),
                savedQuestion.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", responseDto,
                "message", "질문이 등록되었습니다."
        ));
    }

}

