package com.example.qnb.book.controller;
//AladinApiService를 실행시키기 위한 컨트롤러 코드

import com.example.qnb.book.service.AladinApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final AladinApiService aladinApiService;

    public BookController(AladinApiService aladinApiService) {
        this.aladinApiService = aladinApiService;
    }

    // 도서 데이터 수집 API
    @PostMapping("/import")
    public ResponseEntity<String> importBooks() {
        // [카테고리명, 알라딘 CategoryId] 순서로 수집
        aladinApiService.fetchBooksByCategory("한국소설", 50973);
        aladinApiService.fetchBooksByCategory("과학소설", 50992);
        aladinApiService.fetchBooksByCategory("로맨스", 50976);
        aladinApiService.fetchBooksByCategory("자기계발", 336);
        aladinApiService.fetchBooksByCategory("에세이", 2551);

        return ResponseEntity.ok("도서 수집 완료");
    }
}
