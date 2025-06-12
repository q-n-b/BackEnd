package qnb.question.dto;
//질문 Request Dto

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class QuestionRequestDto {
    @NotBlank(message = "질문 내용을 입력해주세요.")
    private String questionContent;
}
