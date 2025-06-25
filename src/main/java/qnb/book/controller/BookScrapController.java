package qnb.book.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import qnb.book.dto.BookScrapRequestDto;
import qnb.book.dto.BookScrapResponseDto;
import qnb.book.service.BookScrapService;
import qnb.common.dto.ApiResponse;
import qnb.common.exception.InvalidStatusException;
import qnb.common.exception.LoginRequiredException;
import qnb.user.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookScrapController {

    private final BookScrapService bookScrapService;

    @PostMapping("/{bookId}/scrap")
    public ResponseEntity<ApiResponse<BookScrapResponseDto>> toggleBookScrap(
            @PathVariable Integer bookId,
            @RequestBody(required = false) BookScrapRequestDto request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null || userDetails.getUserId() == null) {
            throw new LoginRequiredException();
        }

        if (request == null || request.getStatus() == null) {
            throw new InvalidStatusException();
        }

        Long userId = userDetails.getUserId();
        BookScrapResponseDto result = bookScrapService.toggleScrap(bookId, userId, request.getStatus());

        return ResponseEntity.ok(ApiResponse.success(result.getMessage(), result));
    }
}
