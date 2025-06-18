package qnb.search.dto.summary.Preview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPreviewDto {
    private Long questionId;
    private Long bookId;
    private String title;
    private String author;
    private String questionContent;
}
