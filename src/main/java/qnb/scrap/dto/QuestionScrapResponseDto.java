package qnb.scrap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionScrapResponseDto {
    private Integer questionId;
    private int scrapCount;

    private boolean scrapped;

    @JsonProperty("isScrapped")
    public boolean isScrapped() {
        return scrapped;
    }
}
