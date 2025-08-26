package qnb.book.dto;

//개인 추천 도서 1권 조회 dto 파일
//프론트에 최종 응답할 추천 도서 데이터
// 키워드 포함

import qnb.book.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SingleRecommendedBookResponseDto {

    private BookResponseDto book;
    private RecommendationInfo recommendation;

    public SingleRecommendedBookResponseDto(Book book, String keyword) {
        this.book = new BookResponseDto(book);
        this.recommendation = new RecommendationInfo(keyword);
    }

    @Data
    @AllArgsConstructor
    public static class RecommendationInfo {
        private String keyword;
    }
}
