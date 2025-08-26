package qnb.recommend.dto;
//MY 페이지의 <추천 도서 리스트> 조회용 평탄화 DTO

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class WeeklyFeaturedFlatViewDto {
    private final Integer bookId;
    private final String isbn13;
    private final String title;
    private final String author;
    private final String imageUrl;
    private final String genre;
    private final String publisher;
    private final Integer publishedYear;
    private final LocalDate recommendedAt;

    public WeeklyFeaturedFlatViewDto(Integer bookId, String isbn13, String title, String author,
                                     String imageUrl, String genre, String publisher,
                                     Integer publishedYear, LocalDate recommendedAt) {
        this.bookId = bookId;
        this.isbn13 = isbn13;
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.publisher = publisher;
        this.publishedYear = publishedYear;
        this.recommendedAt = recommendedAt;
    }
}
