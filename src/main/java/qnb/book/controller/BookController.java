package qnb.book.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import qnb.book.service.AladinApiService;
import qnb.book.service.BookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qnb.common.exception.LoginRequiredException;
import qnb.user.entity.User;
import qnb.user.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final AladinApiService aladinApiService;
    private final BookService bookService;

    public BookController(AladinApiService aladinApiService, BookService bookService) {
        this.aladinApiService = aladinApiService;
        this.bookService = bookService;
    }

    /**
     * [POST] /api/books/import
     * 알라딘 API를 통해 각 카테고리별 도서 데이터를 수집하여 DB에 저장하는 엔드포인트
     */
    @PostMapping("/fetch")
    public ResponseEntity<String> importAllBooks() {
        aladinApiService.fetchAllCategories();
        return ResponseEntity.ok("도서 수집 완료");
    }

    /**
     * [GET] /api/books
     * type 파라미터에 따라 도서 정보를 조회하는 통합 엔드포인트
     * 지원하는 type: recommendations, category-recommendations, new, detail, questions
     */
    @GetMapping
    public ResponseEntity<?> getBooksByType(
            @RequestParam String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer bookId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return switch (type) {
            case "recommendations" -> {

                // 로그인 안 되어 있으면 401
                if (userDetails == null) {
                    throw new LoginRequiredException();
                }

                Long userId = userDetails.getUserId();

                //개인 추천 도서 1권
                if (limit != null && limit == 1) {
                    yield ResponseEntity.ok(bookService.getSingleRecommendedBook(userId));
                }

                //개인 추천 도서 리스트 조회
                yield ResponseEntity.ok(bookService.getRecommendedBooks(userId));
            }

            //카테고리별 추천 도서 조회
            case "category-recommendations" -> {
                yield ResponseEntity.ok(bookService.getRecommendedBooksByGenre(category));
            }

            //신간 도서 조회
            case "new" -> {
                yield ResponseEntity.ok(bookService.getNewBooks(PageRequest.of(page, size)));
            }

            //도서 상세 조회
            case "detail" -> {
                User user = (userDetails != null) ? userDetails.getUser() : null;
                yield ResponseEntity.ok(bookService.getBookDetail(bookId, user));
            }

            //도서별 질문 리스트 조회
            case "questions" -> {
                yield ResponseEntity.ok(bookService.getBookQuestions(bookId, sort, PageRequest.of(page, size)));
            }

            default -> {
                yield ResponseEntity.badRequest().body("지원하지 않는 type입니다.");
            }
        };
    }
}
