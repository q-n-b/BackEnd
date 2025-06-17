package qnb.question.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.answer.dto.AnswersByUserDto;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDetailResponseDto {
    private QuestionResponseDto question;
    private List<AnswersByUserDto> answersByUser;
}
