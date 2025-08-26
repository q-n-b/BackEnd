package qnb.recommend.dto;

//MY 페이지의 <추천 도서 리스트> 조회시 월별로 사용할 응답 DTO

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyRecommendationYearMonthGroupDto {
    
    // 프론트 스펙이 대문자 "YearMonth" 이므로 JsonProperty로 키 고정
    @JsonProperty("YearMonth")
    private String yearMonth;

    private List<MyRecommendationBookItemDto> books;
}