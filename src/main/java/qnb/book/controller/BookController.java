package qnb.book.controller;

import qnb.book.service.AladinApiService;
import qnb.book.service.BookService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> importBooks() {
        // [장르명, 알라딘 CategoryId]
        aladinApiService.fetchBooksByCategory("한국소설", 50973);
        aladinApiService.fetchBooksByCategory("과학소설", 50992);
        aladinApiService.fetchBooksByCategory("로맨스", 50976);
        aladinApiService.fetchBooksByCategory("자기계발", 336);
        aladinApiService.fetchBooksByCategory("에세이", 2551);

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
            @RequestParam(required = false) String category, // genre로 매핑됨
            @RequestParam(required = false) Integer bookId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long userId
    ) {
        return switch (type) {
            case "recommendations" -> {
                if (limit != null && limit == 1) {
                    // 개인 추천 도서 1권 조회
                    yield ResponseEntity.ok(bookService.getSingleRecommendedBook(userId));
                }
                // 개인 추천 도서 전체 조회
                yield ResponseEntity.ok(bookService.getRecommendedBooks(userId));
            }

            // 장르별 추천 도서
            case "category-recommendations" ->
                    ResponseEntity.ok(bookService.getRecommendedBooksByGenre(category));

            // 신간 도서
            case "new" ->
                    ResponseEntity.ok(bookService.getNewBooks(PageRequest.of(page, size)));

            // 도서 상세 조회
            case "detail" ->
                    ResponseEntity.ok(bookService.getBookDetail(bookId));

            // 도서 관련 질문 목록
            case "questions" ->
                    ResponseEntity.ok(bookService.getBookQuestions(bookId, sort, PageRequest.of(page, size)));

            default ->
                    ResponseEntity.badRequest().body("지원하지 않는 type입니다.");
        };
    }
}
