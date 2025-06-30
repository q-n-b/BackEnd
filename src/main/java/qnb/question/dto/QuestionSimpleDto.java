package qnb.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import qnb.question.entity.Question;

@Getter
@Builder
@AllArgsConstructor
public class QuestionSimpleDto {
    private Long questionId;
    private String questionContent;

    public static QuestionSimpleDto from(Question question) {
        if (question == null) {
            return null;
        }

        return QuestionSimpleDto.builder()
                .questionId(question.getQuestionId() != null ? question.getQuestionId().longValue() : null)
                .questionContent(question.getQuestionContent() != null ? question.getQuestionContent() : "")
                .build();
    }
}
