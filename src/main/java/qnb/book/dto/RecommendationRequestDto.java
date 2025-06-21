package qnb.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationRequestDto {
    private Long userId;
    private List<Integer> preferredGenres;
    private int importantFactor;
}

