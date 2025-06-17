package qnb.answer.dto;

//답변의 간단한 정보만 담는 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.answer.entity.Answer;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerSimpleDto {
    private Long answerId;
    private Long userId;
    private String answerContent;
    private String answerState;

    public static AnswerSimpleDto from(Answer answer) {
        return new AnswerSimpleDto(
                answer.getAnswerId(),
                answer.getUserId(),
                answer.getAnswerContent(),
                answer.getAnswerState()
        );
    }
}

