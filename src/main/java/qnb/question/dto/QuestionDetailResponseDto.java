package qnb.question.dto;
//질문 상세 조회시에 쓰이는 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.answer.dto.AnswersByUserDto;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDetailResponseDto {
    private QuestionListItemDto question;
    private List<AnswersByUserDto> answersByUser;
}
