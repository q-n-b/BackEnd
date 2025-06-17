package qnb.answer.dto;
//AnswersByUserDto 생성 시 User user에서 직접 값을 꺼내는 방식

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AnswersByUserDto {
    private Long userId;
    private String userNickname;
    private String profileUrl;
    private List<AnswerResponseDto> answers;
}
