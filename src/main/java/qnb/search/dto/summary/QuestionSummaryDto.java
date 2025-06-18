package qnb.search.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.search.dto.summary.Preview.QuestionPreviewDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSummaryDto {
    private int count;
    private List<QuestionPreviewDto> preview;
}
