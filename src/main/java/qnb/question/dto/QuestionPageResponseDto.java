package qnb.question.dto;

import qnb.common.dto.PageInfoDto;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionPageResponseDto {
    private List<QuestionResponseDto> questions;
    private PageInfoDto pageInfoDto;

    public QuestionPageResponseDto(List<QuestionResponseDto> questions, PageInfoDto pageInfoDto) {
        this.questions = questions;
        this.pageInfoDto = pageInfoDto;
    }
}

