package qnb.recommend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import qnb.recommend.entity.UserWeeklyFeaturedBook;

import java.time.LocalDate;
import java.time.LocalDateTime;

//추천 도서 이번주 확정본 조회에서 쓰이는 DTO

@Getter
@Builder
public class WeeklyFeaturedDto {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDate weekStartDate;
    private Double score;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static WeeklyFeaturedDto from(UserWeeklyFeaturedBook w) {
        return WeeklyFeaturedDto.builder()
                .id(w.getId())
                .userId(w.getUserId())
                .bookId(w.getBookId())
                .weekStartDate(w.getWeekStartDate())
                .score(w.getScore())
                .createdAt(w.getCreatedAt())
                .build();
    }
}
