package qnb.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.search.dto.summary.AnswerSummaryDto;
import qnb.search.dto.summary.BookSummaryDto;
import qnb.search.dto.summary.QuestionSummaryDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SummarySearchResponseDto {
    private BookSummaryDto bookSummary;
    private QuestionSummaryDto questionSummary;
    private AnswerSummaryDto answerSummary;

    public static SummarySearchResponseDto of(
            BookSummaryDto bookSummary,
            QuestionSummaryDto questionSummary,
            AnswerSummaryDto answerSummary
    ) {
        return new SummarySearchResponseDto(bookSummary, questionSummary, answerSummary);
    }
}