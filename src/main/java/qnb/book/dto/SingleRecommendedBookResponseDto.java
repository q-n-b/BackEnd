package com.example.qnb.book.dto;

//개인 추천 도서 1권 조회 dto 파일
//추천 이유, 키워드 포함

import com.example.qnb.book.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SingleRecommendedBookResponseDto {

    private BookResponseDto book;
    private RecommendationInfo recommendation;

    public SingleRecommendedBookResponseDto(Book book, String reason, String keyword) {
        this.book = new BookResponseDto(book);  // 기존 DTO 활용
        this.recommendation = new RecommendationInfo(reason, keyword);
    }

    @Data
    @AllArgsConstructor
    public static class RecommendationInfo {
        private String reason;   //  question/answer/search/scrap/like
        private String keyword;  // ex: "아포칼립스"
    }
}
