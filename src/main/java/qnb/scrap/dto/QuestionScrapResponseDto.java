package qnb.scrap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionScrapResponseDto {
    private Integer questionId;
    private int scrapCount;

    @JsonProperty("isScrapped")
    private boolean isScrapped;
}
