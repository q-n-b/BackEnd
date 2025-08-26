package qnb.recommend.dto;

//MY 페이지의 <추천 도서 리스트> 조회시 사용할 응답 DTO

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MyRecommendationBookItemDto {
    private Integer bookId;
    private String isbn13;
    private String title;
    private String author;
    private String imageUrl;
    private String genre;
    private String publisher;
    private String publishedYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recommendedAt; // = weekStartDate (월요일)
}
