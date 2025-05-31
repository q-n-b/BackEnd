package com.example.qnb.question.dto;

import com.example.qnb.common.dto.PageInfo;

import java.util.List;

public class QuestionPageResponseDto {
    private List<QuestionResponseDto> questions;
    private PageInfo pageInfo;

    public QuestionPageResponseDto(List<QuestionResponseDto> questions, PageInfo pageInfo) {
        this.questions = questions;
        this.pageInfo = pageInfo;
    }
}
