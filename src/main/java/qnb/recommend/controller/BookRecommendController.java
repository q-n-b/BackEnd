package qnb.recommend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qnb.recommend.dto.MlRecommendRequest;
import qnb.recommend.dto.MlRecommendResponse;
import qnb.recommend.service.RecommendationGenerationService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookRecommendController {

    private final RecommendationGenerationService service;

    @PostMapping("/recommend")
    public ResponseEntity<MlRecommendResponse> recommend(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, // "Bearer xxx"
            @RequestParam(name = "topK", defaultValue = "10") @Min(1) @Max(100) int topK,
            @Valid @RequestBody MlRecommendRequest requestBody
    ) {
        // 간단 방어: "Bearer ..." 형식 확인
        if (authorization == null || !authorization.toLowerCase().startsWith("bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        MlRecommendResponse resp = service.generateAndPersist(authorization, topK, requestBody);
        return ResponseEntity.ok(resp);
    }
}