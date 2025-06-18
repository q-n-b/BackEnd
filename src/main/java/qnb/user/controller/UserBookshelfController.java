package qnb.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qnb.user.dto.BookshelfResponseDto;
import qnb.user.entity.User;
import qnb.user.security.UserDetailsImpl;
import qnb.user.service.UserBookshelfService;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/bookshelf")
public class UserBookshelfController {

    private final UserBookshelfService bookshelfService;

    @GetMapping
    public ResponseEntity<List<BookshelfResponseDto>> getBookshelf(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String status
    ) {
        User user = userDetails.getUser(); // 실제 User 꺼내기
        List<BookshelfResponseDto> result = bookshelfService.getBooksByStatus(user, status);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(result);
    }

}