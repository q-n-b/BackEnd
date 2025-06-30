package qnb.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionScrapResponseDto {
    private Integer questionId;
    private int scrapCount;
    private boolean isScrapped;
}
