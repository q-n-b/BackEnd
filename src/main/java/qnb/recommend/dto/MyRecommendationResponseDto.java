package qnb.recommend.dto;

//MY 페이지의 <추천 도서 리스트> 조회시 사용할 응답 DTO
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyRecommendationResponseDto {
    private List<MyRecommendationYearMonthGroupDto> recommendedBooks;
}
