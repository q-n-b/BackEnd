package qnb.search.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.search.dto.summary.Preview.AnswerPreviewDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSummaryDto {
    private int count;
    private List<AnswerPreviewDto> preview;
}