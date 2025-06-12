package com.example.qnb.answer.dto;

import com.example.qnb.answer.entity.Answer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerRequestDto {
    private String answerContent;
    private String answerState;
}

