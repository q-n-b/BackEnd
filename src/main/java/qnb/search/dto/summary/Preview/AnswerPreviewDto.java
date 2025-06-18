package qnb.search.dto.summary.Preview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerPreviewDto {
    private Long answerId;
    private Long questionId;
    private Long bookId;
    private String bookTitle;
    private String answerContent;
}
