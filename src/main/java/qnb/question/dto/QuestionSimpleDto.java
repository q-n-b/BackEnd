package qnb.question.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionSimpleDto {
    private Long questionId;
    private String questionContent;
}
